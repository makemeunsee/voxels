package voxels

import geometry.Vec3
import voxels.Voxel.RegularPolygon
import voxels.Voxel.Vertex

/**
 * Created by markus on 23/05/15.
 */
object Cube extends VoxelStandard {

  val vertices: List[Vertex] =
    Vec3( 1,1,1 ) ::
    Vec3( -1,1,1 ) ::
    Vec3( -1,1,-1 ) ::
    Vec3( 1,1,-1 ) ::
    Vec3( -1,-1,-1 ) ::
    Vec3( 1,-1,-1 ) ::
    Vec3( 1,-1,1 ) ::
    Vec3( -1,-1,1 ) :: Nil

  val facesStructure =
    List(
      List( 0, 3, 2, 1 ),
      List( 4, 5, 6, 7 ),
      List( 0, 6, 5, 3 ),
      List( 1, 2, 4, 7 ),
      List( 0, 1, 7, 6 ),
      List( 2, 3, 5, 4 ) )
    .map( ( _, RegularPolygon( 4 ), 1 ) )
}
