package voxels

import geometry.Vec3
import voxels.Voxel._
import geometry.gold

/**
 * Created by markus on 03/01/20.
 */
object DodecahedronShard extends VoxelStandard {

  val vertices: List[Vertex] =
    Vec3( 1,0,-3/2-math.sqrt(5)/2 ) ::
    Vec3( 0,0,-3/2-math.sqrt(5)/2 ) ::
    Vec3( 3/4+math.sqrt(5)/4, 1/4 + math.sqrt(5)/4, -1-math.sqrt(5)/2 ) ::
    Vec3( 3/4+math.sqrt(5)/4, -1/4 - math.sqrt(5)/4, -1-math.sqrt(5)/2 ) ::
    Vec3( 0,1/2+3*math.sqrt(5)/10,-1-2*math.sqrt(5)/5 ) ::
    Vec3( 0,-1/2-3*math.sqrt(5)/10,-1-2*math.sqrt(5)/5 ) ::
    Vec3( 1+2*math.sqrt(5)/5, 0, -1/2-3*math.sqrt(5)/10 ) :: Nil

  val facesStructure =
    List(
      ( List( 0 , 1 , 4, 2 ), DDCSFace, List ( 0 ) ),
      ( List( 0 , 2 , 6, 3 ), DDCSFace, List ( 0 ) ),
      ( List( 0 , 3 , 5, 1 ), DDCSFace, List ( 0 ) ),
      ( List( 1 , 5 , 4 ), DDCSSide, List ( 0 ) ),
      ( List( 2 , 4 , 6 ), DDCSSide, List ( 0 ) ),
      ( List( 3 , 6 , 5 ), DDCSSide, List ( 0 ) ),
      ( List( 4 , 5 , 6 ), RegularPolygon( 3 ), List( 0 ) ) )
}
