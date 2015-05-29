package voxels

import geometry.{Vec3, gold}
import voxels.Voxel.Vertex

import scala.annotation.tailrec

/**
 * Created by markus on 26/05/15.
 */
object TruncatedIcosahedron extends VoxelStandard {
  def newVs = Icosahedron.facesStructure
    .foldLeft( Map.empty[( Int, Int ), Vertex] ) { case ( m, i0::i1::i2::Nil) =>
      val ( v0, v1, v2 ) = ( Icosahedron.vertices( i0 ), Icosahedron.vertices( i1 ), Icosahedron.vertices( i2 ) )
      m + ( ( ( i0, i1 ), v0 + ( v1 - v0 ) / 3 ), ( ( i1, i2 ), v1 + ( v2 - v1 ) / 3 ), ( ( i2, i0 ), v2 + ( v0 - v2 ) / 3 ) )
    }.toList.unzip

  val vertices = newVs._2

  val facesStructure = {
    val oldToNew = newVs._1.zipWithIndex.toMap
    val hexas = Icosahedron.facesStructure
      .map { case i0::i1::i2::Nil =>
        ( ( i0, i1 )::( i1, i0 )::( i1, i2 )::( i2, i1 )::( i2, i0 )::( i0, i2 )::Nil ).map( oldToNew )
      }
    val edges = hexas
      .foldLeft( Set.empty[( Int, Int )]) { case ( acc, i0::i1::i2::i3::i4::i5::Nil ) =>
        val ns = Set( ( i0, i1 ), ( i1, i2 ), ( i2, i3 ), ( i3, i4 ), ( i4, i5 ), ( i5, i0 ) )
        ns.foldLeft( acc ) { case ( s, ( i, j ) ) =>
          if ( s( j, i ) )
            s - ( ( j, i ) )
          else
            s + ( ( i, j ) )
        }
      }.toMap
    val pentas = oldToNew.values
      .map { i =>
        i::edges( i )::edges( edges( i ) )::edges( edges( edges( i ) ) )::edges( edges( edges( edges( i ) ) ) )::Nil
      }
      .map( _.reverse )
    hexas ++ pentas
  }
}
