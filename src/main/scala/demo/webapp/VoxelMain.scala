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
import scala.scalajs.js.{JSON, JSApp}
import scala.util.Random

object VoxelMain extends JSApp {

  private var model: VoronoiModel = CubeModel
  private var maze: Maze[Int] = Maze.empty( -1 )
  private var depthMap: Map[Int, Int] = Map.empty
  private var depthMax = 0

  private implicit var rnd = new Random( System.currentTimeMillis() )

  private val config = new Config
  private val datGUI = new DatGUI( js.Dynamic.literal( "load" -> JSON.parse( Config.presets ), "preset" -> "Default" ) )

  // ******************** actual three.js scene ********************

  @JSExport
  val scene = new ThreeScene( config )

  // ******************** DatGUI menu controllers ********************

  private def rainbowControllers( rainbowFct: Float => Float => ( Float, Float, Float ) )
                                ( implicit colorParams: DatGUI
                                  , jsCfg: js.Dynamic
                                  , paletteController: DatController[String] ): Seq[DatController[_]] = {
    val spanController = colorParams.addRange( jsCfg, "Rainbow span", 1f, 100f )
    val reverseController = colorParams.addBoolean( jsCfg, "Reverse palette" )
    val newControllers = Seq( spanController, reverseController )
    spanController.onChange{ _: Float => rainbow( config.`Reverse palette` )( rainbowFct ) }
    reverseController.onChange{ _: Boolean => rainbow( config.`Reverse palette` )( rainbowFct ) }
    newControllers
  }

  // apply color palette and dynamically load relevant parameters in the gui
  private def applyColors( colorControllers: Seq[DatController[_]] )
                         ( implicit colorParams: DatGUI, paletteController: DatController[String] ): Unit = {

    implicit val jsCfg = config.asInstanceOf[js.Dynamic]

    // palette has change, remove all parameters from the gui
    colorControllers.foreach( colorParams.remove )

    val newControllers = config.`Palette` match {

      case Config.uniforms =>
        import Colors.jsStringToColor
        color( config.`Color 0`, config.`Color 1` )
        val col0Controller = colorParams.addColor( jsCfg, "Color 0" )
        val col1Controller = colorParams.addColor( jsCfg, "Color 1" )
        val result = Seq( col0Controller, col1Controller )
        col0Controller.onChange { _: String => color( config.`Color 0`, config.`Color 1` ) }
        col1Controller.onChange { _: String => color( config.`Color 0`, config.`Color 1` ) }
        result

      case Config.randoms =>
        fullRndColor()
        Seq.empty

      case Config.randoms2 =>
        rndColor()
        Seq.empty

      case Config.chRainbow =>
        rainbow( config.`Reverse palette` )( Colors.cubeHelixRainbow )
        rainbowControllers( Colors.cubeHelixRainbow )

      case Config.niRainbow =>
        rainbow( config.`Reverse palette` )( Colors.matteoNiccoliRainbow )
        rainbowControllers( Colors.matteoNiccoliRainbow )

      case Config.laRainbow =>
        rainbow( config.`Reverse palette` )( Colors.lessAngryRainbow )
        rainbowControllers( Colors.lessAngryRainbow )

      case _ =>
        Seq.empty
    }
    // update palette controller with new parameter controllers
    paletteController.onChange { _: String => applyColors( newControllers ) }
  }

  // ******************** init code ********************

