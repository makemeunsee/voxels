package voxels

import geometry.Vec3

/**
 * Created by markus on 23/05/15.
 */

object Voxel {
  type Vertex = Vec3

  case class Face(vertices: List[Vertex]) {
    lazy val center = vertices.reduce( _+ _) / vertices.length
    lazy val normal = center.normalize
    def rawVertices = vertices.flatMap{ case Vec3( x, y, z ) => x :: y :: z :: Nil }
  }
}

import Voxel._

trait VoxelStandard {
  def faceCount: Int
  def verticesCount: Int
  def vertices: List[Vertex]
  def facesStructure: List[List[Int]]
  def scale = {
    val vertsIds = facesStructure.head
    ( vertices( vertsIds.head ) - vertices( vertsIds.tail.head ) ).norm
  }
}

case class Voxel( standard: VoxelStandard ) {
  val faces = standard.facesStructure map { l =>
    Face( l map { i => standard.vertices( i ) / standard.scale } )
  }
}