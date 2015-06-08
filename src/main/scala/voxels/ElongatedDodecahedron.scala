package voxels

import geometry.Vec3
import voxels.Voxel._

/**
 * Created by markus on 26/05/15.
 */
object ElongatedDodecahedron extends VoxelStandard {

  override val scale = 1d

  private val a = math.sqrt( 3 ) / 3
  private val b = 2 * math.sqrt( 3 ) / 3

  val vertices: List[Vertex] =
    Vec3 ( a, a+0.5, a ) ::
    Vec3 ( a, a+0.5, -a ) ::
    Vec3 ( a, -a-0.5, a ) ::
    Vec3 ( a, -a-0.5, -a ) ::
    Vec3 ( -a, a+0.5, a ) ::
    Vec3 ( -a, a+0.5, -a ) ::
    Vec3 ( -a, -a-0.5, a ) ::
    Vec3 ( -a, -a-0.5, -a) ::
    Vec3 ( b, 0.5, 0 ) ::
    Vec3 ( -b, 0.5, 0 ) ::
    Vec3 ( 0, 0.5, b ) ::
    Vec3 ( 0, 0.5, -b ) ::
    Vec3 ( b, -0.5, 0 ) ::
    Vec3 ( -b, -0.5, 0 ) ::
    Vec3 ( 0, -0.5, b ) ::
    Vec3 ( 0, -0.5, -b ) ::
    Vec3 ( 0, b+0.5, 0 ) ::
    Vec3 ( 0, -b-0.5, 0 ) :: Nil

  val facesStructure =
    List(
      ( List( 0, 16, 4, 10 ), ??? ),
      ( List( 0, 8, 1, 16 ), ??? ),
      ( List( 1, 11, 5, 16 ), ??? ),
      ( List( 4, 16, 5, 9 ), ??? ),
      ( List( 2, 14, 6, 17 ), ??? ),
      ( List( 2, 17, 3, 12 ), ??? ),
      ( List( 3, 17, 7, 15 ), ??? ),
      ( List( 6, 17, 7, 13 ), ??? ),
      ( List( 0, 10, 14, 2, 12, 8 ), RegularPolygon( 6, ??? ) ),
      ( List( 4, 9, 13, 6, 14, 10 ), RegularPolygon( 6, ??? ) ),
      ( List( 5, 11, 15, 7, 13, 9 ), RegularPolygon( 6, ??? ) ),
      ( List( 1, 8, 12, 3, 15, 11 ), RegularPolygon( 6, ??? ) )
    )
}
