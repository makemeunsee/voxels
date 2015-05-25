package voxels

import geometry.Vec3
import geometry.gold
import voxels.Voxel._

/**
 * Created by markus on 26/05/15.
 */
object Dodecahedron extends Voxel {
  val faceCount = 12
  val verticesCount = 20

  val vertices: List[Vertex] =
    Vec3 ( 1, 1, 1 ) ::
    Vec3 ( 1, 1, -1 ) ::
    Vec3 ( 1, -1, 1 ) ::
    Vec3 ( 1, -1, -1 ) ::
    Vec3 ( -1, 1, 1 ) ::
    Vec3 ( -1, 1, -1 ) ::
    Vec3 ( -1, -1, 1 ) ::
    Vec3 ( -1, -1, -1) ::
    Vec3 ( 0, 1/gold, gold ) ::
    Vec3 ( 0, 1/gold, -gold ) ::
    Vec3 ( 0, -1/gold, gold ) ::
    Vec3 ( 0, -1/gold, -gold ) ::
    Vec3 ( 1/gold, gold, 0 ) ::
    Vec3 ( 1/gold, -gold, 0 ) ::
    Vec3 ( -1/gold, gold, 0 ) ::
    Vec3 ( -1/gold, -gold, 0) ::
    Vec3 ( gold, 0, 1/gold ) ::
    Vec3 ( gold, 0, -1/gold ) ::
    Vec3 ( -gold, 0, 1/gold ) ::
    Vec3 ( -gold, 0, -1/gold ) :: Nil

  val faces =
    Face( List( vertices( 12 ), vertices( 0 ), vertices( 16 ), vertices( 17 ), vertices( 1 ) ) ) :: 
    Face( List( vertices( 0 ), vertices( 8 ), vertices( 10 ), vertices( 2 ), vertices( 16 ) ) ) ::  
    Face( List( vertices( 14 ), vertices( 12 ), vertices( 1 ), vertices( 9 ), vertices( 5 ) ) ) ::  
    Face( List( vertices( 14 ), vertices( 5 ), vertices( 19 ), vertices( 18 ), vertices( 4 ) ) ) :: 
    Face( List( vertices( 18 ), vertices( 19 ), vertices( 7 ), vertices( 15 ), vertices( 6 ) ) ) :: 
    Face( List( vertices( 7 ), vertices( 11 ), vertices( 3 ), vertices( 13 ), vertices( 15 ) ) ) :: 
    Face( List( vertices( 6 ), vertices( 15 ), vertices( 13 ), vertices( 2 ), vertices( 10 ) ) ) :: 
    Face( List( vertices( 2 ), vertices( 13 ), vertices( 3 ), vertices( 17 ), vertices( 16 ) ) ) :: 
    Face( List( vertices( 7 ), vertices( 19 ), vertices( 5 ), vertices( 9 ), vertices( 11 ) ) ) ::  
    Face( List( vertices( 0 ), vertices( 12 ), vertices( 14 ), vertices( 4 ), vertices( 8 ) ) ) ::  
    Face( List( vertices( 8 ), vertices( 4 ), vertices( 18 ), vertices( 6 ), vertices( 10 ) ) ) ::  
    Face( List( vertices( 1 ), vertices( 17 ), vertices( 3 ), vertices( 11 ), vertices( 9 ) ) ) :: Nil
}
