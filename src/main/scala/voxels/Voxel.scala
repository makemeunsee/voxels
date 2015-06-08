package voxels

import geometry.{Matrix4, Vec3}

/**
 * Created by markus on 23/05/15.
 */

object Voxel {
  type Vertex = Vec3

  // rotationInvariance = how many successive rotation steps (see FaceType) become an identity transformation (for the whole voxel)
  case class Face( vertices: List[Vertex], faceType: FaceType, rotationInvariance: Int ) {
    assert( vertices.length > 2 )
    def center = vertices.reduce( _ + _ ) / vertices.length
    def normal = ( vertices( 2 ) - vertices( 1 ) cross ( vertices( 0 ) - vertices( 1 ) ) ).normalize
    def rawVertices = vertices.flatMap{ case Vec3( x, y, z ) => x :: y :: z :: Nil }
  }

  sealed trait FaceType {
    // 2*Pi/rotations = rotation steps that leave the face unchanged
    def rotations: Int
  }
  case class RegularPolygon( sides: Int ) extends FaceType {
    def rotations = sides
  }
  case object StdRhombus extends FaceType {
    def rotations = 2
  }
}

import Voxel._

trait VoxelStandard {
  def name: String = getClass.getSimpleName
  def faceCount = facesStructure.size
  def verticesCount = vertices.size
  def vertices: List[Vertex]
  def facesStructure: List[( List[Int], FaceType, Int )]
  def scale = {
    val vertsIds = facesStructure.head
    ( vertices( vertsIds._1.head ) - vertices( vertsIds._1.tail.head ) ).norm
  }
}

case class Voxel( standard: VoxelStandard, transformation: Matrix4 ) {
  val faces = standard.facesStructure map { case ( l, t, rotInv ) =>
    Face( l map { i => transformation( standard.vertices( i ) / standard.scale ) }, t, rotInv )
  }
}