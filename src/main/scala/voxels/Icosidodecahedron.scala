package voxels

import geometry.Vector3
import voxels.Voxel.{RegularPolygon, Vertex}

/**
 * Created by markus on 12/06/2015.
 */
object Icosidodecahedron extends VoxelStandard {

  override val scale = 1d

  private val phi: Double = ( 1 + math.sqrt( 5 ) ) / 2

  val vertices: List[Vertex] =
      Vector3 ( 0, 0, phi ) ::
      Vector3 ( 1d/2, -phi/2, ( 1d+phi )/2 ) ::
      Vector3 ( 1d/2, phi/2, ( 1d+phi )/2 ) ::
      Vector3 ( -1d/2, phi/2, ( 1d+phi )/2 ) ::
      Vector3 ( -1d/2, -phi/2, ( 1d+phi )/2 ) ::
      Vector3 ( ( 1d+phi )/2, -1d/2, phi/2 ) ::
      Vector3 ( ( 1d+phi )/2, 1d/2, phi/2 ) ::
      Vector3 ( -( 1d+phi )/2, 1d/2, phi/2 ) ::
      Vector3 ( -( 1d+phi )/2, -1d/2, phi/2 ) ::
      Vector3 ( phi/2, -( 1d+phi )/2, 1d/2 ) ::
      Vector3 ( phi/2, ( 1d+phi )/2, 1d/2 ) ::
      Vector3 ( -phi/2, ( 1d+phi )/2, 1d/2 ) ::
      Vector3 ( -phi/2, -( 1d+phi )/2, 1d/2 ) ::
      Vector3 ( 0, -phi, 0 ) ::
      Vector3 ( phi, 0, 0 ) ::
      Vector3 ( 0, phi, 0 ) ::
      Vector3 ( -phi, 0, 0 ) ::
      Vector3 ( phi/2, -( 1d+phi )/2, -1d/2 ) ::
      Vector3 ( phi/2, ( 1d+phi )/2, -1d/2 ) ::
      Vector3 ( -phi/2, ( 1d+phi )/2, -1d/2 ) ::
      Vector3 ( -phi/2, -( 1d+phi )/2, -1d/2 ) ::
      Vector3 ( ( 1d+phi )/2, -1d/2, -phi/2 ) ::
      Vector3 ( ( 1d+phi )/2, 1d/2, -phi/2 ) ::
      Vector3 ( -( 1d+phi )/2, 1d/2, -phi/2 ) ::
      Vector3 ( -( 1d+phi )/2, -1d/2, -phi/2 ) ::
      Vector3 ( 1d/2, -phi/2, -( 1d+phi )/2 ) ::
      Vector3 ( 1d/2, phi/2, -( 1d+phi )/2 ) ::
      Vector3 ( -1d/2, phi/2, -( 1d+phi )/2 ) ::
      Vector3 ( -1d/2, -phi/2, -( 1d+phi )/2 ) ::
      Vector3 ( 0, 0, -phi ) :: Nil

  val facesStructure =
    List(
      List( 0, 1, 5, 6, 2 ),
      List( 0, 3, 7, 8, 4 ),
      List( 1, 4, 12, 13, 9 ),
      List( 2, 10, 15, 11, 3 ),
      List( 5, 9, 17, 21, 14 ),
      List( 6, 14, 22, 18, 10 ),
      List( 7, 11, 19, 23, 16 ),
      List( 8, 16, 24, 20, 12 ),
      List( 13, 20, 28, 25, 17 ),
      List( 15, 18, 26, 27, 19 ),
      List( 21, 25, 29, 26, 22 ),
      List( 23, 27, 29, 28, 24 ),
      List( 0, 4, 1 ),
      List( 0, 2, 3 ),
      List( 1, 9, 5 ),
      List( 2, 6, 10 ),
      List( 3, 11, 7 ),
      List( 4, 8, 12 ),
      List( 9, 13, 17 ),
      List( 10, 18, 15 ),
      List( 11, 15, 19 ),
      List( 12, 20, 13 ),
      List( 5, 14, 6 ),
      List( 7, 16, 8 ),
      List( 14, 21, 22 ),
      List( 16, 23, 24 ),
      List( 17, 25, 21 ),
      List( 18, 22, 26 ),
      List( 19, 27, 23 ),
      List( 20, 24, 28 ),
      List( 25, 28, 29 ),
      List( 26, 29, 27 )
    ).map { l => ( l, RegularPolygon( l.length ), List( 0 ) ) }
}
