package voxels

import geometry.Vec3
import voxels.Voxel.Face
import voxels.Voxel.Vertex

/**
 * Created by markus on 23/05/15.
 */
object Cube extends Voxel {
  val faceCount = 6
  val verticesCount = 8

  val vertices: List[Vertex] =
    Vec3( 1,1,1 ) ::
    Vec3( -1,1,1 ) ::
    Vec3( -1,1,-1 ) ::
    Vec3( 1,1,-1 ) ::
    Vec3( -1,-1,-1 ) ::
    Vec3( 1,-1,-1 ) ::
    Vec3( 1,-1,1 ) ::
    Vec3( -1,-1,1 ) :: Nil

  val faces =
    Face( List( vertices( 0 ), vertices( 3 ), vertices( 2 ), vertices( 1 ) ) ) ::
    Face( List( vertices( 4 ), vertices( 5 ), vertices( 6 ), vertices( 7 ) ) ) ::
    Face( List( vertices( 0 ), vertices( 6 ), vertices( 5 ), vertices( 3 ) ) ) ::
    Face( List( vertices( 1 ), vertices( 2 ), vertices( 4 ), vertices( 7 ) ) ) ::
    Face( List( vertices( 0 ), vertices( 1 ), vertices( 7 ), vertices( 6 ) ) ) ::
    Face( List( vertices( 2 ), vertices( 3 ), vertices( 5 ), vertices( 4 ) ) ) :: Nil
}
