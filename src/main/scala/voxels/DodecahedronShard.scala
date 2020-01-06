package voxels

import geometry.Vec3
import voxels.Voxel._
import geometry.gold

/**
 * Created by markus on 03/01/20.
 */
object DodecahedronShard extends VoxelStandard {

  val A = Vec3( -1/gold,0,-gold ) // 0
  val B = Vec3( 1/gold,0,-gold ) // 1
  val C= Vec3( 1,1,-1 ) // 2
  val Cp = Vec3( -1,1,-1 ) // 3
  val D = Vec3( 1,-1,-1 ) // 4
  val Dp = Vec3( -1,-1,-1 ) // 5
  val E = Vec3( gold,1/gold,0 ) // 6
  val F = Vec3( gold,-1/gold,0 ) // 7
  val G = Vec3( 0,gold,-1/gold ) // 8
  val H = Vec3( 0,-gold,-1/gold ) // 9
  val I = ( A + B ) / 2 // 10
  val J = ( C + B ) / 2 // 11
  val K = ( B + D ) / 2 // 12
  val M = ( A + B + C + Cp + G ) / 5 // 13
  val N = ( A + B + D + Dp + H ) / 5 // 14
  val O = ( B + C + D + E + F ) / 5 // 15
  val vertices: List[Vertex] = A :: B :: C :: Cp :: D :: Dp :: E :: F :: G :: H ::
    I :: J :: K :: M :: N :: O ::
    Nil

  val facesStructure =
    List(
      ( List(13,14,15), RegularPolygon( 3 ), List( 0 ) ), // MNO
      ( List( 1 , 10 , 13, 11 ), DDCSFace, List ( 0 ) ), // BIMJ
      ( List( 1 , 11 , 15, 12 ), DDCSFace, List ( 0 ) ), // BJOK
      ( List( 1 , 12 , 14, 10 ), DDCSFace, List ( 0 ) ), // BKNI
      ( List( 10 , 14 , 13 ), DDCSSide, List ( 0 ) ),    // INM
      ( List( 11 , 13 , 15 ), DDCSSide, List ( 0 ) ),    // JMO
      ( List( 12 , 15 , 14 ), DDCSSide, List ( 0 ) )    // KON
    )
}
