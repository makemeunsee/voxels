package demo.webapp

/**
 * Created by markus on 16/02/2015.
 */

import demo.Colors
import geometry.voronoi.CubeModel
import voxels._

import scala.scalajs.js.JSConverters._
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.Dictionary

object VoxelMain extends JSApp {

  private var model = CubeModel( (0 until 6).map(_ => (Colors.WHITE, Colors.WHITE)) )

  def main(): Unit = {
    println( "start" )
    scene.addModel( model )
  }

  // ******************** actual three.js scene ********************
  
  @JSExport
  val scene = new ThreeScene

  // ******************** voxels management ********************
  
  // clear everything
  @JSExport
  def unloadVoxels(): Unit = {
    scene.clear()
    clearSelection()
  }

  // ******************** voxel and face selection management ********************

  // what's currently selected
  private var selectedFace: Int = -1
  // what can be docked to the selection ( voxelStd id, face id, rotation step )

  @JSExport
  def selectFace( colorCode: Int ): Dictionary[String] = {
    selectedFace = ThreeScene.revertColorCode( colorCode )

    Map( ( "faceId", selectedFace.toString )
       , ( "faceColor", "%06x".format( Colors.WHITE ) )
       , ( "faceCenterColor", "%06x".format( Colors.WHITE ) )
       )
      .toJSDictionary
  }

  @JSExport
  def clearSelection(): Unit = {
    selectedFace = -1
  }

  // ******************** coloring ********************

  private implicit var rndColors = false

  private def colorsForStd( std: VoxelStandard ): Seq[( Int, Int )] = {
    if ( rndColors ) ( 0 until std.faceCount ).map( _ => ( Colors.rndColor(), Colors.rndColor() ) )
    else ( 0 until std.faceCount ).map( _ => ( Colors.WHITE, Colors.WHITE ) )
  }

  @JSExport
  def colorsAreRandom(): Boolean = rndColors

  @JSExport
  def toggleRndColors(): Unit = {
    rndColors = !rndColors
    if ( rndColors )
      rndColor()
    else
      whiteColor()
  }

  private def rndColor(): Unit = {
    colorVoxel( Colors.rndColor, Colors.rndColor )
  }

  private def whiteColor(): Unit = {
    colorVoxel( () => Colors.WHITE, () => Colors.WHITE )
  }

  private def colorVoxel( color: () => Int, centerColor: () => Int ): Unit = {
    import Colors.intColorToFloatsColors
    var newColors = List.empty[( Int, Int )]
    ThreeScene.withOffsetsAndSizes( model.faces )
      .foreach { case ( _, o, s ) =>
      val col = color()
      val cCol = centerColor()
      newColors = ( col, cCol ) :: newColors
      scene.colorFace( o, s, col, cCol )
    }
    model = model.copy( colors = newColors.reverse )
  }

  private def colorFace( faceId: Int, color: Option[Int] = None, centerColor: Option[Int] = None ): Unit = {
    import Colors.intColorToFloatsColors
    ThreeScene.withOffsetsAndSizes( model.faces ).lift( faceId ).foreach { case ( f, o, s ) =>
      val defCols = model.colors( faceId )
      val col = color.getOrElse( defCols._1 )
      val cCol = centerColor.getOrElse( defCols._2 )
      scene.colorFace( o, s, col, cCol )
      model = model.copy( colors = model.colors.updated( faceId, ( col, cCol ) ) )
    }
  }

  @JSExport
  def changeFaceColor( color: String ): Unit = {
    colorFace( selectedFace, color = Some( Integer.parseInt( color.substring( 1 ), 16 ) ) )
  }

  @JSExport
  def changeFaceCenterColor( color: String ): Unit = {
    colorFace( selectedFace, centerColor = Some( Integer.parseInt( color.substring( 1 ), 16 ) ) )
  }

}
