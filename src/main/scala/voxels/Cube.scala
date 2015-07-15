package voxels

import geometry.Vector3
import voxels.Voxel.RegularPolygon
import voxels.Voxel.Vertex

/**
 * Created by markus on 23/05/15.
 */
object Cube extends VoxelStandard {

  val vertices: Seq[Vertex] =
    Vector3( 1,1,1 ) ::
    Vector3( -1,1,1 ) ::
    Vector3( -1,1,-1 ) ::
    Vector3( 1,1,-1 ) ::
    Vector3( -1,-1,-1 ) ::
    Vector3( 1,-1,-1 ) ::
    Vector3( 1,-1,1 ) ::
    Vector3( -1,-1,1 ) :: Nil

  val facesStructure =
    Seq(
      Seq( 0, 3, 2, 1 ),
      Seq( 4, 5, 6, 7 ),
      Seq( 0, 6, 5, 3 ),
      Seq( 1, 2, 4, 7 ),
      Seq( 0, 1, 7, 6 ),
      Seq( 2, 3, 5, 4 ) )
    .map( ( _, RegularPolygon( 4 ), Seq( 0 ) ) )
}
