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
      List( 10, 4, 12, 0 ),
      List( 12, 2, 8, 0 ),
      List( 12, 6, 11, 2 ),
      List( 12, 4, 9, 6 ),
      List( 10, 0, 8, 1 ),
      List( 10, 5, 9, 4 ),
      List( 11, 6, 9, 7 ),
      List( 11, 3, 8, 2 ),
      List( 10, 1, 13, 5 ),
      List( 13, 1, 8, 3 ),
      List( 11, 7, 13, 3 ),
      List( 13, 7, 9, 5 )
    ).map( ( _, StdRhombus, List( 0 ) ) )
}
