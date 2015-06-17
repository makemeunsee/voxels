package demo.webapp

/**
 * Created by markus on 16/02/2015.
 */

import demo.Rnd
import geometry.{Matrix4, Vec3}
import geometry.Matrix4.{rotationMatrix, translationMatrix}
import io._
import voxels._

import scala.collection.immutable.TreeSet
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.Dictionary

object VoxelMain extends JSApp {
  def main(): Unit = {
    println( "start" )
  }

  // ******************** voxel static properties ********************
  
  @JSExport
  val voxelTypeCount = standards.size

  @JSExport
  def getVoxelName( id: Int ): String = standards.lift( id ).fold( "" )( _.name )

  // ******************** actual three.js scene ********************
  
  @JSExport
  val scene = new ThreeScene

  // ******************** voxels management ********************
  
  private var voxels: Map[Int, Voxel] = Map.empty
  // memory of all actions undertaken in this session
  private var voxelActionsStack: List[VoxelAction] = Nil
  // memory of "holes" in the voxel ids
  private var freeVoxelIds: Set[Int] = TreeSet.empty

  // clear everything, load a single voxel
  @JSExport
  def loadVoxel( i: Int ): Unit = {
    unloadVoxels()
    val v = Voxel( standards.getOrElse( i, Cube ), Matrix4.unit )
    voxels = Map( 0 -> v )
    scene.addVoxel( 0, v )
    scene.centerOn( v )
    voxelActionsStack = Insert( i ) :: voxelActionsStack
  }

  // clear everything
  @JSExport
  def unloadVoxels(): Unit = {
    freeVoxelIds = TreeSet.empty
    scene.clear()
    voxels = Map.empty
    voxelActionsStack = Nil
    clearSelection()
  }

  // ******************** memorized visual actions ********************

  @JSExport
  def centerViewOn( voxelId: Int ): Unit = {
    voxels.lift( voxelId ).foreach { voxel =>
      scene.centerOn( voxel )
      voxelActionsStack = CenterOn( voxelId ) :: voxelActionsStack
    }
  }

  // ******************** voxel and face selection management ********************

  // what's currently selected
  private var selectedVoxel: Int = -1
  private var selectedFace: Int = -1
  // what can be docked to the selection ( voxelStd id, face id, rotation step )
  private var dockingOptions = Seq.empty[( Int,Int,Int )]
  // which id was last docked
  private var lastDockedId: Int = -1

  @JSExport
  def selectFace( colorCode: Int ): Dictionary[String] = {
    val ( vId, fId ) = ThreeScene.revertColorCode( colorCode )
    dockingOptions = listDockingOptions( vId, fId )
    selectedVoxel = vId
    selectedFace = fId
    val selection = voxels.lift( vId ).flatMap( _.faces.lift( fId ) ).fold( -1, -1, "" )( f => ( vId, fId, f.faceType.toString ) )
    Map( ( "voxelId", selection._1.toString )
       , ( "faceId", selection._2.toString )
       , ( "faceInfo", selection._3 )
       )
      .toJSDictionary
  }

  @JSExport
  def clearSelection(): Unit = {
    selectedVoxel = -1
    selectedFace = -1
    dockingOptions = Seq.empty
    lastDockedId = -1
  }

  @JSExport
  def showDockingOptions(): Dictionary[String] = dockingOptions
    .zipWithIndex
    .map { case ( ( stdId, _, _ ), i ) =>
      val std = standards( stdId )
      ( i.toString
      , s"${std.name}"
      )
    }
    .toMap
    .toJSDictionary

  private def listDockingOptions( voxelId: Int, faceId: Int ): Seq[( Int,Int,Int )] = {
    voxels
      .lift( voxelId )
      .flatMap( _.faces.lift( faceId ) )
      .fold( Seq.empty[( Int,Int,Int )] ) { f =>
        val faceType = f.faceType
        standards
          .flatMap { case ( stdId, std ) =>
            std
              .uniquePositionings
              .collect { case ( fId, fType, rot ) if fType == faceType => ( stdId, fId, rot ) }
        }.toSeq
    }
  }
  
  @JSExport
  def dockVoxel( dockingID: Int ): Unit = {
    assert( dockingID >= 0 && dockingID < dockingOptions.length )

    val newVId = dockingOptions( dockingID )._1
    val fId = dockingOptions( dockingID )._2
    val rot = dockingOptions( dockingID )._3

    dockVoxel( newVId, fId, rot, selectedVoxel, selectedFace )
  }

