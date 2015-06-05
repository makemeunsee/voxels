package voxels

import geometry.Vec3
import voxels.Voxel.{RegularPolygon, Vertex}

/**
 * Created by markus on 23/05/15.
 */
object Rhombicuboctahedron extends VoxelStandard {
  override val scale = 1d

  private val a = math.sqrt( 2 ) / 2

  val vertices: List[Vertex] =
    Vec3( 0.5+a,0.5,0.5 ) ::
    Vec3( 0.5,0.5+a,0.5 ) ::
    Vec3( 0.5,0.5,0.5+a ) ::
    Vec3( -0.5-a,0.5,0.5 ) ::
    Vec3( -0.5,0.5+a,0.5 ) ::
    Vec3( -0.5,0.5,0.5+a ) ::
    Vec3( -0.5-a,0.5,-0.5 ) ::
    Vec3( -0.5,0.5+a,-0.5 ) ::
    Vec3( -0.5,0.5,-0.5-a ) ::
    Vec3( 0.5+a,0.5,-0.5 ) ::
    Vec3( 0.5,0.5+a,-0.5 ) ::
    Vec3( 0.5,0.5,-0.5-a ) ::
    Vec3( -0.5-a,-0.5,-0.5 ) ::
    Vec3( -0.5,-0.5-a,-0.5 ) ::
    Vec3( -0.5,-0.5,-0.5-a ) ::
    Vec3( 0.5+a,-0.5,-0.5 ) ::
    Vec3( 0.5,-0.5-a,-0.5 ) ::
    Vec3( 0.5,-0.5,-0.5-a ) ::
    Vec3( 0.5+a,-0.5,0.5 ) ::
    Vec3( 0.5,-0.5-a,0.5 ) ::
    Vec3( 0.5,-0.5,0.5+a ) ::
    Vec3( -0.5-a,-0.5,0.5 ) ::
    Vec3( -0.5,-0.5-a,0.5 ) ::
    Vec3( -0.5,-0.5,0.5+a ) :: Nil

  val facesStructure =
    List(
      List( 0, 1, 2 ),
      List( 3, 5, 4 ),
      List( 6, 7, 8 ),
      List( 9, 11, 10 ),
      List( 12, 14, 13 ),
      List( 15, 16, 17 ),
      List( 18, 20, 19 ),
      List( 21, 22, 23 ),
      List( 1, 4, 5, 2 ),
      List( 3, 21, 23, 5 ),
      List( 19, 20, 23, 22 ),
      List( 0, 2, 20, 18 ),
      List( 2, 5, 23, 20 ),
      List( 8, 11, 17, 14 ),
      List( 7, 10, 11, 8 ),
      List( 9, 15, 17, 11 ),
      List( 13, 14, 17, 16 ),
      List( 6, 8, 14, 12 ),
      List( 0, 18, 15, 9 ),
      List( 3, 6, 12, 21 ),
      List( 1, 10, 7, 4 ),
      List( 13, 16, 19, 22 ),
      List( 0, 9, 10, 1 ),
      List( 3, 4, 7, 6 ),
      List( 15, 18, 19, 16 ),
      List( 12, 13, 22, 21 )
    ).map( l => ( l, RegularPolygon( l.length ) ) )
}
