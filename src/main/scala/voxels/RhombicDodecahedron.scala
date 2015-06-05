package voxels

import geometry.Vec3
import voxels.Voxel._

/**
 * Created by markus on 26/05/15.
 */
object RhombicDodecahedron extends VoxelStandard {

  override val scale = 1d

  private val a = math.sqrt( 3 ) / 3
  private val b = 2 * math.sqrt( 3 ) / 3

  val vertices: List[Vertex] =
    Vec3 ( a, a, a ) ::
    Vec3 ( a, a, -a ) ::
    Vec3 ( a, -a, a ) ::
    Vec3 ( a, -a, -a ) ::
    Vec3 ( -a, a, a ) ::
    Vec3 ( -a, a, -a ) ::
    Vec3 ( -a, -a, a ) ::
    Vec3 ( -a, -a, -a) ::
    Vec3 ( b, 0, 0 ) ::
    Vec3 ( -b, 0, 0 ) ::
    Vec3 ( 0, b, 0 ) ::
    Vec3 ( 0, -b, 0 ) ::
    Vec3 ( 0, 0, b ) ::
    Vec3 ( 0, 0, -b ) :: Nil

  val facesStructure =
    List(
      List( 0, 10, 4, 12 ),
      List( 0, 12, 2, 8 ),
      List( 2, 12, 6, 11 ),
      List( 4, 9, 6, 12 ),
      List( 0, 8, 1, 10 ),
      List( 4, 10, 5, 9 ),
      List( 6, 9, 7, 11 ),
      List( 2, 11, 3, 8 ),
      List( 1, 13, 5, 10 ),
      List( 1, 8, 3, 13 ),
      List( 3, 11, 7, 13 ),
      List( 5, 13, 7, 9 )
    ).map( ( _, StdRhombus ) )
}
