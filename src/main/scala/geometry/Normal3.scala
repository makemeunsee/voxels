package geometry

/**
 * Created by markus on 29/06/2015.
 */
object Normal3 {
  def apply( x: Double, y: Double, z: Double ): Normal3 = apply( Vector3( x, y, z ) )
  def apply( vec3: Vector3 ): Normal3 = {
    val Vec3( x, y ,z ) = vec3 / vec3.norm
    new Norm3( x, y ,z )
  }

  // equator on plane x,z
  def latLongPosition( lati: Double, longi: Double ): Normal3 = {
    val cos0 = math.cos( lati )
    val sin0 = math.sin( lati )
    val cos1 = math.cos( longi )
    val sin1 = math.sin( longi )
    new Norm3( cos0*cos1, sin0, -cos0*sin1 )
  }
}

trait Normal3 extends Vector3 {
  override def norm: Double = 1d
}

private class Norm3( x: Double, y: Double, z: Double ) extends Vec3( x, y, z ) with Normal3