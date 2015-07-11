package geometry.voronoi

import geometry.{Normal3, Vector3}
import voxels.Cube

import scala.annotation.tailrec

/**
 * Created by markus on 29/06/15.
 */
object VoronoiModel {

  sealed trait ToPlane
  object Above extends ToPlane
  object Below extends ToPlane
  object OnPlane extends ToPlane
  val tolerance = 1e-8d

  // gives the position of a point 'p' to a plane of normal 'planeNormal' (as a unit vector) and seed 'planeNormal' (as a point)
  private def toCutPlane( planeNormal: Normal3 )( p: Vector3 ): ToPlane = {
    val diff = p - planeNormal
    diff.x * planeNormal.x + diff.y * planeNormal.y + diff.z * planeNormal.z match {
      case k if math.abs( k ) <= tolerance => OnPlane
      case k if k > tolerance              => Above
      case _                               => Below
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

  private def intersectPlaneAndSegment( planeNormal: Normal3, segment: ( Vector3, Vector3 ) ): Vector3 = {
    val ( p0, p1 ) = orderPoints( segment._1, segment._2 )
    val d = p1 - p0
    val k = planeNormal.x * d.x + planeNormal.y * d.y + planeNormal.z * d.z
    if ( tolerance >= math.abs( k ) )
      throw new Error( "bad cut!")
    else {
      val at = ( planeNormal.x*( planeNormal.x-p0.x ) + planeNormal.y*( planeNormal.y-p0.y ) + planeNormal.z*( planeNormal.z-p0.z ) ) / k
      p0 + ( d * at )
    }
  }

  @tailrec
  private def cyclicPairs[A]( first: A, seq: Seq[A], acc: Seq[( A, A )] ): Seq[( A, A )] = seq match {
    case Nil             => acc.reverse
    case a :: Nil        => cyclicPairs( first, Nil, ( a, first ) +: acc )
    case a0 :: a1 :: as  => cyclicPairs( first, a1 :: as, ( a0, a1 ) +: acc )
  }
  private def cyclicConsecutivePairs[A]( seq: Seq[A] ): Seq[( A, A )] = seq match {
    case Nil     => Seq.empty
    case a :: _  => cyclicPairs( a, seq, Seq.empty )
  }

  @tailrec
  private def cyclicRemoveDups[A]( seq: Seq[A], acc: Seq[A] ): Seq[A] = seq match {
    case Nil     => acc.reverse
    case a :: as =>
      cyclicRemoveDups( as.dropWhile( _ == a ), a +: acc )
  }
  private def cyclicRemoveConsecutiveDuplicates[A]( seq: Seq[A] ): Seq[A] = {
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
  private def removeDuplicates[A]( seq: Seq[A] ): Seq[A] = removeDups( seq, Seq.empty )

  @tailrec
  private def chain( iPts: Map[Vector3, Set[Int]], acc: Seq[( Vector3, Set[Int] )] ): Seq[Vector3] = {
    if ( iPts.isEmpty )
      acc.unzip._1
    else {
      val ids = acc.head._2
      iPts.find( _._2.intersect( ids ).size == 1 ) match {
        case None         => chain( Map.empty, acc )
        case Some( next ) => chain( iPts - next._1, next +: acc )
      }
    }
  }
  def chain( iPts: Map[Vector3, Set[Int]] ): Seq[Vector3] = if ( iPts.isEmpty ) Seq.empty else chain( iPts.tail, Seq( iPts.head ) )

  def maybeFlip( n: Normal3, pts: Seq[Vector3] ): Seq[Vector3] = {
    if ( pts.length > 2 ) {
      val ptsNormal = ( pts( 1 ) - pts( 0 ) ) cross ( pts( 2 ) - pts( 1 ) )
      if ( 0 > ( ptsNormal dot n ) )
        pts.reverse
      else
        pts
    } else
      pts
  }

  @tailrec
  private def cleanNeighs( indexedFaces: Seq[( Int, Face )], acc: Seq[( Int, Face )] ): Seq[( Int, Face )] = indexedFaces match {
    case Nil             => acc
    case ( i, f ) :: ifs =>
      val ( cleanedF, cleanedFs ) = ifs
        .foldLeft( ( i, f ), Seq.empty[( Int, Face )] ) { case ( ( ( i0, f0 ), newIFs ), ( i1, f1 ) ) =>
        if ( f0.neighbours.contains( i1 ) )
          if ( 2 <= f0.vertices.count( f1.vertices.contains ) )
            ( ( i0, f0 ), ( i1, f1 ) +: newIFs )
          else
            ( ( i0, f0.copy( neighbours = f0.neighbours.filter( _ != i1 ) ) )
              , ( i1, f1.copy( neighbours = f1.neighbours.filter( _ != i0 )) ) +: newIFs )
        else
          ( ( i0, f0 ), ( i1, f1 ) +: newIFs )
      }
      cleanNeighs( cleanedFs, cleanedF +: acc )
  }
  def cleanNeighbours( indexedFaces: Seq[( Int, Face )] ): Seq[( Int, Face )] = cleanNeighs( indexedFaces, Seq.empty )

  type Cut = ( Int, Face, Seq[Vector3] )

  def cutFace( newFaceId: Int, face: Face, n: Normal3 ): ( Face, Seq[Vector3] ) = {
    def toPlane = toCutPlane( n ) _
    val cutInfo = face.vertices.map( v => ( v, toPlane( v ) ) )
    val afterCut = cyclicConsecutivePairs( cutInfo )
      .foldLeft( List.empty[Vector3], List.empty[Vector3]  ) { case ( ( allVerts, newVerts ), ( vi0, vi1 ) ) =>
        ( vi0, vi1 ) match {
          case ( ( v0, OnPlane ), ( v1, OnPlane ) ) =>
            // both on the plane -> both on edge, both kept
            ( v1 :: v0 :: allVerts, v1 :: v0 :: newVerts )
          case ( ( v0, OnPlane ), ( _, Above ) ) =>
            // only keep the one on the plane
            ( v0 :: allVerts, v0 :: newVerts )
          case ( ( _, Above ),( v1, OnPlane ) )   =>
            // only keep the one on the plane
            ( v1 :: allVerts, v1 :: newVerts )
          case ( ( _, Above ),( _, Above ) )       =>
            // discard both, no cut edge
            ( allVerts, newVerts )
          case ( ( v0, Below ),( v1, Above ) )     =>
            // cut, keep first and create one, new one on cut edge
            val newV = intersectPlaneAndSegment( n, ( v0, v1 ) )
            ( newV :: v0 :: allVerts, newV :: newVerts )
          case ( ( v0, Above ),( v1, Below ) )     =>
            // cut, keep second and create one, new one on cut edge
            val newV = intersectPlaneAndSegment( n, ( v0, v1 ) )
            ( v1 :: newV :: allVerts, newV :: newVerts )
          case ( ( v0, Below ),( v1, Below ) )     =>
            // keep both, no cut edge
            ( v1 :: v0 :: allVerts, newVerts )
          case ( ( v0, Below ),( v1, OnPlane ) )   =>
            // keep both, first on cut edge
            ( v1 :: v0 :: allVerts, v1 :: newVerts )
          case ( ( v0, OnPlane ),( v1, Below ) )   =>
            // keep both, second on cut edge
            ( v1 :: v0 :: allVerts, v0 :: newVerts )
        }
      }
    val edgePoints = removeDuplicates( afterCut._2.reverse )
    val newNeighbours = if ( edgePoints.isEmpty ) face.neighbours else face.neighbours + newFaceId
    val newVertices = cyclicRemoveConsecutiveDuplicates( afterCut._1.reverse )
    ( Face( face.seed, newVertices, newNeighbours ), edgePoints )
  }

  private val cube = Cube.facesStructure map { _._1 map { i => Cube.vertices( i ) } }

  private val cubeFaceInfo: Seq[( Normal3, Set[Int] )] = Seq(
    ( Normal3( 0,  1,  0 ),  Set( 2, 5, 3, 4 ) ),
    ( Normal3( 0,  -1, 0 ),  Set( 2, 5, 3, 4 ) ),
    ( Normal3( 1,  0,  0 ),  Set( 0, 4, 1, 5 ) ),
    ( Normal3( -1, 0,  0 ),  Set( 0, 4, 1, 5 ) ),
    ( Normal3( 0,  0,  1 ),  Set( 0, 3, 1, 5 ) ),
    ( Normal3( 0,  0,  -1 ), Set( 0, 3, 1, 5 ) )
  )

  private val cubeFaces = cube
    .zip( cubeFaceInfo )
    .map { case ( vs, ( n, ns ) ) =>
      Face( n, vs, ns )
    }.toArray

  object CubeModel extends VoronoiModelImpl( cubeFaces )
}

trait VoronoiModel {
  def faces: Array[Face]
  // mutates the original!
  def cut( ns: Seq[Normal3] ): VoronoiModel
}

import geometry.voronoi.VoronoiModel._

case class VoronoiModelImpl( faces: Array[Face] ) extends VoronoiModel {

  // mutates!
  // faces array must be of the proper size to accomodate the new face at position newFaceId
  private def cut( n: Normal3, newFaceId: Int ): Unit = {
    // the find the closest face to the cut
    val ( closestId, _ ) = closestSeed( n )

    // cut the closest face, and its neighbours, and their neighbours, and so on,
    // until nothing is cut
    val cuts = doCuts( n, newFaceId, Set( closestId ), Set( closestId ), Seq.empty )

    if ( cuts.isEmpty ) {
      println("cut cancelled", n)
      throw new Error("bad cut!")
    } else {
      //      println("cutting", n)

      // update topology (neighbours of each face) then update the face array with the updated faces
      cleanNeighbours( cuts.map( triplet => ( triplet._1, triplet._2 ) ) )
        .foreach { case ( i, f ) =>
          faces.update( i, f )
        }

      // gather all points created by the cut, attaching to them the indices of the face they're part of
      val newPoints = cuts
        .foldLeft( Seq.empty[( Vector3, Int )] ) { case ( allPts, ( i, _, pts ) ) =>
          pts.map( p => ( p, i ) ) ++ allPts
        }
        .groupBy( _._1 )
        .map { case ( p, pairs ) => ( p, pairs.unzip._2.toSet ) }
      // chain the points into a coherent polygon
      // and reverse the polygon if needed
      val orderedPoints = maybeFlip( n, chain( newPoints ) )
      // gather all face indices (neighbours) for each new point
      val newFaceNeighbours = newPoints.values.reduce( _ ++ _ )
      // create a new face
      val newFace = Face( n, orderedPoints, newFaceNeighbours )

      faces.update( newFaceId, newFace )
    }
  }

  // mutates!
  def cut( ns: Seq[Normal3] ): VoronoiModel = {
    val fL = faces.length
    val nL = ns.length
    val newFaces: Array[Face] = Array.fill( fL + nL )( null: Face )
    Array.copy( faces, 0, newFaces, 0, fL )
    // create a model, copy of the original, with a face array of the proper length
    val cutModel = VoronoiModelImpl( newFaces )
    // do cuts -> for each cut, update the existing faces and add a new face in the faces array
    for( i <- 0 until nL ) {
      cutModel.cut( ns( i ), i + fL )
    }
    cutModel
  }

  // find all the faces cut at the normal n, returns all the faces with the cut applied
  @tailrec
  private def doCuts( n: Normal3, newFaceId: Int, ids: Set[Int], visited: Set[Int], acc: Seq[Cut] ): Seq[Cut] =
    if ( ids.isEmpty )
      acc
    else {
      val i = ids.head
      val t = ids.tail
      val face = faces( i )
      val ( newFace, newPoints ) = cutFace( newFaceId, face, n )
      // valid intersection of polygon edges and plane happens at only 2 points
      if ( newPoints.size != 2 )
        doCuts( n, newFaceId, t, visited + i, acc )
      else
        doCuts( n
              , newFaceId
              , t ++ face.neighbours.filterNot( j => t.contains( j ) || visited.contains( j ) )
              , visited + i
              , ( i, newFace, newPoints ) +: acc )
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
}