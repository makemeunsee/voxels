package demo.webapp

import demo.Colors

import scala.annotation.meta.field
import scala.language.implicitConversions

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExport
import scala.util.Try

/**
 * Created by markus on 10/07/15.
 */
object Config {

  val uniforms = "Uniform colors"
  val randoms = "Random colors"
  val randoms2 = "Random colors (flat)"
  val chRainbow = "Cubehelix rainbow"
  val niRainbow = "Niccoli's rainbow"
  val laRainbow = "Less Angry rainbow"
  val grayscale = "Grayscale"
  val colorings: js.Array[String] =
    Array( uniforms
         , randoms
         , randoms2
         , chRainbow
         , niRainbow
         , laRainbow
         , grayscale
         ).toJSArray

  val wilson = "Wilson's"
  val depthFirst = "Depth first"
  val prim = "Prim's"
  val rndTraversal = "Random traversal"
  val mazeTypes: js.Array[String] =
    Array( wilson
         , depthFirst
         , prim
         , rndTraversal
         ).toJSArray

  val spherical = "Spherical"
  val hammerAitoff = "Hammer-Aitoff"
  val projections: js.Array[String] =
    Array( spherical, hammerAitoff ).toJSArray

  val presets = """{
                  |  "preset": "path2",
                  |  "closed": false,
                  |  "remembered": {
                  |    "Default": {
                  |      "0": {
                  |        "Show axes": false,
                  |        "Background color": "#000000",
                  |        "Downsampling": 0,
                  |        "Explosion": 0,
                  |        "Draw cells": true,
                  |        "Borders width": 1,
                  |        "Borders color": "#181111",
                  |        "Thickness": 4,
                  |        "Draw path": false,
                  |        "Path color": "#00ff00",
                  |        "Maze depth scaling": 20,
                  |        "Palette": "Less Angry rainbow",
                  |        "Rainbow span": 100,
                  |        "Reverse palette": false,
                  |        "Color 0": "#000000",
                  |        "Color 1": "#000000"
                  |      }
                  |    },
                  |    "path": {
                  |      "0": {
                  |        "Show axes": false,
                  |        "Background color": "#000000",
                  |        "Downsampling": 0,
                  |        "Explosion": 0,
                  |        "Draw cells": false,
                  |        "Borders width": 0,
                  |        "Borders color": "#0d0d0d",
                  |        "Thickness": 0,
                  |        "Draw path": true,
                  |        "Path color": "#00ff00",
                  |        "Maze depth scaling": 0,
                  |        "Palette": "Uniform colors",
                  |        "Rainbow span": 100,
                  |        "Reverse palette": false,
                  |        "Color 0": "#000000",
                  |        "Color 1": "#000000"
                  |      }
                  |    },
                  |    "flat": {
                  |      "0": {
                  |        "Show axes": false,
                  |        "Background color": "#000000",
                  |        "Downsampling": 0,
                  |        "Explosion": 0,
                  |        "Draw cells": true,
                  |        "Borders width": 0,
                  |        "Borders color": "#0d0d0d",
                  |        "Thickness": 0,
                  |        "Draw path": false,
                  |        "Path color": "#00ff00",
                  |        "Maze depth scaling": 0,
                  |        "Palette": "Cubehelix rainbow",
                  |        "Rainbow span": 100,
                  |        "Reverse palette": false,
                  |        "Color 0": "#000000",
                  |        "Color 1": "#000000"
                  |      }
                  |    },
                  |    "pixels": {
                  |      "0": {
                  |        "Show axes": false,
                  |        "Background color": "#000000",
                  |        "Downsampling": 3,
                  |        "Explosion": 0,
                  |        "Draw cells": true,
                  |        "Borders width": 0,
                  |        "Borders color": "#181111",
                  |        "Thickness": 0,
                  |        "Draw path": false,
                  |        "Path color": "#00ff00",
                  |        "Maze depth scaling": 0,
                  |        "Palette": "Niccoli's rainbow",
                  |        "Rainbow span": 100,
                  |        "Reverse palette": false,
                  |        "Color 0": "#000000",
                  |        "Color 1": "#000000"
                  |      }
                  |    },
                  |    "bright": {
                  |      "0": {
                  |        "Show axes": false,
                  |        "Background color": "#ffffff",
                  |        "Downsampling": 0,
                  |        "Explosion": 0,
                  |        "Draw cells": true,
                  |        "Borders width": 1.4776700735092163,
                  |        "Borders color": "#ffffff",
                  |        "Thickness": 7.71917200088501,
                  |        "Draw path": false,
                  |        "Path color": "#00ff00",
                  |        "Maze depth scaling": 0,
                  |        "Palette": "Uniform colors",
                  |        "Rainbow span": 100,
                  |        "Reverse palette": false,
                  |        "Color 0": "#000000",
                  |        "Color 1": "#000000"
                  |      }
                  |    },
                  |    "heightmap": {
                  |      "0": {
                  |        "Show axes": false,
                  |        "Background color": "#000000",
                  |        "Downsampling": 0,
                  |        "Explosion": 0,
                  |        "Draw cells": true,
                  |        "Borders width": 0,
                  |        "Borders color": "#000000",
                  |        "Thickness": 0,
                  |        "Maze type": "Prim's",
                  |        "Draw path": false,
                  |        "Path color": "#00ff00",
                  |        "Maze depth scaling": 0,
                  |        "Palette": "Grayscale",
                  |        "Rainbow span": 50,
                  |        "Reverse palette": false
                  |      }
                  |    }
                  |  },
                  |  "folders": {
                  |    "General": {
                  |      "preset": "Default",
                  |      "closed": false,
                  |      "folders": {}
                  |    },
                  |    "Cells": {
                  |      "preset": "Default",
                  |      "closed": false,
                  |      "folders": {}
                  |    },
                  |    "Maze": {
                  |      "preset": "Default",
                  |      "closed": false,
                  |      "folders": {}
                  |    },
                  |    "Coloring": {
                  |      "preset": "Default",
                  |      "closed": false,
                  |      "folders": {}
                  |    },
                  |    "Coloring parameters": {
                  |      "preset": "Default",
                  |      "closed": false,
                  |      "folders": {}
                  |    }
                  |  }
                  |}""".stripMargin
}

