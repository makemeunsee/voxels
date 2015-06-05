package voxels

import geometry.Vec3

/**
 * Created by markus on 23/05/15.
 */

object Voxel {
  type Vertex = Vec3

  case class Face( vertices: List[Vertex], faceType: FaceType ) {
    lazy val center = vertices.reduce( _+ _ ) / vertices.length
    lazy val normal = center.normalize
    def rawVertices = vertices.flatMap{ case Vec3( x, y, z ) => x :: y :: z :: Nil }
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
  def facesStructure: List[( List[Int], FaceType )]
  def scale = {
    val vertsIds = facesStructure.head
    ( vertices( vertsIds._1.head ) - vertices( vertsIds._1.tail.head ) ).norm
  }
}

case class Voxel( standard: VoxelStandard ) {
  val faces = standard.facesStructure map { case ( l, t ) =>
    Face( l map { i => standard.vertices( i ) / standard.scale }, t )
  }
}