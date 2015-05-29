package voxels

import geometry.Vec3

/**
 * Created by markus on 29/05/2015.
 */
object RhombicPrism extends VoxelStandard {
  override val scale = 1d

  private val a = math.sqrt( 3 ) / 3
  private val b = math.sqrt( 2 )

  val vertices =
    Vec3( b*a, 0, 0.5 ) ::
    Vec3( 0, a, 0.5 ) ::
    Vec3( -b*a, 0, 0.5 ) ::
    Vec3( 0, -a, 0.5 ) ::
    Vec3( b*a, 0, -0.5 ) ::
    Vec3( 0, a, -0.5 ) ::
    Vec3( -b*a, 0, -0.5 ) ::
    Vec3( 0, -a, -0.5 ) ::
    Nil

  val facesStructure =
    List( 0, 1, 2, 3 ) ::
    List( 7, 6, 5, 4 ) ::
    List( 0, 4, 5, 1) ::
    List( 1, 5, 6, 2) ::
    List( 2, 6, 7, 3) ::
    List( 3, 7, 4, 0) ::
    Nil
}

