package voxels

import geometry.Vec3
import geometry.gold
import voxels.Voxel._

/**
 * Created by markus on 06/01/20.
 */
object TruncatedDodecahedron extends VoxelStandard {

  val vertices: List[Vertex] =
    Vec3 (0, 1/gold, (2+gold)) :: // A - 0
    Vec3 (0, -1/gold, (2+gold)) :: // B - 1
    Vec3 (1/gold, gold, 2*gold) :: // C - 2
    Vec3 (-1/gold, gold, 2*gold) :: // D - 3
    Vec3 (1/gold, -gold, 2*gold) :: // E - 4
    Vec3 (-1/gold, -gold, 2*gold) :: // F - 5
    Vec3 (gold, 2, gold*gold) :: // G - 6
    Vec3 (-gold, 2, gold*gold) :: // H - 7
    Vec3 (gold, -2, gold*gold) :: // I - 8
    Vec3 (-gold, -2, gold*gold) :: // J - 9
    Vec3 (gold*gold, gold, 2) :: // K - 10
    Vec3 (-gold*gold, gold, 2) :: // L - 11
    Vec3 (gold*gold, -gold, 2) :: // M - 12
    Vec3 (-gold*gold, -gold, 2) :: // N - 13
    Vec3 (2*gold, 1/gold, gold) :: // O - 14
    Vec3 (-2*gold, 1/gold, gold) :: // P - 15
    Vec3 (2*gold, -1/gold, gold) :: // Q - 16
    Vec3 (-2*gold, -1/gold, gold) :: // R - 17
    Vec3 (2, gold*gold, gold) :: // S - 18
    Vec3 (-2, gold*gold, gold) :: // T - 19
    Vec3 (2, -gold*gold, gold) :: // U - 20
    Vec3 (-2, -gold*gold, gold) :: // V - 21
    Vec3 ((2+gold), 0, 1/gold) :: // W - 22
    Vec3 (-(2+gold), 0, 1/gold) :: // X - 23
    Vec3 (gold, 2*gold, 1/gold) :: // Y - 24
    Vec3 (-gold, 2*gold, 1/gold) :: // Z - 25
    Vec3 (gold, -2*gold, 1/gold) :: // AA - 26
    Vec3 (-gold, -2*gold, 1/gold) :: // AB - 27
    Vec3 (1/gold, (2+gold), 0) :: // AC - 28
    Vec3 (-1/gold, (2+gold), 0) :: // AD - 29
    Vec3 (1/gold, -(2+gold), 0) :: // AE - 30
    Vec3 (-1/gold, -(2+gold), 0) :: // AF - 31
    Vec3 (gold, 2*gold, -1/gold) :: // Y' - 32
    Vec3 (-gold, 2*gold, -1/gold) :: // Z' - 33
    Vec3 (gold, -2*gold, -1/gold) :: // AA' - 34
    Vec3 (-gold, -2*gold, -1/gold) :: // AB' - 35
    Vec3 ((2+gold), 0, -1/gold) :: // W' - 36
    Vec3 (-(2+gold), 0, -1/gold) :: // X' - 37
    Vec3 (2, gold*gold, -gold) :: // S' - 38
    Vec3 (-2, gold*gold, -gold) :: // T' - 39
    Vec3 (2, -gold*gold, -gold) :: // U' - 40
    Vec3 (-2, -gold*gold, -gold) :: // V' - 41
    Vec3 (2*gold, 1/gold, -gold) :: // O' - 42
    Vec3 (-2*gold, 1/gold, -gold) :: // P' - 43
    Vec3 (2*gold, -1/gold, -gold) :: // Q' - 44
    Vec3 (-2*gold, -1/gold, -gold) :: // R' - 45
    Vec3 (gold*gold, gold, -2) :: // K' - 46
    Vec3 (-gold*gold, gold, -2) :: // L' - 47
    Vec3 (gold*gold, -gold, -2) :: // M' - 48
    Vec3 (-gold*gold, -gold, -2) :: // N' - 49
    Vec3 (gold, 2, -gold*gold) :: // G' - 50
    Vec3 (-gold, 2, -gold*gold) :: // H' - 51
    Vec3 (gold, -2, -gold*gold) :: // I' - 52
    Vec3 (-gold, -2, -gold*gold) :: // J' - 53
    Vec3 (1/gold, gold, -2*gold) :: // C' - 54
    Vec3 (-1/gold, gold, -2*gold) :: // D' - 55
    Vec3 (1/gold, -gold, -2*gold) :: // E' - 56
    Vec3 (-1/gold, -gold, -2*gold) :: // F' - 57
    Vec3 (0, 1/gold, -(2+gold)) :: // A' - 58
    Vec3 (0, -1/gold, -(2+gold)) :: // B' - 59
    Nil

  val facesStructure =
    List(
      List( 0,2,3 ),
      List( 1,5,4 ),
      List( 6,10,18 ),
      List( 7,19,11 ),
      List( 8,20,12 ),
      List( 9,13,21 ),
      List( 14,16,22 ),
      List( 15,23,17 ),
      List( 24,32,28 ),
      List( 25,29,33 ),
      List( 26,30,34 ),
      List( 27,35,31 )
    ).map( ( _, RegularPolygon( 3 ), List( 0 ) ) )
}