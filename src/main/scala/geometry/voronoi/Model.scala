package geometry.voronoi

import demo.Colors
import geometry.{Vector3, Normal3}
import voxels.Cube

import scala.annotation.tailrec

/**
 * Created by markus on 29/06/15.
 */
object Model {
  sealed trait ToPlane
  object Above extends ToPlane
  object Below extends ToPlane
  object OnPlane extends ToPlane
  val tolerance = 1e-8d

  // gives the position of a point 'p' to a plane of normal 'planeNormal' (as a unit vector) and seed 'planeNormal' (as a point)
  def toCutPlane( planeNormal: Normal3 )( p: Vector3 ): ToPlane = {
    val diff = p - planeNormal
    diff.x * planeNormal.x + diff.y * planeNormal.y + diff.z * planeNormal.z match {
      case k if k <= tolerance => OnPlane
      case k if k > tolerance  => Above
      case _                   => Below
    }
  }

  private def orderPoints( p0: Vector3, p1: Vector3 ): ( Vector3, Vector3 ) =
    if( p0.x < p1.x )
      ( p0, p1 )
    else if( p1.x < p0.x )
      ( p1, p0 )
    else if( p0.y < p1.y )
      ( p0, p1 )
    else if( p1.y < p0.y )
      ( p1, p0 )
    else if( p0.z < p1.z )
      ( p0, p1 )
    else if( p1.z < p0.z )
      ( p1, p0 )
    else
      ( p0, p1 )

  def intersectPlaneAndSegment( planeNormal: Normal3, segment: ( Vector3, Vector3 ) ): Option[Vector3] = {
    val ( p0, p1 ) = orderPoints( segment._1, segment._2 )
    val d = p1 - p0
    val k = planeNormal.x * d.x + planeNormal.y * d.y + planeNormal.z * d.z
    if ( tolerance >= math.abs( k ) )
      None
    else {
      val at = ( planeNormal.x*( planeNormal.x-p0.x ) + planeNormal.y*( planeNormal.y-p0.y ) + planeNormal.z*( planeNormal.z-p0.z ) ) / k
      Some( p0 + ( d * at ) )
    }
  }

  @tailrec
  private def cyclicPairs[A]( first: A, seq: Seq[A], acc: Seq[( A, A )] ): Seq[( A, A )] = seq match {
    case Nil             => acc.reverse
    case a :: Nil        => cyclicPairs( first, Nil, ( first, a ) +: acc )
    case a0 :: a1 :: as  => cyclicPairs( first, a1 :: as, ( a0, a1 ) +: acc )
  }
  def cyclicConsecutivePairs[A]( seq: Seq[A] ): Seq[( A, A )] = seq match {
    case Nil     => Seq.empty
    case a :: _  => cyclicPairs( a, seq, Seq.empty )
  }

  @tailrec
  private def cyclicRemoveDups[A]( seq: Seq[A], acc: Seq[A] ): Seq[A] = seq match {
    case Nil     => acc.reverse
    case a :: as =>
      cyclicRemoveDups( as.dropWhile( _ == a ), a +: acc )
  }
  def cyclicRemoveConsecutiveDuplicates[A]( seq: Seq[A] ): Seq[A] = {
    val cleaned = cyclicRemoveDups( seq, Seq.empty )
    val l = cleaned.length
    if ( l > 1 && cleaned.head == cleaned.last )
      cleaned.take( l - 1 )
    else
      cleaned
  }

  @tailrec
  private def removeDups[A]( seq: Seq[A], acc: Seq[A] ): Seq[A] = seq match {
    case Nil     => acc.reverse
    case a :: as =>
      if ( as.contains( a ) )
        removeDups( as, acc )
      else
        removeDups( as, a +: acc )
  }
  def removeDuplicates[A]( seq: Seq[A] ): Seq[A] = removeDups( seq, Seq.empty )
}

trait Model {
  def faces: Array[Face]
  def cut( theta: Double, phi: Double ): Model = cut( Normal3( Vector3.latLongPosition( theta, phi, 1 ) ) )
  def cut( n: Normal3 ): Model
}

import Model._

