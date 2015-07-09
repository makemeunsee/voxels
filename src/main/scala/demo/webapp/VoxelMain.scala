package demo.webapp

/**
 * Created by markus on 16/02/2015.
 */

import demo.Colors
import geometry.Normal3
import geometry.voronoi.VoronoiModel
import geometry.voronoi.VoronoiModel.CubeModel
import maze.Maze

import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.{Dictionary, JSApp}
import scala.util.Random

object VoxelMain extends JSApp {

  private var model: VoronoiModel = CubeModel
  private implicit var rnd = new Random( System.currentTimeMillis() )

  // ******************** init code ********************

  def main(): Unit = {}

  @JSExport
  def initRnd( seed: Long ): Unit = {
    rnd = new Random( seed )
  }

  @JSExport
  def loadModel( cutCount: Int ): Unit = {
    println( "start" )

    // prepare cuts
    val cutNormals = ( 0 until cutCount ).map { _ =>
      val ( theta, phi ) = geometry.uniformToSphericCoords( rnd.nextDouble(), rnd.nextDouble() )
      Normal3.fromSphericCoordinates( theta, phi )
    }

    val t0 = System.currentTimeMillis()
    // apply cuts
    model = model.cut( cutNormals )
    val t1 = System.currentTimeMillis()
    println( "cut time", t1 - t0 )

    scene.addModel( model, Maze.depthFirstMaze( model.faces ).get )

    if ( rndColors )
      rndColor()
  }

  // ******************** actual three.js scene ********************
  
  @JSExport
  val scene = new ThreeScene

  // ******************** face selection management ********************

  // what's currently selected
  private var selectedFace: Int = -1

  @JSExport
  def selectFace( colorCode: Int ): Dictionary[String] = {
    selectedFace = ThreeScene.revertColorCode( colorCode )

    val ( col, cCol ) = {
      val faces = model.faces
      if ( selectedFace < faces.length && selectedFace > -1 ) {
        val face = faces( selectedFace )
        ( face.color, face.centerColor )
      } else
        ( Colors.BLACK, Colors.BLACK )
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
  def toggleRndColors(): Unit = {
    rndColors = !rndColors
    if ( rndColors )
      rndColor()
    else
      color( Colors.WHITE )
  }

  private def rndColor(): Unit = {
    colorModel( Colors.rndColor, Colors.rndColor )
  }

  private def color( c: Int ): Unit = {
    colorModel( () => c, () => c )
  }

  private def colorModel( color: () => Int, centerColor: () => Int ): Unit = {
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
