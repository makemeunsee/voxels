package voxels

import geometry.Vec3
import voxels.Voxel.RegularPolygon

/**
 * Created by markus on 29/05/2015.
 */
case class RegularPrism( sides: Int ) extends VoxelStandard {
  assert( sides >= 3 )

  override val scale = {
    val x1 = math.cos( 2 * math.Pi / sides )
    val y1 = math.sin( 2 * math.Pi / sides )
    math.sqrt( ( 1 - x1 ) * ( 1 - x1 ) + y1 * y1 )
  }

  val vertices = ( 0 until sides ).flatMap { i =>
    val x = math.cos( i * 2 * math.Pi / sides )
    val y = math.sin( i * 2 * math.Pi / sides )
    Vec3( x, y, scale / 2 ) :: Vec3( x, y, -scale / 2 ) :: Nil
  }.toList

  val facesStructure = {
    val l = vertices.size
    ( ( 0 until l by 2 ).toList, RegularPolygon( l / 2 ), List( 0 ) ) ::
    ( ( 1 until l by 2 ).reverse.toList, RegularPolygon( l / 2 ), List( 0 ) ) ::
    Nil ++
    ( 0 until l / 2 ).map { i =>
      ( List( 2 * i, 2 * i + 1, ( 2 * i + 3 ) % l, ( 2 * i + 2 ) % l ), RegularPolygon( 4 ), List( 0, 1 ) )
    }
  }

  override def name = s"$sides-sided prism"
}

