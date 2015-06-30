package demo.webapp

/**
 * Created by markus on 16/02/2015.
 */

import demo.{Colors, Rnd}
import geometry.voronoi.VoronoiModel
import geometry.voronoi.VoronoiModel.CubeModel

import scala.scalajs.js.{Dictionary, JSApp}
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport

object VoxelMain extends JSApp {

  private val model: VoronoiModel = ( 0 until 10000 ).foldLeft( CubeModel: VoronoiModel ) { case ( m, _ ) =>
    val u = Rnd.rndUniformDouble()
    val v = Rnd.rndUniformDouble()
    val ( theta, phi ) = geometry.uniformToSphericCoords( u, v )
    m.cut( theta, phi )
  }

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

    val ( col, cCol ) = {
      val faces = model.faces
      if ( selectedFace < faces.length && selectedFace > -1 ) {
        val face = faces( selectedFace )
        ( face.color, face.centerColor )
      } else
        ( Colors.WHITE, Colors. WHITE )
    }
    Map( ( "faceId", selectedFace.toString )
       , ( "faceColor", "%06x".format( col ) )
       , ( "faceCenterColor", "%06x".format( cCol ) )
       )
      .toJSDictionary
  }

  @JSExport
  def clearSelection(): Unit = {
    selectedFace = -1
  }

  // ******************** coloring ********************

  private implicit var rndColors = false

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
    ThreeScene.withOffsetsAndSizes( model.faces )
      .zipWithIndex
      .foreach { case ( ( _, o, s ), id ) =>
        val col = color()
        val cCol = centerColor()
        scene.colorFace( o, s, col, cCol )
        model.updateColor( id, col, cCol )
      }
  }

  private def colorFace( faceId: Int, color: Option[Int] = None, centerColor: Option[Int] = None ): Unit = {
    import Colors.intColorToFloatsColors
    ThreeScene.withOffsetsAndSizes( model.faces ).lift( faceId ).foreach { case ( f, o, s ) =>
      val defCols = ( f.color, f.centerColor )
      val col = color.getOrElse( defCols._1 )
      val cCol = centerColor.getOrElse( defCols._2 )
      scene.colorFace( o, s, col, cCol )
      model.updateColor( faceId, col, cCol )
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
