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
    Vec3( 0,-2d/3,b ) ::
    Vec3( -a,1d/3,b ) ::
    Vec3( 0,0,3*b ) ::
    Nil

  val facesStructure =
    List( 0, 3, 5, 2 ) ::
    List( 0, 1, 6, 3 ) ::
    List( 0, 2, 4, 1 ) ::
    List( 2, 5, 7, 4 ) ::
    List( 1, 4, 7, 6 ) ::
    List( 3, 6, 7, 5 ) ::
    Nil
}
