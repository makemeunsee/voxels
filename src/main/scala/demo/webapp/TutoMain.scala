package demo.webapp

/**
 * Created by markus on 16/02/2015.
 */

import geometry.Vec3
import voxels._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.Array

object TutoMain extends JSApp {
  def main(): Unit = {
    println("start")
  }

  def rotMat( angle: Double, vX: Double, vY: Double, vZ: Double ): Array[Array[Double]] = {
    val n = math.sqrt( vX*vX + vY*vY + vZ*vZ )
    val nX = vX / n
    val nY = vY / n
    val nZ = vZ / n
    val cosA = math.cos( angle )
    val sinA = math.sin( angle )
    Array( Array( cosA + nX*nX*(1-cosA),    nX*nY*(1-cosA) - nZ*sinA, nX*nZ*(1-cosA) + nY*sinA, 0 )
         , Array( nX*nY*(1-cosA) + nZ*sinA, cosA + nY*nY*(1-cosA),    nY*nZ*(1-cosA) - nX*sinA, 0 )
         , Array( nX*nZ*(1-cosA) - nY*sinA, nY*nZ*(1-cosA) + nX*sinA, cosA + nZ*nZ*(1-cosA),    0 )
         , Array( 0,                        0,                        0,                        1 ))
  }

  def multMat( m0: Array[Array[Double]], m1: Array[Array[Double]] ): Array[Array[Double]] = {
    val r: Array[Array[Double]] = Array( Array(0,0,0,0), Array(0,0,0,0), Array(0,0,0,0), Array(0,0,0,0) )
    for ( i <- 0 to 3; j <- 0 to 3 )
      for ( k <- 0 to 3 )
        r( i ).update( j, r ( i )( j ) + m0( i )( k ) * m1( k )( j ) )
    r
  }

  @JSExport
  def naiveRotMat(theta: Double, phi: Double): Array[Array[Double]] = {
    multMat ( rotMat( theta, 0, 1, 0 ), rotMat( phi, 1, 0, 0 ) )
  }

  def latLongPosition(theta: Double, phi: Double, distance: Double): Vec3 = {
    val cosP = math.cos( phi )
    val sinP = math.sin( phi )
    val cosT = math.cos( theta )
    val sinT = math.sin( theta )
    Vec3( distance * sinP * cosT, distance * cosP, distance * sinP * sinT )
  }

  def lookAtMatrix( eye: Vec3, target: Vec3, up: Vec3 ): Array[Array[Double]] = {
    val forward = (target minus eye).normalize
    val right = (forward cross up).normalize
    val up0 =  right cross forward
    Array( Array( right.x, right.y, right.z, -(right dot eye) ),
           Array( up0.x, up0.y, up0.z, -(up0 dot eye) ),
           Array( -forward.x, -forward.y, -forward.z, forward dot eye ),
           Array( 0, 0, 0, 1 ) )
  }

  @JSExport
  def viewMatOf( theta: Double, phi: Double, distance: Double): Array[Array[Double]] = {
    val p = latLongPosition ( theta, math.max( 0.00001, math.min( math.Pi - 0.00001, phi ) ), distance )
    lookAtMatrix( p, Vec3( 0, 0, 0 ), Vec3( 0, 1, 0 ) )
  }

  def orthoMatrix( left: Double, right: Double, bottom: Double, top: Double, near: Double, far: Double ): Array[Array[Double]] = {
    val x_orth = 2 / (right - left)
    val y_orth = 2 / (top - bottom)
    val z_orth = -2 / (far - near)
    val tx = -(right + left) / (right - left)
    val ty = -(top + bottom) / (top - bottom)
    val tz = -(far + near) / (far - near)
    Array( Array( x_orth, 0, 0, tx ),
           Array( 0, y_orth, 0, ty ),
           Array( 0, 0, z_orth, tz ),
           Array( 0, 0, 0, 1 ) )
  }

  @JSExport
  def orthoMatrixFromScreen( w: Double, h: Double, k: Double ) = {
    val hh = if ( h < 0 ) 1 else h
    val aspect = w / hh
    val far = 100
    val near = -100
    val right = k * aspect
    val top = k
    val left = -right
    val bottom = -top
    orthoMatrix( left, right, bottom, top, near, far )
  }

  @JSExport
  val voxelTypeCount = standards.size

  @JSExport
  def getVoxel( i: Int ): Array[Array[Double]] = {
    val voxel = Voxel( standards.getOrElse( i, Cube ) )
    def aod: Array[Double] = Array()
    val ( vs, ns, cs, pcs, ds, is ) = voxel.faces.zipWithIndex.foldLeft( aod, aod, aod, aod, aod, aod ) { case ( ( vertices, normals, centerFlags, pickColors, depths, indices ), ( f, fId ) ) =>
      val count = f.vertices.length
      val Vec3( nx, ny, nz ) = f.normal
      val Vec3( cx, cy, cz ) = f.center
      val offset = ( vertices.length / 3 ).toDouble
      val newVertices = vertices ++ f.rawVertices ++ Array( cx, cy, cz )
      val newNormals = normals ++ ( 0 to count ).flatMap( _ => nx :: ny :: nz :: Nil )
      val newCenterFlags = centerFlags ++ ( 0 until count ).map( _ => 0d ) :+ 1d
      val newPickColors = pickColors ++ ( 0 to count ).map( _ => ( (fId+1) << 17 ).toDouble )
      val newDepths = depths ++ ( 0 to count ).map( _ => 0d )
      val newIndices = indices ++ ( 0 until count ).flatMap( i => ( offset + count ) :: ( offset + i ) :: ( offset + (i+1)%count ) :: Nil )
      ( newVertices, newNormals, newCenterFlags, newPickColors, newDepths, newIndices )
    }
    Array( vs, ns, cs, pcs, ds, is )
  }

}