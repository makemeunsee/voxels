package voxels

import geometry.Vec3
import geometry.gold
import voxels.Voxel._

/**
 * Created by markus on 06/01/20.
 */
object Rhombicosidodecahedron extends VoxelStandard {

  val vertices: List[Vertex] =
    Vec3 (1, 1, gold*gold*gold) ::
    Vec3 (-1, 1, gold*gold*gold) ::
    Vec3 (1, -1, gold*gold*gold) ::
    Vec3 (-1, -1, gold*gold*gold) ::
    Vec3 (0, gold*gold, (2+gold)) ::
    Vec3 (0, -gold*gold, (2+gold)) ::
    Vec3 (gold*gold, gold, 2*gold) ::
    Vec3 (-gold*gold, gold, 2*gold) ::
    Vec3 (gold*gold, -gold, 2*gold) ::
    Vec3 (-gold*gold, -gold, 2*gold) ::
    Vec3 (gold, 2*gold, gold*gold) ::
    Vec3 (-gold, 2*gold, gold*gold) ::
    Vec3 (gold, -2*gold, gold*gold) ::
    Vec3 (-gold, -2*gold, gold*gold) ::
    Vec3 ((2+gold), 0, gold*gold) ::
    Vec3 (-(2+gold), 0, gold*gold) ::
    Vec3 (2*gold, gold*gold, gold) ::
    Vec3 (-2*gold, gold*gold, gold) ::
    Vec3 (2*gold, -gold*gold, gold) ::
    Vec3 (-2*gold, -gold*gold, gold) ::
    Vec3 (gold*gold*gold, 1, 1) ::
    Vec3 (-gold*gold*gold, 1, 1) ::
    Vec3 (gold*gold*gold, -1, 1) ::
    Vec3 (-gold*gold*gold, -1, 1) ::
    Vec3 (1, gold*gold*gold, 1) ::
    Vec3 (-1, gold*gold*gold, 1) ::
    Vec3 (1, -gold*gold*gold, 1) ::
    Vec3 (-1, -gold*gold*gold, 1) ::
    Vec3 (gold*gold, (2+gold), 0) ::
    Vec3 (-gold*gold, (2+gold), 0) ::
    Vec3 (gold*gold, -(2+gold), 0) :: // 30
    Vec3 (-gold*gold, -(2+gold), 0) :: // 31
    Vec3 (1, gold*gold*gold, -1) ::
    Vec3 (-1, gold*gold*gold, -1) ::
    Vec3 (1, -gold*gold*gold, -1) ::
    Vec3 (-1, -gold*gold*gold, -1) ::
    Vec3 (gold*gold*gold, 1, -1) :: // 20' = 36
    Vec3 (-gold*gold*gold, 1, -1) :: // 21' = 37
    Vec3 (gold*gold*gold, -1, -1) :: // 22' = 38
    Vec3 (-gold*gold*gold, -1, -1) :: // 23' = 39
    Vec3 (2*gold, gold*gold, -gold) :: // 16' = 40
    Vec3 (-2*gold, gold*gold, -gold) :: // 17' = 41
    Vec3 (2*gold, -gold*gold, -gold) :: // 18' = 42
    Vec3 (-2*gold, -gold*gold, -gold) :: // 19' = 43
    Vec3 ((2+gold), 0, -gold*gold) :: // 14' = 44
    Vec3 (-(2+gold), 0, -gold*gold) :: // 15' = 45
    Vec3 (gold, 2*gold, -gold*gold) :: // 10' = 46
    Vec3 (-gold, 2*gold, -gold*gold) :: // 11' = 47
    Vec3 (gold, -2*gold, -gold*gold) :: // 12' = 48
    Vec3 (-gold, -2*gold, -gold*gold) :: // 13' = 49
    Vec3 (gold*gold, gold, -2*gold) :: // 6' = 50
    Vec3 (-gold*gold, gold, -2*gold) :: // 7' = 51
    Vec3 (gold*gold, -gold, -2*gold) :: // 8' = 52
    Vec3 (-gold*gold, -gold, -2*gold) :: // 9' = 53
    Vec3 (0, gold*gold, -(2+gold)) :: // 4' = 54
    Vec3 (0, -gold*gold, -(2+gold)) :: // 5' = 55
    Vec3 (1, 1, -gold*gold*gold) :: // 0' = 56
    Vec3 (-1, 1, -gold*gold*gold) :: // 1' = 57
    Vec3 (1, -1, -gold*gold*gold) :: // 2' = 58
    Vec3 (-1, -1, -gold*gold*gold) :: // 3' = 59
    Nil

  val facesStructure =
    List(
      List( 0,4,1 ),
      List( 2,3,5 ),
      List( 6,16,10 ),
      List( 7,11,17 ),
      List( 8,12,18 ),
      List( 9,19,13 ),
      List( 14,22,20 ),
      List( 15,21,23 ),
      List( 24,28,32 ),
      List( 25,33,29 ),
      List( 26,34,30 ),
      List( 27,31,35 ),
      List( 56, 57, 54 ),
      List( 58, 55, 59 ),
      List( 50, 46, 40 ),
      List( 51, 41, 47 ),
      List( 52, 42, 48 ),
      List( 53, 49, 43 ),
      List( 44, 36, 38 ),
      List( 45, 39, 37 )
    ).map( ( _, RegularPolygon( 3 ), List( 0 ) ) )
}