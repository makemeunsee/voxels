package geometry.voronoi

import geometry.{Vector3, Normal3}

/**
 * Created by markus on 29/06/15.
 */
case class Face( seed: Normal3, vertice: Seq[ Normal3 ], neighbours: Seq[ Int ] ) {
  def barycenter: Vector3 = vertice.foldLeft( Vector3( 0, 0, 0 ) )( _ + _ ) / vertice.length
}
