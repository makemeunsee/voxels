package geometry

/**
 * Created by markus on 23/05/15.
 */
object Vector4 {
  def apply( v: Vector3 ): Vector4 = Vector4( v.x, v.y, v.z, 1 )
}

case class Vector4( x: Double, y: Double, z: Double, w: Double ) {
  def apply( i: Int ) = i match {
    case 0 => x
    case 1 => y
    case 2 => z
    case 3 => w
  }

  def toVec3 = Vector3( x, y, z )

  def update( i: Int, v: Double ) = i match {
    case 0 => copy( x = v )
    case 1 => copy( y = v )
    case 2 => copy( z = v )
    case 3 => copy( w = v )
  }

  def norm = math.sqrt( x*x + y*y + z*z + w*w )

  def normalize = {
    val n = norm
    Vector4( x / n, y / n, z / n, w / n )
  }

  def minus( v: Vector4 ) = Vector4( x - v.x, y - v.y, z - v.z, w - v.w )
  def - = minus _
  def plus( v: Vector4 ) = Vector4( x + v.x, y + v.y, z + v.z, w + v.w )
  def + = plus _
  def times( scalar: Double ) = Vector4( x*scalar, y*scalar, z*scalar, w*scalar )
  def * = times _
  def dividedBy( scalar: Double) = times( 1d/scalar )
  def / = dividedBy _
  def dot( v: Vector4 ) = x*v.x + y*v.y + z*v.z + w*v.w
}