package demo.webapp

/**
 * Created by markus on 16/02/2015.
 */

import demo.Colors
import geometry.Normal3
import geometry.voronoi.VoronoiModel
import maze.Maze

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSName, JSExport}
import scala.scalajs.js.{JSON, JSApp}
import scala.util.{Failure, Success, Random}

@JSName("jQuery")
@js.native
object JQuery extends js.Object {
  def apply(x: String): js.Dynamic = js.native
}

object VoxelMain extends JSApp {

  private var model: VoronoiModel = VoronoiModel.cubeModel
  private var depthMap: Map[Int, Int] = Map.empty
  private var depthMax = 0

  private val config = new Config
  private val datGUI = new DatGUI( js.Dynamic.literal( "load" -> JSON.parse( Config.presets ), "preset" -> "Default" ) )
  private var cutCount = config.safeCutCount
  private var seed: Long = 0
  private var mazeType: String = config.`Maze type`
  private implicit var iRnd = new Random( 0 )
  initRnd( config.`Seed (!slow!)` )


  // ******************** actual three.js scene ********************

  @JSExport
  val scene = new ThreeScene( config )

  // ******************** DatGUI menu controllers ********************

  private def rainbowControllers( rainbowFct: Float => Float => ( Float, Float, Float ) )
                                ( implicit colorParams: DatGUI
                                , jsCfg: js.Dynamic
                                , paletteController: DatController[String] ): Seq[DatController[_]] = {
    val spanController = colorParams.addRange( jsCfg, "Rainbow span", 1f, 100f ).step( 1 )
    val reverseController = colorParams.addBoolean( jsCfg, "Reverse palette" )
    val newControllers = Seq( spanController, reverseController )
    spanController.onChange{ _: Float => rainbow( config.`Reverse palette` )( rainbowFct ) }
    reverseController.onChange{ _: Boolean => rainbow( config.`Reverse palette` )( rainbowFct ) }
    newControllers
  }

  // apply color palette and dynamically load relevant parameters in the gui
  private def updateColorControllers( colorControllers: Seq[DatController[_]] )
                                    ( implicit colorParams: DatGUI, paletteController: DatController[String] ): Unit = {

    implicit val jsCfg = config.asInstanceOf[js.Dynamic]

    // palette has change, remove all parameters from the gui
    colorControllers.foreach( colorParams.remove )

    val newControllers = config.`Palette` match {

      case Config.uniforms =>
        val col0Controller = colorParams.addColor( jsCfg, "Color 0" )
        val col1Controller = colorParams.addColor( jsCfg, "Color 1" )
        val result = Seq( col0Controller, col1Controller )
        import Colors.jsStringToColor
        col0Controller.onChange { _: String => color( config.`Color 0`, config.`Color 1` ) }
        col1Controller.onChange { _: String => color( config.`Color 0`, config.`Color 1` ) }
        result

      case Config.randoms =>
        Seq.empty

      case Config.randoms2 =>
        Seq.empty

      case Config.chRainbow =>
        rainbowControllers( Colors.cubeHelixRainbow )

      case Config.niRainbow =>
        rainbowControllers( Colors.matteoNiccoliRainbow )

      case Config.laRainbow =>
        rainbowControllers( Colors.lessAngryRainbow )

      case Config.grayscale =>
        rainbowControllers( Colors.grayscale )

      case _ =>
        Seq.empty
    }
    // update palette controller with new parameter controllers
    paletteController.onChange { _: String =>
      applyColors()
      updateColorControllers( newControllers )
    }
  }

  // ******************** init code ********************

  // scala.js wants it...
  def main(): Unit = {}

  // magic number... more than 400 and "too much recursion" errors occur on firefox
  private val step: Int = 400

