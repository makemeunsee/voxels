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
  def fromSphericCoordinates( theta: Double, phi: Double ): Normal3 = {
    val cosT = math.cos( theta )
    val sinT = math.sin( theta )
    val cosP = math.cos( phi )
    val sinP = math.sin( phi )
    new Norm3( sinP*cosT, cosP, sinP*sinT )
  }
}

trait Normal3 extends Vector3 {
  override def norm: Double = 1d
}

private class Norm3( x: Double, y: Double, z: Double ) extends Vec3( x, y, z ) with Normal3