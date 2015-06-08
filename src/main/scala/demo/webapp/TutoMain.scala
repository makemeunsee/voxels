package demo.webapp

/**
 * Created by markus on 16/02/2015.
 */

import geometry.{Matrix4, Vec3}
import geometry.Matrix4.rotationMatrix
import voxels._

import scala.scalajs.js.JSConverters._

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.Array

object TutoMain extends JSApp {
  def main(): Unit = {
    println("start")
  }

  @JSExport
  def naiveRotMat(theta: Double, phi: Double): Array[Array[Double]] = {
    ( rotationMatrix( theta, 0, 1, 0 ) * rotationMatrix( phi, 1, 0, 0 ) ).toSeqs.map( _.toJSArray ).toJSArray
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

  private var voxel = Voxel( Cube, Matrix4.unit )
  private var colorToFaceDict = Map.empty[Int, Int]

  @JSExport
  def loadVoxel( i: Int ): Array[Array[Double]] = {
    voxel = Voxel( standards.getOrElse( i, Cube ), Matrix4.unit )
    voxelToRaw( voxel )
  }

  private def voxelToRaw( v: Voxel ): Array[Array[Double]] = {
    def faceIdToColorCode( i: Int ) = ( i+1 ) << 17
    colorToFaceDict = v.faces.indices.map( i => ( faceIdToColorCode( i ), i ) ).toMap
    def aod: Array[Double] = Array()
    val ( vs, ns, cs, pcs, is ) = v.faces.zipWithIndex.foldLeft( aod, aod, aod, aod, aod ) { case ( ( vertices, normals, centerFlags, pickColors, indices ), ( f, fId ) ) =>
      val count = f.vertices.length
      val Vec3( nx, ny, nz ) = f.normal
      val Vec3( cx, cy, cz ) = f.center
      val offset = ( vertices.length / 3 ).toDouble
      val newVertices = vertices ++ f.rawVertices ++ Array( cx, cy, cz )
      val newNormals = normals ++ ( 0 to count ).flatMap( _ => nx :: ny :: nz :: Nil )
      val newCenterFlags = centerFlags ++ ( 0 until count ).map( _ => 0d ) :+ 1d
      val newPickColors = pickColors ++ ( 0 to count ).map( _ => faceIdToColorCode( fId ).toDouble )
      val newIndices = indices ++ ( 0 until count ).flatMap( i => ( offset + count ) :: ( offset + i ) :: ( offset + (i+1)%count ) :: Nil )
      ( newVertices, newNormals, newCenterFlags, newPickColors, newIndices )
    }
    Array( vs, ns, cs, pcs, is )
  }

  @JSExport
  def getFaceType( color: Array[Int] ): String = {
    assert( color.length == 3 )
    val colorCode = ( color( 0 ) << 16 ) + ( color( 1 ) << 8 ) + color( 2 )
    val fId = colorToFaceDict.get( colorCode )
    fId.fold( "" )( i => voxel.faces( i ).faceType.toString )
  }

  @JSExport
  def getVoxelName(): String = voxel.standard.name

  @JSExport
  def rotateAroundFace( color: Array[Int] ): Array[Array[Double]] = {
    assert( color.length == 3 )
    val colorCode = ( color( 0 ) << 16 ) + ( color( 1 ) << 8 ) + color( 2 )
    colorToFaceDict.get( colorCode ) match {
      case None =>
        voxelToRaw( voxel )
      case Some( fId ) =>
        voxel = voxel.copy( transformation = voxel.transformation * voxel.faces( fId ).conjugationMatrix )
        voxelToRaw( voxel )
    }
  }
}
