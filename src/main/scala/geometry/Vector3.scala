package geometry

/**
 * Created by markus on 23/05/15.
 */

trait Vector3 {
  def x: Double
  def y: Double
  def z: Double

  def apply( i: Int ): Double = i match {
    case 0 => x
    case 1 => y
    case 2 => z
  }

  def update( i: Int, v: Double ): Vector3

  def norm: Double = math.sqrt( x*x + y*y + z*z )

  def minus( v: Vector3 ): Vector3
  def - = minus _
  def plus( v: Vector3 ): Vector3
  def + = plus _
  def times( scalar: Double ): Vector3
  def * = times _
  def dividedBy( scalar: Double) = times( 1d/scalar )
  def / = dividedBy _

  def dot( v: Vector3 ): Double = x*v.x + y*v.y + z*v.z

  def cross( v: Vector3 ): Vector3

  def negate: Vector3
}

object Vector3 {
  def apply( x: Double, y: Double, z: Double ): Vector3 = new Vec3( x, y, z )
}

private case class Vec3( x: Double, y: Double, z: Double) extends Vector3 {

  def update( i: Int, v: Double ) = i match {
    case 0 => copy( x = v )
    case 1 => copy( y = v )
    case 2 => copy( z = v )
  }

  def minus( v: Vector3 ) = Vec3( x - v.x, y - v.y, z - v.z )
  def plus( v: Vector3 ) = Vec3( x + v.x, y + v.y, z + v.z )
  def times( scalar: Double ) = Vec3( x*scalar, y*scalar, z*scalar )

  def cross( v: Vector3 ) =
    Vec3( y*v.z - v.y*z,
          z*v.x - v.z*x,
          x*v.y - v.x*y )

  def negate = Vec3( -x, -y, -z )
}

object Normal3 {
  def apply( x: Double, y: Double, z: Double ): Normal3 = apply( Vector3( x, y, z ) )
  def apply( vec3: Vector3 ): Normal3 = {
    val Vec3( x, y ,z ) = vec3 / vec3.norm
    new Norm3( x, y ,z )
  }
}

trait Normal3 extends Vector3 {
  override def norm: Double = 1d
}

private class Norm3( x: Double, y: Double, z: Double ) extends Vec3( x, y, z ) with Normal3