package geometry

/**
 * Created by markus on 23/05/15.
 */

object Vector3 {
  def apply( x: Double, y: Double, z: Double ): Vector3 = new Vec3( x, y, z )

  def dist( v0: Vector3, v1: Vector3): Double = {
    ( v0 - v1 ).norm
  }
}

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