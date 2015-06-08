package voxels

import voxels.Voxel.RegularPolygon

/**
 * Created by markus on 26/05/15.
 */
object TruncatedTetrahedron extends VoxelStandard {
  override val scale = 1d

  private def computeVertices = {
    val tetraVerts = Tetrahedron.vertices map ( _*1.5 )
    for( v0 <- tetraVerts;
         v1 <- tetraVerts if v0 != v1 )
      yield v0 + ( ( v1 - v0 ) / 3 )
  }
  val vertices = computeVertices

  val facesStructure =
    List(
      List( 0, 1, 6, 7, 4, 3 ),
      List( 1, 2, 9, 11, 8, 6 ),
      List( 5, 4, 7, 8, 11, 10 ),
      List( 2, 0, 3, 5, 10, 9 ),
      List( 0, 2, 1 ),
      List( 3, 4, 5 ),
      List( 6, 8, 7 ),
      List( 9, 10, 11 )
    ).map( l => ( l, RegularPolygon( l.length ) ) )
}
