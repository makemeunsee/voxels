package voxels

import geometry.Vec3
import geometry.gold
import voxels.Voxel.{RegularPolygon, Vertex}

/**
 * Created by markus on 26/05/15.
 */
object Icosahedron extends VoxelStandard {

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

  val facesStructure =
    List(
      List( 0, 1, 8 ),
      List( 0, 11, 1 ),
      List( 2, 3, 9 ),
      List( 2, 10, 3 ),
      List( 4, 5, 0 ),
      List( 4, 3, 5 ),
      List( 6, 7, 1 ),
      List( 6, 2, 7 ),
      List( 8, 9, 4 ),
      List( 8, 7, 9 ),
      List( 10, 11, 5 ),
      List( 10, 6, 11 ),
      List( 0, 8, 4 ),
      List( 0, 5, 11 ),
      List( 1, 7, 8 ),
      List( 1, 11, 6 ),
      List( 2, 9, 7 ),
      List( 2, 6, 10 ),
      List( 3, 4, 9 ),
      List( 3, 10, 5 )
    ).map( ( _, RegularPolygon( 3 ), 3 ) )
}
