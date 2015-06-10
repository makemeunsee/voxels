package voxels

import geometry.Vec3
import voxels.Voxel._

/**
 * Created by markus on 26/05/15.
 */
object TrigonalTrapezohedron extends VoxelStandard {

  override val scale = 1d

  private val a = math.sqrt( 3 ) / 3
  private val b = math.sqrt( 5 ) / 6

  val vertices: List[Vertex] =
    Vec3( 0,0,-3*b ) ::
    Vec3( 0,2d/3,-b ) ::
    Vec3( a,-1d/3,-b ) ::
    Vec3( -a,-1d/3,-b ) ::
    Vec3( a,1d/3,b ) ::
    Vec3( -a,1d/3,b ) ::
    Vec3( 0,-2d/3,b ) ::
    Vec3( 0,0,3*b ) ::
    Nil

  val facesStructure =
    List(
      List( 3, 6, 2, 0 ),
      List( 1, 5, 3, 0 ),
      List( 2, 4, 1, 0 ),
      List( 4, 2, 6, 7 ),
      List( 5, 1, 4, 7 ),
      List( 6, 3, 5, 7 )
    ).map( l => ( l.reverse, StdRhombus, List( 0, 2 ) ) )
}
