package voxels

import geometry.Vec3
import voxels.Voxel._

/**
 * Created by markus on 26/05/15.
 */
object Octahedron extends Voxel {
  val faceCount = 8
  val verticesCount = 6

  val vertices: List[Vertex] =
    Vec3( 0,1,0 ) ::
    Vec3( 1,0,0 ) ::
    Vec3( 0,0,1 ) ::
    Vec3( -1,0,0 ) ::
    Vec3( 0,0,-1 ) ::
    Vec3( 0,-1,0 ) :: Nil

  val faces =
    Face( List( vertices( 0 ), vertices( 2 ), vertices( 1 ) ) ) ::
    Face( List( vertices( 0 ), vertices( 3 ), vertices( 2 ) ) ) ::
    Face( List( vertices( 0 ), vertices( 4 ), vertices( 3 ) ) ) ::
    Face( List( vertices( 0 ), vertices( 1 ), vertices( 4 ) ) ) ::
    Face( List( vertices( 5 ), vertices( 1 ), vertices( 2 ) ) ) ::
    Face( List( vertices( 5 ), vertices( 2 ), vertices( 3 ) ) ) ::
    Face( List( vertices( 5 ), vertices( 3 ), vertices( 4 ) ) ) ::
    Face( List( vertices( 5 ), vertices( 4 ), vertices( 1 ) ) ) :: Nil

}
