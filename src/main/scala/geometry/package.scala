/**
 * Created by markus on 26/05/15.
 */
package object geometry {
  val gold = ( 1 + math.sqrt( 5 ) ) / 2

  def uniformToSphericCoordinates( u: Double, v: Double ): ( Double, Double ) = ( 2*math.Pi*u, math.acos( 2*v - 1 ) )
}
