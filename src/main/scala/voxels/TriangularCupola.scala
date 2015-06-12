package voxels

import geometry.Vec3
import voxels.Voxel.{RegularPolygon, Vertex}

/**
 * Created by markus on 23/05/15.
 */
object TriangularCupola extends VoxelStandard {

  override val scale = 1d

  private val a = math.sqrt( 6 ) / 3
  private val b = math.sqrt( 3 ) / 2
  private val c = math.sqrt( 3 ) / 3
  private val d = math.sqrt( 3 ) / 6

  val vertices: List[Vertex] =
    Vec3( 1,0,0 ) ::
    Vec3( 0.5,b,0 ) ::
    Vec3( -0.5,b,0 ) ::
    Vec3( -1,0,0 ) ::
    Vec3( -0.5,-b,0 ) ::
    Vec3( 0.5,-b,0 ) ::
    Vec3( 0.5,d,a ) ::
    Vec3( -0.5,d,a ) ::
    Vec3( 0,-c,a ) ::
    Nil

  val facesStructure =
    List(
      ( List( 6, 7, 8 ), List( 0 ) ),
      ( List( 0, 1, 6 ), List( 0, 1, 2 ) ),
      ( List( 2, 3, 7 ), List( 0, 1, 2 ) ),
      ( List( 4, 5, 8 ), List( 0, 1, 2 ) ),
      ( List( 0, 6, 8, 5 ), List( 0, 1, 2, 3 ) ),
      ( List( 1, 2, 7, 6 ), List( 0, 1, 2, 3 ) ),
      ( List( 3, 4, 8, 7 ), List( 0, 1, 2, 3 ) ),
      ( List( 0, 5, 4, 3, 2, 1 ), List( 0, 1 ) )
    ).map { case ( l, rots ) => ( l, RegularPolygon( l.length ), rots ) }
}
