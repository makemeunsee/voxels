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

trait Voxel {
  def faceCount: Int
  def verticesCount: Int
  def vertices: List[Vertex]
  def faces: List[Face]
}
