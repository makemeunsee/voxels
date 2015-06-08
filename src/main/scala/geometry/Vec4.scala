package geometry

/**
 * Created by markus on 23/05/15.
 */
object Vec4 {
  def apply( v: Vec3 ): Vec4 = Vec4( v.x, v.y, v.z, 1 )
}

case class Vec4( x: Double, y: Double, z: Double, w: Double) {
  def apply( i: Int ) = i match {
    case 0 => x
    case 1 => y
    case 2 => z
    case 3 => w
  }

  def toVec3 = Vec3( x, y, z )

  def update( i: Int, v: Double ) = i match {
    case 0 => copy( x = v )
    case 1 => copy( y = v )
    case 2 => copy( z = v )
    case 3 => copy( w = v )
  }

  def norm = math.sqrt( x*x + y*y + z*z + w*w )

  def normalize = {
    val n = norm
    Vec4( x / n, y / n, z / n, w / n )
  }

  def minus( v: Vec4 ) = Vec4( x - v.x, y - v.y, z - v.z, w - v.w )
  def - = minus _
  def plus( v: Vec4 ) = Vec4( x + v.x, y + v.y, z + v.z, w + v.w )
  def + = plus _
  def times( scalar: Double ) = Vec4( x*scalar, y*scalar, z*scalar, w*scalar )
  def * = times _
  def dividedBy( scalar: Double) = times( 1d/scalar )
  def / = dividedBy _
  def dot( v: Vec4 ) = x*v.x + y*v.y + z*v.z + w*v.w
}