package geometry

/**
 * Created by markus on 23/05/15.
 */
case class Vec3( x: Double, y: Double, z: Double) {
  def apply( i: Int ) = i match {
    case 0 => x
    case 1 => y
    case 2 => z
  }

  def update( i: Int, v: Double ) = i match {
    case 0 => copy( x = v )
    case 1 => copy( y = v )
    case 2 => copy( z = v )
  }

  def norm = math.sqrt( x*x + y*y + z*z )

  def normalize = {
    val n = norm
    Vec3( x / n, y / n, z / n )
  }

  def minus( v: Vec3 ) = Vec3( x - v.x, y - v.y, z - v.z )
  def - = minus _
  def plus( v: Vec3 ) = Vec3( x + v.x, y + v.y, z + v.z )
  def + = plus _
  def times( scalar: Double ) = Vec3( x*scalar, y*scalar, z*scalar )
  def dividedBy( scalar: Double) = times( 1d/scalar )
  def * = times _
  def / = dividedBy _
  def dot( v: Vec3 ) = x*v.x + y*v.y + z*v.z
  def cross( v: Vec3 ) =
    Vec3( y*v.z - v.y*z,
      z*v.x - v.z*x,
      x*v.y - v.x*y )

}