package demo.webapp

import demo.Colors

import scala.language.implicitConversions

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport

/**
 * Created by markus on 10/07/15.
 */
object Config {
  implicit def colorIntToJsArray( color: Int ): js.Array[Float] = Colors.intColorToIntColors( color ).map( _.toFloat ).toJSArray

  val uniforms = "Uniform colors"
  val randoms = "Random colors"
  val chRainbow = "Cubehelix rainbow"
  val niRainbow = "Niccoli's rainbow"
  val laRainbow = "Less Angry rainbow"
  val colorings: js.Array[String] =
    Array( uniforms
         , randoms
         , chRainbow
         , niRainbow
         , laRainbow
         ).toJSArray
}

import Config._

class Config {

  @JSExport
  var `Show axis`: Boolean = false

  @JSExport
  var `Cull back`: Boolean = false


  // 0 <= downsampling <= 7
  @JSExport
  var `Downsampling`: Int = 0
  def safeDownsamplingFactor = math.pow( 2, math.max( 0, math.min( 7, `Downsampling` ) ) ).toInt

  // 0 <= explosion <= 100
  @JSExport
  var `Explosion`: Int = 0
  def safeExplosionFactor: Float = {
    val f = math.min( 100, math.max( 0, `Explosion` ) ).toFloat / 100
    f*f*10f
  }

  // 0 <= bordersWidth <= 2
  @JSExport
  var `Borders width`: Float = 1f
  def safeBordersWidth = math.min( 2f, math.max( 0f, `Borders width` ) )

  // color
  @JSExport
  var `Borders color`: js.Array[Float] = Colors.LIGHT_BLACK

  /*
  @JSExport
  var drawMaze: Boolean = false
  @JSExport
  var mazeColor: Int = 0x00ff00
  */

  // -100 <= mazeDepthScale <= 100
  @JSExport
  var `Maze depth scaling`: Int = -25
  def mazeDepthFactor: Float = math.min( 100, math.max( -100, `Maze depth scaling` ) ).toFloat / 100

  @JSExport
  var `Palette` = laRainbow

  @JSExport
  var `Color 0`: js.Array[Float] = Colors.WHITE
  @JSExport
  var `Color 1`: js.Array[Float] = Colors.WHITE

  // 0.1 <= rainbow span <= 2
  @JSExport
  var `Rainbow span`: Float = 1f
  def safeRainbowSpan: Float = math.max( 0.01f, math.min( 2, `Rainbow span` ) )

  @JSExport
  var `Reverse palette`: Boolean = false
}