case class CubeModel( colors: Seq[( Int, Int )] ) extends Model {
  def cut( n: Normal3 ) = {
    val ( closestId, closestSd ) = closestSeed( n )
    val cuts = doCuts( n, List( closestId ), Set( closestId ), Seq.empty )
    // TODO
    this
  }

  type Cut = ( Int, Face, Seq[Vector3] )
  @tailrec
  private def doCuts( n: Normal3, ids: Seq[Int], visited: Set[Int], acc: Seq[Cut] ): Seq[Cut] = ids match {
    case Nil    => acc
    case i :: t =>
      val face = faces( i )
      val ( newFace, newPoints ) = cutFace( faces.length, face, n )
      // valid intersection of polygon edges and plane happens at only 2 points
      if ( newPoints.size != 2 )
        doCuts( n, t, visited + i, acc )
      else
        doCuts( n
              , ( face.neighbours ++ t ).filterNot( j => t.contains( j ) || visited.contains( j ) )
              , visited + i
              , ( i, newFace, newPoints ) +: acc )
  }

  private def cutFace( newFaceId: Int, face: Face, n: Normal3 ): ( Face, Seq[Vector3] ) = {
    def toPlane = toCutPlane( n ) _
    val cutInfo = face.vertices.map( v => ( v, toPlane( v ) ) )
    val afterCut = cyclicConsecutivePairs( cutInfo )
      .foldLeft( List.empty[Vector3], List.empty[Vector3]  ) { case ( ( allVerts, newVerts ), ( vi0, vi1 ) ) =>
        ( vi0, vi1 ) match {
          case ( ( v0, OnPlane ), ( v1, OnPlane ) ) =>
            // both on the plane -> both on edge, both kept
            ( v0 :: v1 :: allVerts, v0 :: v1 :: newVerts )
          case ( ( v0, OnPlane ), ( _, Above ) ) =>
            // only keep the one on the plane
            ( v0 :: allVerts, v0 :: newVerts )
          case ( ( _,Above ),( v1,OnPlane ) )   =>
            // only keep the one on the plane
            ( v1 :: allVerts, v1 :: newVerts )
          case ( ( _,Above ),( _,Above ) )       =>
            // discard both, no cut edge
            ( allVerts, newVerts )
          case ( ( v0,Below ),( v1,Above ) )     =>
            // cut, keep first and create one, new one on cut edge
            val newV = intersectPlaneAndSegment( n, ( v0,v1 ) ).get
            ( v0 :: newV :: allVerts, newV :: newVerts )
          case ( ( v0,Above ),( v1,Below ) )     =>
            // cut, keep second and create one, new one on cut edge
            val newV = intersectPlaneAndSegment( n, ( v0,v1 ) ).get
            ( newV :: v1 :: allVerts, newV :: newVerts )
          case ( ( v0,Below ),( v1,Below ) )     =>
            // keep both, no cut edge
            ( v0 :: v1 :: allVerts, newVerts )
          case ( ( v0,Below ),( v1,OnPlane ) )   =>
            // keep both, first on cut edge
            ( v0 :: v1 :: allVerts, v1 :: newVerts )
          case ( ( v0,OnPlane ),( v1,Below ) )   =>
            // keep both, second on cut edge
            ( v0 :: v1 :: allVerts, v0 :: newVerts )
        }
      }
    val edgePoints = removeDuplicates( afterCut._2 )
    val newNeighbours = if ( edgePoints.isEmpty ) face.neighbours else newFaceId +: face.neighbours
    val newVertices = cyclicRemoveConsecutiveDuplicates( afterCut._1 )
    ( Face( n, newVertices, newNeighbours, Colors.WHITE, Colors.WHITE ), edgePoints )
  }

  private def closestSeed( seed: Normal3 ): ( Int, Normal3 ) = {
    @tailrec
    def closestRec( i: Int ): ( Int, Normal3 ) = {
      val face = faces( i )
      val faceSeed = face.seed
      // among the neighbours of a given face, look for the one which seed is the closest to the given seed
      val ( closestNeighbour, _ ) = face.neighbours.foldLeft( i, Vector3.dist( faceSeed, seed ) ) { case ( ( closerId, d ), j ) =>
        val neighbourFace = faces( j )
        val neighbourSeed = neighbourFace.seed
        val newD = Vector3.dist( neighbourSeed, seed )
        if ( newD < d )
          ( j, newD )
        else
          ( closerId, d )
      }
      // if no neighbour is closer, this face is the one we were looking for
      if ( closestNeighbour == i )
        ( i, faceSeed )
      // otherwise, we keep on looking using the closest face we found to start from
      else
        closestRec( closestNeighbour )
    }
    closestRec( 0 )
  }

  private val cube = Cube.facesStructure map { _._1 map { i => Cube.vertices( i ) } }

  private val faceInfo: Seq[( Normal3, Seq[Int] )] = Seq(
    ( Normal3( 0,  1,  0 ),  Seq( 2, 5, 3, 4 ) ),
    ( Normal3( 0,  -1, 0 ),  Seq( 2, 5, 3, 4 ) ),
    ( Normal3( 1,  0,  0 ),  Seq( 0, 4, 1, 5 ) ),
    ( Normal3( -1, 0,  0 ),  Seq( 0, 4, 1, 5 ) ),
    ( Normal3( 0,  0,  1 ),  Seq( 0, 3, 1, 5 ) ),
    ( Normal3( 0,  0,  -1 ), Seq( 0, 3, 1, 5 ) )
  )

  val faces = cube
    .zip( faceInfo )
    .zip( colors )
    .map { case ( ( vs, ( n, ns ) ), ( color, centerColor ) ) =>
      Face( n, vs, ns, color, centerColor )
    }.toArray
}