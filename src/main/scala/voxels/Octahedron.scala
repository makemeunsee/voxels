package voxels

import geometry.Vec3
import voxels.Voxel._

/**
 * Created by markus on 26/05/15.
 */
object Octahedron extends VoxelStandard {

  val vertices: List[Vertex] =
    Vec3( 0,1,0 ) ::
    Vec3( 1,0,0 ) ::
    Vec3( 0,0,1 ) ::
    Vec3( -1,0,0 ) ::
    Vec3( 0,0,-1 ) ::
    Vec3( 0,-1,0 ) :: Nil

  val facesStructure =
    List(
      List( 0, 2, 1 ),
      List( 0, 3, 2 ),
      List( 0, 4, 3 ),
      List( 0, 1, 4 ),
      List( 5, 1, 2 ),
      List( 5, 2, 3 ),
      List( 5, 3, 4 ),
      List( 5, 4, 1 )
    ).map( ( _, RegularPolygon( 3, 1 ) ) )

}