  def setupDatGUI( jsCfg: js.Dynamic
                 , uiToggler: js.Function1[Boolean, _]
                 , continuator: js.Function1[js.Function0[_], _]
                 , continuation: js.Function0[_] ): Unit = {

    val general = datGUI.addFolder( "General" )

    general
      .addList( jsCfg, "Projection", Config.projections )
      .onFinishChange ( scene.setShader _ )

    general
      .addRange( jsCfg, "Mega texture", Config.minSupportedTextureSize, scene.maxTextureSize ).step( Config.minSupportedTextureSize )

    general
      .addString( jsCfg, "Seed (!slow!)" )
      .onFinishChange { str: String =>
        if ( initRnd( str ) ) {
          loadModel( uiToggler
                   , continuator
                   , continuation
                   , initDatGUI = false )
        } else
          ()
      }
    general
      .addColor( jsCfg, "Background color" )
      .onChange { a: String => scene.setBackground( Colors.jsStringToColor( a ) ) }
    general
      .addRange( jsCfg, "Downsampling", 0, 7 ).step( 1 )
      .onChange { _: Float => scene.udpateDownsampling() }
    general.open()

    val cells = datGUI.addFolder( "Cells" )

    cells
      .addBoolean( jsCfg, "Draw cells" )
      .onChange { _: Boolean => scene.toggleCells() }
    cells
      .addString( jsCfg, "Count (!slow!)" )
      .onFinishChange { _: String =>
        config.cutCount match {
          case Success( c ) =>
            if ( c != cutCount )
              loadModel( uiToggler
                       , continuator
                       , continuation
                       , initDatGUI = false )
          case Failure( _ ) =>
            import scala.scalajs.js.Dynamic.global
            global.alert( "Positive integer expected!" )
            ()
        }
      }
    cells
      .addRange( jsCfg, "Borders width", 0, 2 ).step( 0.1f )
    cells
      .addColor( jsCfg, "Borders color" )
    cells.open()

    val mazeFolder = datGUI.addFolder( "Maze" )
    mazeFolder
      .addList( jsCfg, "Maze type", Config.mazeTypes )
      .onFinishChange
        { _: String =>
          val size = model.faces.length
          val vStep = math.min( step, size / 100 )
          if ( mazeType != config.`Maze type` ) {
            mazeType = config.`Maze type`
            val progressBar = JQuery( "#progressbar" )
            val progressLabel = JQuery( "#progress-label" )
            progressBar.show()
            reloadMaze( { ( recStep, actualProgress, nextStep ) =>
              if ( recStep % vStep == 0 ) {
                val v = 100*actualProgress/size
                progressBar.progressbar( "value", v )
                progressLabel.text( s"Regenerating maze - $v %" )
                continuator( () => nextStep )
              } else
                nextStep
            }, { _ =>
              progressBar.hide()
            } )
          }
        }
    mazeFolder
      .addBoolean( jsCfg, "Draw path" )
      .onChange { _: Boolean => scene.toggleMazePath() }
    mazeFolder
      .addColor( jsCfg, "Path color" )
    mazeFolder.open()

    val colors = datGUI.addFolder( "Coloring" )
    colors.open()
    implicit val colorsParams = datGUI.addFolder( "Coloring parameters" )
    colorsParams.open()

    implicit val paletteController = colors.addList( jsCfg, "Palette", Config.colorings )

    updateColorControllers( Seq.empty )

    datGUI.open()
  }

  private def initRnd( seedStr : String ): Boolean = {
    val newSeed: Long =
      try java.lang.Long.parseLong( seedStr )
      catch { case e: NumberFormatException => seedStr.hashCode }
    if ( newSeed != seed ) {
      seed = newSeed
      println( s"seed: $seed from string: $seedStr" )
      iRnd = new Random( seed )
      true
    } else
      false
  }

  private def reloadMaze( progressHandler: ( Int, Int, => Unit ) => Unit
                        , continuation: Maze[Int] => Unit ): Unit = {
    genMazeStructure( progressHandler, { result =>
      genMazeMetrics( result )
      applyMaze( result )
      continuation( result )
    } )
  }

  private def genMazeStructure( progressHandler: ( Int, Int, => Unit ) => Unit
                              , continuation: Maze[Int] => Unit ): Unit = {
    // always use the original random to generate the maze => get the same mazes
    // generate maze
    val mazeGenerator = config.`Maze type` match {
      case Config.wilson       => Maze.wilsonMaze[Unit] _
      case Config.rndTraversal => Maze.randomTraversal[Unit] _
      case Config.depthFirst   => Maze.depthFirstMaze[Unit] _
      case _ =>                   Maze.prim[Unit] _
    }
    mazeGenerator( new Random( seed ), model.faces, progressHandler, continuation )
    //    println( maze.toNiceString() )
  }

  private def genMazeMetrics( maze: Maze[Int] ): Unit = {
    val mazeMetrics = maze.metrics
    depthMap = mazeMetrics._1
    depthMax = mazeMetrics._2
  }

  def applyMaze( maze: Maze[Int] ): Unit = {
    scene.setMaze( model, maze )
    applyColors()
  }

