package voxels

import geometry.Vec3
import voxels.Voxel._

/**
 * Created by markus on 26/05/15.
 */
object Tetrahedron extends Voxel {
  val faceCount = 4
  val verticesCount = 4

  val vertices: List[Vertex] =
    Vec3( 1,1,1 ) ::
    Vec3( -1,1,-1 ) ::
    Vec3( 1,-1,-1 ) ::
    Vec3( -1,-1,1 ) :: Nil

  val faces =
    Face( List( vertices( 0 ), vertices( 1 ), vertices( 3 ) ) ) ::
    Face( List( vertices( 0 ), vertices( 2 ), vertices( 1 ) ) ) ::
    Face( List( vertices( 0 ), vertices( 3 ), vertices( 2 ) ) ) ::
    Face( List( vertices( 1 ), vertices( 2 ), vertices( 3 ) ) ) :: Nil
}