  def main(): Unit = {
    val jsCfg = config.asInstanceOf[js.Dynamic]

    datGUI.remember( jsCfg )

    val general = datGUI.addFolder( "General" )

    general
      .addBoolean( jsCfg, "Show axis" )
      .onChange { _: Boolean => scene.toggleAxis() }
    general
      .addColor( jsCfg, "Background color" )
      .onChange { a: String => scene.setBackground( Colors.jsStringToColor( a ) ) }
    general
      .addRange( jsCfg, "Downsampling", 0, 7 )
      .onChange { _: Float => scene.udpateDownsampling() }
    general
      .addRange( jsCfg, "Explosion", 0, 100 )
    general.open()

    val cells = datGUI.addFolder( "Cells" )

    cells
      .addBoolean( jsCfg, "Draw cells" )
      .onChange { _: Boolean => scene.toggleCells() }
    cells
      .addRange( jsCfg, "Borders width", 0, 2, 0.1f )
    cells
      .addColor( jsCfg, "Borders color" )
    cells
      .addRange( jsCfg, "Thickness", 0, 25 )
    cells.open()

    val mazeFolder = datGUI.addFolder( "Maze" )
    mazeFolder
      .addBoolean( jsCfg, "Draw path" )
      .onChange { _: Boolean => scene.toggleMazePath() }
    mazeFolder
      .addColor( jsCfg, "Path color" )
    mazeFolder
      .addRange( jsCfg, "Maze depth scaling", -100, 100 )
      .onChange { v: Float => () }
    mazeFolder.open()

    val colors = datGUI.addFolder( "Coloring" )
    colors.open()
    implicit val colorsParams = datGUI.addFolder( "Coloring parameters" )
    colorsParams.open()

    implicit val paletteController = colors.addList( jsCfg, "Palette", Config.colorings )

    applyColors( Seq.empty )

    datGUI.open()

  }

  @JSExport
  def initRnd( seed: Long ): Unit = {
    rnd = new Random( seed )
  }

  @JSExport
  def loadModel( cutCount: Int, mazeType: String ): Unit = {
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
    maze = mazeType.toLowerCase match {
      case "depthfirst" => Maze.depthFirstMaze( model.faces)
      case "randomtraversal" => Maze.randomTraversal( model.faces )
      case "prim" => Maze.prim( model.faces )
      case _ => Maze.wilsonMaze( model.faces )
    }
//    println( maze.toNiceString() )
    val mazeMetrics = maze.metrics
    depthMap = mazeMetrics._1
    depthMax = mazeMetrics._2

    val t2 = System.currentTimeMillis()

    println( "cut time", t1 - t0 )
    println( "maze time", t2 - t1 )

    scene.addModel( model, maze, depthMap, depthMax )

    main()
  }

  @JSExport
  def faceInfo( id: Int ): Unit = {
    val trueId = ThreeScene.revertColorCode( id )
    model.faces.lift( trueId ).foreach { f =>
      println( s"Face id: $trueId" )
      println( s"Face depth: ${depthMap( trueId )}" )
      println( s"Face neighbours: ${f.neighbours}" )
    }
  }

  // ******************** coloring ********************

  private def fullRndColor(): Unit = {
    import Colors.intColorToFloatsColors
    colorModel( { _: ( Int, Int ) => Colors.rndColor() }, Some { _: ( Int, Int ) => Colors.rndColor() } )
  }

  private def rndColor(): Unit = {
    import Colors.intColorToFloatsColors
    colorModel( { _: ( Int, Int ) => Colors.rndColor() } )
  }

  private def color( c0: Int, c1: Int ): Unit = {
    import Colors.intColorToFloatsColors
    colorModel( { _: ( Int, Int ) => c0 }, Some { _: ( Int, Int ) => c1 } )
  }

  private def rainbow( reverse: Boolean )( rainbowFct: Float => Float => ( Float, Float, Float ) ): Unit = {
    val rnbw = rainbowFct( 1f / config.safeRainbowSpan )
      .compose { f: Float => if( reverse ) 1f - f else f }
      .compose { idAndDepth: ( Int, Int ) => idAndDepth._2.toFloat / depthMax }
    colorModel( rnbw )
  }

  private def colorModel( color: ( ( Int, Int ) ) => ( Float, Float, Float )
                        , centerColor: Option[( ( Int, Int ) ) => ( Float, Float, Float )] = None ): Unit = {
    ThreeScene.withOffsetsAndSizes( model.faces )
      .zipWithIndex
      .foreach { case ( ( f, o, s ), id ) =>
        val d = depthMap( id )
        val col = color( ( id, d ) )
        val cCol = centerColor match {
          case Some( fct ) =>
            fct( ( id, d ) )
          case _ =>
            col
        }
        scene.colorFace( o, s, col, cCol )
      }
  }
}
