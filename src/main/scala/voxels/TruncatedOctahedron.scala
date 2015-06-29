package voxels

import geometry.Vector3
import voxels.Voxel._

/**
 * Created by markus on 26/05/15.
 */
object TruncatedOctahedron extends VoxelStandard {
  val vertices: List[Vertex] =
    Vector3( 0, 0.5, 1 ) ::
    Vector3( -0.5, 0, 1 ) ::
    Vector3( 0, -0.5, 1 ) ::
    Vector3( 0.5, 0, 1 ) ::
    Vector3( 0.5, 0, -1 ) ::
    Vector3( 0, -0.5, -1 ) ::
    Vector3( -0.5, 0, -1 ) ::
    Vector3( 0, 0.5, -1 ) ::
    Vector3( 0, -1, 0.5 ) ::
    Vector3( -0.5, -1, 0 ) ::
    Vector3( 0, -1, -0.5 ) ::
    Vector3( 0.5, -1, 0 ) ::
    Vector3( 0, 1, 0.5 ) ::
    Vector3( 0.5, 1, 0 ) ::
    Vector3( 0, 1, -0.5 ) ::
    Vector3( -0.5, 1, 0 ) ::
    Vector3( 1, 0, -0.5 ) ::
    Vector3( 1, 0.5, 0 ) ::
    Vector3( 1, 0, 0.5 ) ::
    Vector3( 1, -0.5, 0 ) ::
    Vector3( -1, 0, 0.5 ) ::
    Vector3( -1, 0.5, 0 ) ::
    Vector3( -1, 0, -0.5 ) ::
    Vector3( -1, -0.5, 0 ) :: Nil

  val facesStructure =
    List(
      List( 0, 1, 2, 3 ),
      List( 4, 5, 6, 7 ),
      List( 12, 13, 14, 15 ),
      List( 8, 9, 10, 11 ),
      List( 16, 17, 18, 19 ),
      List( 20, 21, 22, 23 ),
      List( 0, 3, 18, 17, 13, 12 ),
      List( 1, 0, 12, 15, 21, 20 ),
      List( 2, 1, 20, 23, 9, 8 ),
      List( 3, 2, 8, 11, 19, 18 ),
      List( 7, 4, 16, 17, 13, 14 ).reverse,
      List( 6, 7, 14, 15, 21, 22 ).reverse,
      List( 5, 6, 22, 23, 9, 10 ).reverse,
      List( 4, 5, 10, 11, 19, 16 ).reverse
    ).map { list =>
      val l = list.length
      ( list, RegularPolygon( l ), if ( l == 6 ) List( 0, 1 ) else List( 0 ) )
    }

}
