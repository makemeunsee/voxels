package geometry.voronoi

import geometry.{Normal3, Vector3}

/**
 * Created by markus on 29/06/15.
 */
case class Face( seed: Normal3
               , vertices: Seq[ Vector3 ]
               , neighbours: Set[ Int ] ) {
  def barycenter: Vector3 = vertices.foldLeft( Vector3( 0, 0, 0 ) )( _ + _ ) / vertices.length
}
