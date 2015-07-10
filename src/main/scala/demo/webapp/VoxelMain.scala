package demo.webapp

/**
 * Created by markus on 16/02/2015.
 */

import demo.Colors
import geometry.Normal3
import geometry.voronoi.VoronoiModel
import geometry.voronoi.VoronoiModel.CubeModel
import maze.Maze

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.JSApp
import scala.util.Random

object VoxelMain extends JSApp {

  private var model: VoronoiModel = CubeModel
  private var maze: Maze[Int] = null // beh
  private var depthMap: Map[Int, Int] = null // beh
  private var depthMax = 0

  private implicit var rnd = new Random( System.currentTimeMillis() )

  private val config = new Config
  private val datGUI = new DatGUI

  // ******************** actual three.js scene ********************

  @JSExport
  val scene = new ThreeScene( config )

  // ******************** init code ********************

  def main(): Unit = {
    val jsCfg = config.asInstanceOf[js.Dynamic]

    val general = datGUI.addFolder( "General" )

    general
      .addBoolean( jsCfg, "Show axis" )
      .onChange { _: Boolean => scene.toggleAxis() }
    general
      .addBoolean( jsCfg, "Cull back" )
      .onChange { _: Boolean => scene.toggleCullback() }
    general
      .addRange( jsCfg, "Downsampling", 0, 7 )
      .onChange { _: Float => scene.udpateDownsampling() }
    general
      .addRange( jsCfg, "Explosion", 0, 100 )
    general
      .addRange( jsCfg, "Borders width", 0, 2, 0.1f )
    general
      .addColor( jsCfg, "Borders color" )
    general.open()

    val mazeFolder = datGUI.addFolder( "Maze" )
    mazeFolder
      .addRange( jsCfg, "Maze depth scaling", -100, 100 )
      .onChange { v: Float => () }
    mazeFolder.open()

    val colors = datGUI.addFolder( "Coloring" )
    colors
      .addList( jsCfg, "Palette", Config.colorings )
      .onChange { _: String => applyColors() }
    colors
      .addRange( jsCfg, "Rainbow span", 1f, depthMax )
      .onChange { _: Float => applyColors() }
    colors.open()

    datGUI.open()
  }

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

    // generate maze
    maze = Maze.depthFirstMaze( model.faces ).get
    val mazeDepthsAndLimits = maze.toDepthMap
    depthMap = mazeDepthsAndLimits._1
    depthMax = mazeDepthsAndLimits._2

    val t2 = System.currentTimeMillis()

    println( "cut time", t1 - t0 )
    println( "maze time", t2 - t1 )

    scene.addModel( model, maze, mazeDepthsAndLimits )

    config.`Rainbow span` = depthMax

    applyColors()

    main()
  }

  // ******************** coloring ********************

  private def applyColors(): Unit = config.`Palette` match {
    case Config.uniforms =>
      import Colors.floatsToInt
      color( config.`Uniform color 0`.toSeq, config.`Uniform color 1`.toSeq )
    case Config.randoms =>
      rndColor()
    case Config.chRainbow =>
      rainbow( Colors.cubeHelixRainbow )
    case Config.niRainbow =>
      rainbow( Colors.matteoNiccoliRainbow )
    case Config.laRainbow =>
      rainbow( Colors.lessAngryRainbow )
    case _ =>
      ()
  }

  private def rndColor(): Unit = {
    import Colors.intColorToFloatsColors
    colorModel( { _: ( Int, Int ) => Colors.rndColor() }, { _: ( Int, Int ) => Colors.rndColor() } )
  }

  private def color( c0: Int, c1: Int ): Unit = {
    import Colors.intColorToFloatsColors
    colorModel( { _: ( Int, Int ) => c0 }, { _: ( Int, Int ) => c1 } )
  }

  private def rainbow( rainbowFct: Float => Float => ( Float, Float, Float ) ): Unit = {
    val rnbw = rainbowFct( depthMax.toFloat / config.safeRainbowSpan )
      .compose { idAndDepth: ( Int, Int ) => idAndDepth._2.toFloat / depthMax }
    colorModel( rnbw , rnbw )
  }

  private def colorModel( color: ( ( Int, Int ) ) => ( Float, Float, Float )
                        , centerColor: ( ( Int, Int ) ) => ( Float, Float, Float ) ): Unit = {
    ThreeScene.withOffsetsAndSizes( model.faces )
      .zipWithIndex
      .foreach { case ( ( f, o, s ), id ) =>
        val col = color( ( id, depthMap( id ) ) )
        val cCol = centerColor( ( id, depthMap( id ) ) )
        scene.colorFace( o, s, col, cCol )
      }
  }
}
