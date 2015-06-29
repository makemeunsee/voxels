package voxels

import geometry.{Vector3, Normal3, Matrix4}

/**
 * Created by markus on 23/05/15.
 */

object Voxel {
  type Vertex = Vector3

  case class Face( vertices: List[Vertex]
                 , faceType: FaceType
                 ) {
    assert( vertices.length > 2 )
    def center = vertices.reduce( _ + _ ) / vertices.length
    def normal = Normal3( vertices( 2 ) - vertices( 1 ) cross ( vertices( 0 ) - vertices( 1 ) ) )
    def rawVertices = vertices.flatMap{ v => v.x :: v.y :: v.z :: Nil }
  }

  sealed trait FaceType
  case class RegularPolygon( sides: Int ) extends FaceType
  case object StdRhombus extends FaceType
}

import Voxel._

trait VoxelStandard {
  def name: String = getClass.getSimpleName
  def faceCount = facesStructure.size
  def verticesCount = vertices.size
  def vertices: List[Vertex]
  def facesStructure: List[( List[Int], FaceType, Seq[Int] )]
  def scale = {
    val vertsIds = facesStructure.head
    ( vertices( vertsIds._1.head ) - vertices( vertsIds._1.tail.head ) ).norm
  }
  def uniquePositionings: Seq[(Int, FaceType, Int)] = {
    facesStructure
      .zipWithIndex
      .groupBy { case ( ( _, fType, rots ), _ ) => ( fType, rots ) }
      .collect { case ( ( fType, rots ), l ) if l.nonEmpty => rots.map( r => ( l.head._2, fType, r ) ) }
      .flatten
  }.toSeq
}

case class Voxel( standard: VoxelStandard
                , transformation: Matrix4
                , colors: Seq[( Int, Int )] ) {
  assert( colors.length == standard.faceCount )

  val faces = standard.facesStructure map { case ( l, t, _ ) =>
    Face( l map { i => transformation( standard.vertices( i ) / standard.scale ) }, t )
  }
}