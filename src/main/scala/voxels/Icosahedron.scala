package voxels

import geometry.Vec3
import geometry.gold
import voxels.Voxel.{Face, Vertex}

/**
 * Created by markus on 26/05/15.
 */
object Icosahedron extends Voxel {
  val faceCount = 20
  val verticesCount = 12

  val vertices: List[Vertex] =
    Vec3 ( 0, 1, gold ) ::
    Vec3 ( 0, -1, gold ) ::
    Vec3 ( 0, -1, -gold ) ::
    Vec3 ( 0, 1, -gold ) ::
    Vec3 ( 1,  gold, 0 ) ::
    Vec3 ( -1, gold, 0 ) ::
    Vec3 ( -1, -gold, 0 ) ::
    Vec3 ( 1, -gold, 0 ) ::
    Vec3 ( gold, 0, 1 ) ::
    Vec3 ( gold, 0, -1 ) ::
    Vec3 ( -gold, 0, -1 ) ::
    Vec3 ( -gold, 0, 1 ) :: Nil

  val faces =
    Face( List( vertices( 0 ), vertices( 1 ), vertices( 8 ) ) ) ::
    Face( List( vertices( 0 ), vertices( 11 ), vertices( 1 ) ) ) ::
    Face( List( vertices( 2 ), vertices( 3 ), vertices( 9 ) ) ) ::
    Face( List( vertices( 2 ), vertices( 10 ), vertices( 3 ) ) ) ::
    Face( List( vertices( 4 ), vertices( 5 ), vertices( 0 ) ) ) ::
    Face( List( vertices( 4 ), vertices( 3 ), vertices( 5 ) ) ) ::
    Face( List( vertices( 6 ), vertices( 7 ), vertices( 1 ) ) ) ::
    Face( List( vertices( 6 ), vertices( 2 ), vertices( 7 ) ) ) ::
    Face( List( vertices( 8 ), vertices( 9 ), vertices( 4 ) ) ) ::
    Face( List( vertices( 8 ), vertices( 7 ), vertices( 9 ) ) ) ::
    Face( List( vertices( 10 ), vertices( 11 ), vertices( 5 ) ) ) ::
    Face( List( vertices( 10 ), vertices( 6 ), vertices( 11 ) ) ) ::
    Face( List( vertices( 0 ), vertices( 8 ), vertices( 4 ) ) ) ::
    Face( List( vertices( 0 ), vertices( 5 ), vertices( 11 ) ) ) ::
    Face( List( vertices( 1 ), vertices( 7 ), vertices( 8 ) ) ) ::
    Face( List( vertices( 1 ), vertices( 11 ), vertices( 6 ) ) ) ::
    Face( List( vertices( 2 ), vertices( 9 ), vertices( 7 ) ) ) ::
    Face( List( vertices( 2 ), vertices( 6 ), vertices( 10 ) ) ) ::
    Face( List( vertices( 3 ), vertices( 4 ), vertices( 9 ) ) ) ::
    Face( List( vertices( 3 ), vertices( 10 ), vertices( 5 ) ) ) :: Nil
}
