package voxels

import geometry.Vec3
import voxels.Voxel.Vertex

/**
 * Created by markus on 23/05/15.
 */
object Cuboctahedron extends VoxelStandard {
  val faceCount = 14
  val verticesCount = 12

  override val scale = 1d

  private val a = math.sqrt( 2 ) / 2

  val vertices: List[Vertex] =
    Vec3( a,a,0 ) ::
    Vec3( -a,a,0 ) ::
    Vec3( -a,-a,0 ) ::
    Vec3( a,-a,0 ) ::
    Vec3( a,0,a ) ::
    Vec3( -a,0,a ) ::
    Vec3( -a,0,-a ) ::
    Vec3( a,0,-a ) ::
    Vec3( 0,a,a ) ::
    Vec3( 0,-a,a ) ::
    Vec3( 0,-a,-a ) ::
    Vec3( 0,a,-a ) :: Nil

  val facesStructure =
    List( 0, 8, 4 ) ::
    List( 1, 5, 8 ) ::
    List( 1, 11, 6 ) ::
    List( 0, 7, 11 ) ::
    List( 3, 4, 9 ) ::
    List( 2, 9, 5 ) ::
    List( 2, 6, 10 ) ::
    List( 3, 10, 7 ) ::
    List( 0, 11, 1, 8 ) ::
    List( 2, 10, 3, 9 ) ::
    List( 0, 4, 3, 7 ) ::
    List( 8, 5, 9, 4 ) ::
    List( 1, 6, 2, 5 ) ::
    List( 6, 11, 7, 10 ) :: Nil
}
