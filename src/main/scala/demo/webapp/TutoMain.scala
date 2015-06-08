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
import scala.scalajs.js.{Array, Dictionary}

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
  private var selectedFace: Int = -1
  // ( voxelStd id, face id, rotation step )
  private var dockingOptions = Seq.empty[(Int,Int,Int)]

  @JSExport
  def loadVoxel( i: Int ): Array[Array[Double]] = {
    voxel = Voxel( standards.getOrElse( i, Cube ), Matrix4.unit )
    selectedFace = -1
    dockingOptions = Seq.empty
    voxelToRaw( voxel )
  }

  private def voxelToRaw( v: Voxel ): Array[Array[Double]] = {
    def faceIdToColorCode( i: Int ) = ( i+1 ) << 17
    colorToFaceDict = v.faces.indices.map( i => ( faceIdToColorCode( i ), i ) ).toMap
    def aod: Array[Double] = Array()
    val ( vertices, normals, centerFlags, pickColors, indices ) = v.faces
      .zipWithIndex
      .foldLeft( aod, aod, aod, aod, aod ) { case ( ( vs, ns, cs, pcs, is ), ( f, fId ) ) =>
        val count = f.vertices.length
        val Vec3( nx, ny, nz ) = f.normal
        val Vec3( cx, cy, cz ) = f.center
        val offset = ( vs.length / 3 ).toDouble
        val newVertices = vs ++ f.rawVertices ++ Array( cx, cy, cz )
        val newNormals = ns ++ ( 0 to count ).flatMap( _ => nx :: ny :: nz :: Nil )
        val newCenterFlags = cs ++ ( 0 until count ).map( _ => 0d ) :+ 1d
        val newPickColors = pcs ++ ( 0 to count ).map( _ => faceIdToColorCode( fId ).toDouble )
        val newIndices =
          is ++
          ( 0 until count )
            .flatMap( i => ( offset + count ) :: ( offset + i ) :: ( offset + (i+1)%count ) :: Nil )
        ( newVertices, newNormals, newCenterFlags, newPickColors, newIndices )
      }
    Array( vertices, normals, centerFlags, pickColors, indices )
  }

  @JSExport
  def selectFace( colorCode: Int ): String = {
    dockingOptions = listDockingOptions( colorCode )
    colorToFaceDict.get( colorCode ) match {
      case None => ""
      case Some( fId ) => voxel.faces( fId ).faceType.toString
    }
  }

  @JSExport
  def showDockingOptions(): Dictionary[String] = dockingOptions
    .zipWithIndex
    .map { case ( ( vId, fId, rotStep ), i ) =>
      val std = standards( vId )
      val face = std.facesStructure( fId )
      val rotInv = face._3
      ( i.toString
      , s"${std.name}, $fId, 2*Pi*$rotStep/$rotInv"
      )
    }
    .toMap
    .toJSDictionary

  private def listDockingOptions( colorCode: Int ): Seq[(Int,Int,Int)] = {
    colorToFaceDict.get( colorCode ) match {
      case None =>
        List.empty
      case Some( fId ) =>
        val faceType = voxel.faces( fId ).faceType
        standards
          .flatMap { case ( vId, std ) =>
            std.facesStructure
              .zipWithIndex
              .groupBy { case ( ( _, ft, rotInv ), _ ) => ( ft, rotInv ) }
              .collect { case ( k, v ) if v.nonEmpty && k._1 == faceType => ( v.head._2, v.head._1._3 ) }
              .flatMap { case ( o_fId, rotInv ) => ( 0 until rotInv ).map( rotStep => ( vId, o_fId, rotStep ) ) }
          }.toSeq
    }
  }

  @JSExport
  def getVoxelName(): String = voxel.standard.name
}