  private def dockVoxel( stdId: Int, faceId: Int, rot: Int, onVoxelId: Int, onFaceId: Int ): Unit = {

    val newVoxelStd = standards.getOrElse( stdId, Cube )
    val newVoxelUntransformed = Voxel( newVoxelStd, Matrix4.unit )
    val sourceFace = newVoxelUntransformed.faces( faceId )
    val targetFace = voxels( onVoxelId ).faces( onFaceId )

    // rotation so docking faces actually face each other
    val targetN@Vec3( nx, ny, nz ) = targetFace.normal
    val sourceN = sourceFace.normal
    val rM = rotationMatrix( sourceN, targetN.negate )

    // rotated voxel
    val newVoxelRotated = Voxel( newVoxelStd, rM )
    val sourceFace1 = newVoxelRotated.faces( faceId )

    val to = targetFace.center

    // spin so docking faces actually fit each other
    val from = sourceFace1.center
    val preSpinTranslation = translationMatrix( from.negate )
    val postSpinTranslation = translationMatrix( from )
    val spinRotation = rotationMatrix( ( sourceFace1.vertices.head - from ).normalize
      , ( targetFace.vertices.head - to ).normalize
      , Some( targetN ) )
    // spin shift (rotation variations)
    val bonusSpinRotation = rotationMatrix( rot*2*math.Pi/sourceFace.vertices.length, nx, ny, nz )

    // translation so docking faces actually touch each other
    val tM = translationMatrix( to - from )

    val newVoxel = Voxel( newVoxelStd, tM * postSpinTranslation * spinRotation * bonusSpinRotation * preSpinTranslation * rM  )

    lastDockedId = if ( freeVoxelIds.isEmpty ) voxels.size
    else { val r = freeVoxelIds.head
      freeVoxelIds = freeVoxelIds - r
      r }
    println( s"new voxel with id $lastDockedId, free ids: $freeVoxelIds" )

    voxels = voxels + ( ( lastDockedId, newVoxel ) )
    scene.addVoxel( lastDockedId, newVoxel )
    voxelActionsStack = Dock( stdId, faceId, rot, onVoxelId, onFaceId ) :: voxelActionsStack
  }

  @JSExport
  def undockLastVoxel(): Unit = {
    scene.removeVoxel( lastDockedId )
    voxels = voxels - lastDockedId
    freeVoxelIds = freeVoxelIds + lastDockedId
    voxelActionsStack = voxelActionsStack.tail
  }

  @JSExport
  def deleteSelected(): Unit = delete( selectedVoxel )

  private def delete( id: Int ): Unit = {
    if ( voxels.size > 1 ) {
      voxels.get( id ).foreach { v =>
        scene.removeVoxel( id )
        voxels = voxels - id
        freeVoxelIds = freeVoxelIds + id
        voxelActionsStack = Delete( id ) :: voxelActionsStack
        println( s"deleted voxel $id, free ids: $freeVoxelIds" )
        clearSelection()
      }
    }
  }

  // ******************** special effects, only visual effects (yet) ********************

  private implicit var rndColors = true

  @JSExport
  def toggleRndColors(): Unit = {
    rndColors = !rndColors
    if ( rndColors )
      voxels.keys foreach rndColor
    else
      voxels.keys foreach whiteColor
  }

  private def rndColor( voxelId: Int ): Unit = {
    colorVoxel( voxelId, Rnd.rndColor, Rnd.rndColor )
  }

  private def whiteColor( voxelId: Int ): Unit = {
    colorVoxel( voxelId, () => ( 1f, 1f, 1f ), () => ( 1f, 1f, 1f ) )
  }

  private def colorVoxel( voxelId: Int, color: () => ( Float, Float, Float), centerColor: () => ( Float, Float, Float ) ): Unit = {
    voxels.lift( voxelId ).foreach { voxel =>
      ThreeScene.withOffsetsAndSizes( voxel.faces ).foreach { case ( _, o, s ) =>
        scene.colorFace( voxelId, o, s, color(), centerColor() )
      }
    }
  }

  // ******************** load/save voxel constructions ********************

  @JSExport
  def buildCode(): String = voxelActionsStack.mkString( "," )

  @JSExport
  def loadCode( code: String ): Int = {
    var id = -1
    code.split( "," ).reverse.foreach {
      case VoxelAction.deletePattern( c ) =>
        delete( c.toInt )
      case VoxelAction.insertPattern( c ) =>
        id = c.toInt
        loadVoxel( id )
      case VoxelAction.centerOnPattern( c ) =>
        centerViewOn( c.toInt )
      case VoxelAction.dockPattern( c0, c1, c2, c3, c4 ) =>
        dockVoxel( c0.toInt, c1.toInt, c2.toInt, c3.toInt, c4.toInt )
      case _ => ()
    }
    id
  }

}
