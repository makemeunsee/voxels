package geometry

/**
 * Created by markus on 08/06/15.
 */
object Matrix4 {

  val unit = Matrix4(1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1)

  val tolerance: Double = 0.00001d

  private def underTolerance( d: Double ) = d <= tolerance && d >= -tolerance

  def rotationMatrix( angle: Double, vX: Double, vY: Double, vZ: Double ): Matrix4 = {
    val n = math.sqrt( vX*vX + vY*vY + vZ*vZ )
    val nX = vX / n
    val nY = vY / n
    val nZ = vZ / n
    val cosA = math.cos( angle )
    val sinA = math.sin( angle )
    Matrix4( cosA + nX*nX*(1-cosA),    nX*nY*(1-cosA) - nZ*sinA, nX*nZ*(1-cosA) + nY*sinA, 0
           , nX*nY*(1-cosA) + nZ*sinA, cosA + nY*nY*(1-cosA),    nY*nZ*(1-cosA) - nX*sinA, 0
           , nX*nZ*(1-cosA) - nY*sinA, nY*nZ*(1-cosA) + nX*sinA, cosA + nZ*nZ*(1-cosA),    0
           , 0,                        0,                        0,                        1 )
  }

  def rotationMatrix( from: Vector3, to: Vector3, fallbackAxis: Option[Normal3] = None ): Matrix4 = {
    val v = from cross to
    val s = v.norm
    val c = from dot to
    val vM =
      Matrix4( 0,    -v.z, v.y,  0
             , v.z,  0,    -v.x, 0
             , -v.y, v.x,  0,    0
             , 0,    0,    0,    0 )
    if( underTolerance( s ) )
      if( c >= 0 )
        unit
      else {
        val n = fallbackAxis.getOrElse( Normal3( from cross from.update( 0, if ( from.x != 0) 2 * from.x else from.x + 1 ) ) )
        rotationMatrix(math.Pi, n.x, n.y, n.z )
      }
    else
      unit + vM + ( ( vM * vM ) scalarTimes ( ( 1-c ) / s / s ) )
  }

  def translationMatrix( v: Vector3 ): Matrix4 = {
    Matrix4( 1, 0, 0, v.x
           , 0, 1, 0, v.y
           , 0, 0, 1, v.z
           , 0, 0, 0, 1 )
  }

  def naiveRotMat( theta: Double, phi: Double ): Matrix4 = {
    rotationMatrix( theta, 0, 1, 0 ) * rotationMatrix( phi, 1, 0, 0 )
  }

  def zoomMatrix( zoom: Double ): Matrix4 = {
    assert( zoom != 0 )
    Matrix4.unit.copy( a33 = 1d / zoom )
  }

  private def orthoMatrix( left: Double, right: Double, bottom: Double, top: Double, near: Double, far: Double ): Matrix4 = {
    val x_orth = 2 / ( right - left )
    val y_orth = 2 / ( top - bottom )
    val z_orth = -2 / ( far - near )
    val tx = -( right + left ) / ( right - left )
    val ty = -( top + bottom ) / ( top - bottom )
    val tz = -( far + near ) / ( far - near )
    Matrix4( x_orth, 0, 0, tx
      , 0, y_orth, 0, ty
      , 0, 0, z_orth, tz
      , 0, 0, 0, 1 )
  }

  def orthoMatrixFromScreen( w: Double, h: Double, k: Double ) = {
    val aspect = w / h
    val far = 100
    val near = -100
    val right = k * aspect
    val top = k
    val left = -right
    val bottom = -top
    orthoMatrix( left, right, bottom, top, near, far )
  }
}

case class Matrix4( a00: Double, a01: Double, a02: Double, a03: Double
                  , a10: Double, a11: Double, a12: Double, a13: Double
                  , a20: Double, a21: Double, a22: Double, a23: Double
                  , a30: Double, a31: Double, a32: Double, a33: Double ) {
  def toSeqs =
    Seq( Seq( a00, a01, a02, a03 )
       , Seq( a10, a11, a12, a13 )
       , Seq( a20, a21, a22, a23 )
       , Seq( a30, a31, a32, a33 ) )

  def row( i: Int ) = {
    assert( i < 4 && i >= 0 )
    i match {
      case 0 => Vector4( a00, a01, a02, a03 )
      case 1 => Vector4( a10, a11, a12, a13 )
      case 2 => Vector4( a20, a21, a22, a23 )
      case 3 => Vector4( a30, a31, a32, a33 )
    }
  }

  def col( i: Int ) = {
    assert( i < 4 && i >= 0 )
    i match {
      case 0 => Vector4( a00, a10, a20, a30 )
      case 1 => Vector4( a01, a11, a21, a31 )
      case 2 => Vector4( a02, a12, a22, a32 )
      case 3 => Vector4( a03, a13, a23, a33 )
    }
  }

  def times( m: Matrix4 ) = Matrix4(
    row( 0 ) dot m.col( 0 ), row( 0 ) dot m.col( 1 ), row( 0 ) dot m.col( 2 ), row( 0 ) dot m.col( 3 ),
    row( 1 ) dot m.col( 0 ), row( 1 ) dot m.col( 1 ), row( 1 ) dot m.col( 2 ), row( 1 ) dot m.col( 3 ),
    row( 2 ) dot m.col( 0 ), row( 2 ) dot m.col( 1 ), row( 2 ) dot m.col( 2 ), row( 2 ) dot m.col( 3 ),
    row( 3 ) dot m.col( 0 ), row( 3 ) dot m.col( 1 ), row( 3 ) dot m.col( 2 ), row( 3 ) dot m.col( 3 )
  )

  def * = times _

  def plus( m: Matrix4 ): Matrix4 =
    Matrix4( a00 + m.a00, a01 + m.a01, a02 + m.a02, a03 + m.a03
           , a10 + m.a10, a11 + m.a11, a12 + m.a12, a13 + m.a13
           , a20 + m.a20, a21 + m.a21, a22 + m.a22, a23 + m.a23
           , a30 + m.a30, a31 + m.a31, a32 + m.a32, a33 + m.a33 )

  def + = plus _

  def scalarTimes( a: Double ): Matrix4 =
    Matrix4( a00 * a, a01 * a, a02 * a, a03 * a
           , a10 * a, a11 * a, a12 * a, a13 * a
           , a20 * a, a21 * a, a22 * a, a23 * a
           , a30 * a, a31 * a, a32 * a, a33 * a )

  // multiply vector v by this matrix. returns this*v.
  def apply( v: Vector3 ): Vector3 = apply( Vector4( v ) ).toVec3

  // multiply vector v by this matrix. returns this*v.
  def apply( v: Vector4 ): Vector4 = {
    Vector4( row( 0 ) dot v
           , row( 1 ) dot v
           , row( 2 ) dot v
           , row( 3 ) dot v )
  }

}