package voxels

import geometry.Vec3
import voxels.Voxel.Vertex

/**
 * Created by markus on 23/05/15.
 */
object TruncatedCube extends VoxelStandard {

  override val scale = 1d

  private val a = 0.5 + math.sqrt( 2 ) / 2

  val vertices: List[Vertex] =
    Vec3( a,a,0.5 ) ::
    Vec3( 0.5,a,a ) ::
    Vec3( -0.5,a,a ) ::
    Vec3( -a,a,0.5 ) ::
    Vec3( -a,a,-0.5 ) ::
    Vec3( -0.5,a,-a ) ::
    Vec3( 0.5,a,-a ) ::
    Vec3( a,a,-0.5 ) ::
    Vec3( a,0.5,a ) ::
    Vec3( -a,0.5,a ) ::
    Vec3( -a,0.5,-a ) ::
    Vec3( a,0.5,-a ) ::
    Vec3( a,-0.5,a ) ::
    Vec3( -a,-0.5,a ) ::
    Vec3( -a,-0.5,-a ) ::
    Vec3( a,-0.5,-a ) ::
    Vec3( a,-a,0.5 ) ::
    Vec3( 0.5,-a,a ) ::
    Vec3( -0.5,-a,a ) ::
    Vec3( -a,-a,0.5 ) ::
    Vec3( -a,-a,-0.5 ) ::
    Vec3( -0.5,-a,-a ) ::
    Vec3( 0.5,-a,-a ) ::
    Vec3( a,-a,-0.5 ) :: Nil

  val facesStructure =
    List( 1, 0, 8 ) ::
    List( 3, 2, 9 ) ::
    List( 5, 4, 10 ) ::
    List( 7, 6, 11 ) ::
    List( 17, 16, 12 ) ::
    List( 19, 18, 13 ) ::
    List( 21, 20, 14 ) ::
    List( 23, 22, 15 ) ::
    List( 0, 1, 2, 3, 4, 5, 6, 7 ) ::
    List( 23, 22, 21, 20, 19, 18, 17, 16 ) ::
    List( 0, 7, 11, 15, 23, 16, 12, 8 ) ::
    List( 2, 1, 8, 12, 17, 18, 13, 9 ) ::
    List( 4, 3, 9, 13, 19, 20, 14, 10 ) ::
    List( 6, 5, 10, 14, 21, 22, 15, 11 ) ::
    Nil
}