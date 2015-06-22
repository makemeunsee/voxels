package voxels

import geometry.Vec3
import voxels.Voxel._

/**
  * Created by markus on 22/06/2015.
  */
object PentagonalPyramid extends VoxelStandard {

  private val c1 = math.sqrt( 5 )/4 - 0.25
  private val c2 = math.sqrt( 5 )/4 + 0.25
  private val s1 = math.sqrt( 10 + 2 * math.sqrt( 5 ) ) / 4
  private val s2 = math.sqrt( 10 - 2 * math.sqrt( 5 ) ) / 4
  private val h = math.sqrt( 0.25 * ( 10 - 2 * math.sqrt( 5 ) ) - 1 )

   val vertices: List[Vertex] =
     Vec3( 1,0,0 ) ::
     Vec3( c1,0,s1 ) ::
     Vec3( -c2,0,s2 ) ::
     Vec3( -c2,0,-s2 ) ::
     Vec3( c1,0,-s1 ) ::
     Vec3( 0,h,0 ) ::
     Nil

   val facesStructure =
     List(
       List( 0 , 5 , 1 ),
       List( 1 , 5 , 2 ),
       List( 2 , 5 , 3 ),
       List( 3 , 5 , 4 ),
       List( 4 , 5 , 0 ),
       List( 0, 1, 2, 3, 4 )
     ).map { ls =>
       val l = ls.length
       ( ls, RegularPolygon( l ), if ( l == 3 ) List( 0, 1, 2 ) else List( 0 ) )
     }
 }

