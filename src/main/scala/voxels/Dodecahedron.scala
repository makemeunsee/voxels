package voxels

import geometry.Vec3
import geometry.gold
import voxels.Voxel._

/**
 * Created by markus on 26/05/15.
 */
object Dodecahedron extends VoxelStandard {
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

  val facesStructure =
    List( 12, 0, 16, 17, 1 ) ::
    List( 0, 8, 10, 2, 16 ) ::
    List( 14, 12, 1, 9, 5 ) ::
    List( 14, 5, 19, 18, 4 ) ::
    List( 18, 19, 7, 15, 6 ) ::
    List( 7, 11, 3, 13, 15 ) ::
    List( 6, 15, 13, 2, 10 ) ::
    List( 2, 13, 3, 17, 16 ) ::
    List( 7, 19, 5, 9, 11 ) ::
    List( 0, 12, 14, 4, 8 ) ::
    List( 8, 4, 18, 6, 10 ) ::
    List( 1, 17, 3, 11, 9 ) :: Nil
}
