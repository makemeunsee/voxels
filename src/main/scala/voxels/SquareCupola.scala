package voxels

import geometry.Vector3
import voxels.Voxel.{RegularPolygon, Vertex}

/**
 * Created by markus on 23/05/15.
 */
object SquareCupola extends VoxelStandard {
  override val scale = 1d

  private val a = math.sqrt( 2 ) / 2

  val vertices: List[Vertex] =
    Vector3( 0.5+a,0.5,0 ) ::
    Vector3( 0.5,0.5+a,0 ) ::
    Vector3( -0.5,0.5+a,0 ) ::
    Vector3( -0.5-a,0.5,0 ) ::
    Vector3( -0.5-a,-0.5,0 ) ::
    Vector3( -0.5,-0.5-a,0 ) ::
    Vector3( 0.5,-0.5-a,0 ) ::
    Vector3( 0.5+a,-0.5,0 ) ::
    Vector3( 0.5,0.5,a ) ::
    Vector3( -0.5,0.5,a ) ::
    Vector3( -0.5,-0.5,a ) ::
    Vector3( 0.5,-0.5,a ) ::
    Nil

  val facesStructure =
    List(
      ( List( 0, 1, 8 ), List( 0, 1, 2 ) ),
      ( List( 2, 3, 9 ), List( 0, 1, 2 ) ),
      ( List( 4, 5, 10 ), List( 0, 1, 2 ) ),
      ( List( 6, 7, 11 ), List( 0, 1, 2 ) ),
      ( List( 0, 8, 11, 7 ), List( 0, 1, 2, 3 ) ),
      ( List( 1, 2, 9, 8 ), List( 0, 1, 2, 3 ) ),
      ( List( 3, 4, 10, 9 ), List( 0, 1, 2, 3 ) ),
      ( List( 5, 6, 11, 10 ), List( 0, 1, 2, 3 ) ),
      ( List( 8, 9, 10, 11 ), List( 0 ) ),
      ( List( 0, 7, 6, 5, 4, 3, 2, 1 ), List( 0, 1 ) )
    ).map { case ( l, rots ) => ( l, RegularPolygon( l.length ), rots ) }
}
