package voxels

import geometry.Vec3
import voxels.Voxel._

/**
 * Created by markus on 22/06/2015.
 */
object SquarePyramid extends VoxelStandard {

  override val scale = 1d

  val vertices: List[Vertex] =
    Vec3( 0.5,0,0.5 ) ::
    Vec3( -0.5,0,0.5 ) ::
    Vec3( -0.5,0,-0.5 ) ::
    Vec3( 0.5,0,-0.5 ) ::
    Vec3( 0,math.sqrt( 2 )/2,0 ) :: Nil

  val facesStructure =
    List(
      List( 0, 4, 1 ),
      List( 1, 4, 2 ),
      List( 2, 4, 3 ),
      List( 3, 4, 0 ),
      List( 0, 1, 2, 3 )
    ).map { ls =>
      val l = ls.length
      ( ls, RegularPolygon( l ), if ( l == 3 ) List( 0, 1, 2 ) else List( 0 ) )
    }
}

