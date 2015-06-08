package voxels

import geometry.{Matrix4, Vec3}

/**
 * Created by markus on 23/05/15.
 */

object Voxel {
  type Vertex = Vec3

  case class Face( vertices: List[Vertex], faceType: FaceType ) {
    assert( vertices.length > 2 )
    def center = vertices.reduce( _ + _ ) / vertices.length
    def normal = ( vertices( 2 ) - vertices( 1 ) cross ( vertices( 0 ) - vertices( 1 ) ) ).normalize
    def rawVertices = vertices.flatMap{ case Vec3( x, y, z ) => x :: y :: z :: Nil }
    // rotation with angle ( 2 Pi / vertices.length ), with axis ( normal ), about ( center )
    def conjugationMatrix: Matrix4 = {
      val n = normal
      Matrix4.rotationMatrix( 2*math.Pi / vertices.length, n.x, n.y, n.z )
    }
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

case class Voxel( standard: VoxelStandard, transformation: Matrix4 ) {
  val faces = standard.facesStructure map { case ( l, t ) =>
    Face( l map { i => transformation( standard.vertices( i ) / standard.scale ) }, t )
  }
}