case class Config (

  @(JSExport @field)
  var `Projection`: String = Config.hammerAitoff,

  @(JSExport @field)
  var `Seed (!slow!)`: String = System.currentTimeMillis().toString,

  @(JSExport @field)
  var `Count (!slow!)`: String = "5000",

  @(JSExport @field)
  var `Background color`: String = Colors.colorIntToJsString( Colors.BLACK ),

  // 0 <= downsampling <= 7
  @(JSExport @field) 
  var `Downsampling`: Int = 0,


  @(JSExport @field) 
  var `Draw cells`: Boolean = true,


  // 0 <= bordersWidth <= 2
  @(JSExport @field) 
  var `Borders width`: Float = 1f,

  // color
  @(JSExport @field) 
  var `Borders color`: String = Colors.colorIntToJsString( Colors.LIGHT_BLACK ),

  @(JSExport @field)
  var `Maze type`: String = Config.prim,

  @(JSExport @field)
  var `Draw path`: Boolean = false,

  @(JSExport @field) 
  var `Path color`: String = Colors.colorIntToJsString( Colors.GREEN ),


  @(JSExport @field) 
  var `Palette`: String = Config.laRainbow,

  @(JSExport @field) 
  var `Color 0`: String = Colors.colorIntToJsString( Colors.WHITE ),
  @(JSExport @field) 
  var `Color 1`: String = Colors.colorIntToJsString( Colors.WHITE ),

  // 0.1 <= rainbow span <= 2
  @(JSExport @field) 
  var `Rainbow span`: Float = 100f,

  @(JSExport @field) 
  var `Reverse palette`: Boolean = false
) {

  def safeCutCount: Int = cutCount.getOrElse( 994 )

  def cutCount: Try[Int] =
    Try( math.min( 10000, math.max( 0, Integer.parseInt( `Count (!slow!)` )-6 ) ) )

  def safeDownsamplingFactor = math.pow( 2, math.max( 0, math.min( 7, `Downsampling` ) ) ).toInt

  def safeBordersWidth = math.min( 2f, math.max( 0f, `Borders width` ) )

  def safeRainbowSpan: Float = {
    val f = math.max( 1f, math.min( 100f, `Rainbow span` ) ) / 100
    f * f
  }
}