  @JSExport
  def loadModel( uiToggler: js.Function1[Boolean, _]
               , frameHandler: js.Function1[js.Function0[_], _]
               , continuation: js.Function0[_]
               , initDatGUI: Boolean = true  ): Unit = {

    // reset model
    model = VoronoiModel.cubeModel

    cutCount = config.safeCutCount
    println( s"cut count $cutCount" )

    import js.DynamicImplicits._
    val progressBar = JQuery( "#progressbar" )
    val progressLabel = JQuery( "#progress-label" )

    uiToggler( false )
    progressBar.show()

    val jsCfg = config.asInstanceOf[js.Dynamic]

    var cutNormals: Seq[( Normal3 )] = Seq.empty

    val colors: js.Function0[_] = () => {
      applyColors()
      if ( initDatGUI )
        setupDatGUI( jsCfg, uiToggler, frameHandler, continuation )

      progressBar.progressbar( "value", 100 )
      progressLabel.text( s"Loading... (color scheme) - 100 %" )

      progressBar.hide()
      uiToggler( true )

      frameHandler( continuation )
    }

    val intoScene: Maze[Int] => js.Function0[_] = { maze: Maze[Int] => () => {
      scene.setModel( model, maze )
      progressBar.progressbar( "value", 99 )
      progressLabel.text( s"Loading... (meshes) - 99 %" )
      frameHandler( colors )
    } }

    val mazeMetricsGen: Maze[Int] => js.Function0[_] =  { maze: Maze[Int] => () => {
      genMazeMetrics( maze )
      progressBar.progressbar( "value", 95 )
      progressLabel.text( s"Loading... (maze metrics) - 95 %" )
      frameHandler( intoScene( maze ) )
    } }

    val mazeGen: js.Function0[_] = () => {
      val size = model.faces.length
      val vStep = math.min( step, size / 20 )
      genMazeStructure( { ( recStep, actualProgress, nextStep ) =>
        if ( recStep % vStep == 0 ) {
          val v = 75 + 20*actualProgress/size
          progressBar.progressbar( "value", v )
          progressLabel.text( s"Loading... (maze) - $v %" )
          frameHandler( () => nextStep )
        } else
          nextStep
      }, { maze => mazeMetricsGen( maze )() } )
    }

    val cuts: js.Function0[_] = () => {
      val size = cutNormals.length
      val vStep = math.min( step, size / 75 )
      model.cut( cutNormals, { ( i, nextCut ) =>
        if ( i % vStep == 0 ) {
          val v = 75*i/size
          progressBar.progressbar( "value", v )
          progressLabel.text( s"Loading... (generating cells) - $v %" )
          frameHandler( () => nextCut )
        } else
          nextCut
      }, mazeGen )
    }

    val prepare: js.Function0[_] = () => {
      // load config template
      datGUI.remember( jsCfg )

      val rnd = new Random( seed )
      // prepare cuts
      cutNormals = ( 0 until cutCount ).map { _ =>
        val ( theta, phi ) = geometry.uniformToSphericCoords( rnd.nextDouble(), rnd.nextDouble() )
        Normal3.fromSphericCoordinates( theta, phi )
      }

      progressBar.progressbar( "value", 0 )
      progressLabel.text( "Loading... (cuts) - 0 %" )

      frameHandler( cuts )
    }

    prepare()
  }

  // ******************** coloring ********************

  private def applyColors(): Unit = {
    config.`Palette` match {

      case Config.uniforms =>
        import Colors.jsStringToColor
        color( config.`Color 0`, config.`Color 1` )

      case Config.randoms =>
        fullRndColor()

      case Config.randoms2 =>
        rndColor()

      case Config.chRainbow =>
        rainbow( config.`Reverse palette` )( Colors.cubeHelixRainbow )

      case Config.niRainbow =>
        rainbow( config.`Reverse palette` )( Colors.matteoNiccoliRainbow )

      case Config.laRainbow =>
        rainbow( config.`Reverse palette` )( Colors.lessAngryRainbow )

      case Config.grayscale =>
        rainbow( config.`Reverse palette` )( Colors.grayscale )

      case _ =>
        ()
    }
  }

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
        val d = depthMap.getOrElse( id, 0 )
        val col = color( ( id, d ) )
        val cCol = centerColor match {
          case Some( fct ) =>
            fct( ( id, d ) )
          case _ =>
            col
        }
        scene.updateFaceColor( o, s, col, cCol )
      }
  }
}
