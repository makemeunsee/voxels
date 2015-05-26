package voxels

import geometry.Vec3
import voxels.Voxel._

/**
 * Created by markus on 26/05/15.
 */
object Tetrahedron extends VoxelStandard {
  val faceCount = 4
  val verticesCount = 4

  val vertices: List[Vertex] =
    Vec3( 1,1,1 ) ::
    Vec3( -1,1,-1 ) ::
    Vec3( 1,-1,-1 ) ::
    Vec3( -1,-1,1 ) :: Nil

  val facesStructure =
    List( 0 , 1 , 3 ) ::
    List( 0 , 2 , 1 ) ::
    List( 0 , 3 , 2 ) ::
    List( 1 , 2 , 3 ) :: Nil
}
