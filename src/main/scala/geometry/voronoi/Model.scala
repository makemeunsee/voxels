package geometry.voronoi

import geometry.Normal3
import voxels.Cube

/**
 * Created by markus on 29/06/15.
 */
trait Model {
  def faces: Array[Face]
  def cutNewFace( theta: Double, phi: Double ): Model
}

object CubeModel extends Model {
  def cutNewFace( t: Double, p: Double ) = this
  private val cube = Cube.facesStructure map { _._1 map { i => Normal3( Cube.vertices( i ) ) } }
  private val faceInfo: Seq[(Normal3, Seq[Int])] = Seq(
    ( Normal3( 0,  1,  0 ),  Seq( 2, 5, 3, 4 ) ),
    ( Normal3( 0,  -1, 0 ),  Seq( 2, 5, 3, 4 ) ),
    ( Normal3( 1,  0,  0 ),  Seq( 0, 4, 1, 5 ) ),
    ( Normal3( -1, 0,  0 ),  Seq( 0, 4, 1, 5 ) ),
    ( Normal3( 0,  0,  1 ),  Seq( 0, 3, 1, 5 ) ),
    ( Normal3( 0,  0,  -1 ), Seq( 0, 3, 1, 5 ) )
  )
  def faces = cube.zip( faceInfo ).map { case ( vs, ( n, ns ) ) =>
    Face( n, vs, ns )
  }.toArray
}