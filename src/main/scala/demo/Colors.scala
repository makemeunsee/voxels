package demo

import scala.language.implicitConversions
import scala.util.Random

/**
 * Created by markus on 22/06/2015.
 */
object Colors {
  implicit def intColorToFloatsColors( c: Int ): ( Float, Float, Float ) = {
    val r = ( ( c >> 16 ) & 0xff ).toFloat / 255f
    val g = ( ( c >> 8 ) & 0xff ).toFloat / 255f
    val b = ( c & 0xff ).toFloat / 255f
    ( r, g, b )
  }

  def rndColor()( implicit rnd: Random ): Int = rnd.nextInt() & WHITE

  val WHITE = 0xFFFFFF
  val BLACK = 0x000000

  // 0 <= t <= 1
  def cubeHelixInterpolationFct( gamma: Float, h0: Float, s0: Float, l0: Float, h1: Float, s1: Float, l1: Float )
                               ( t: Float ): ( Float, Float, Float ) = {
    val startH = ( h0 + 120f ) * math.Pi / 180
    val diffH = ( h1 + 120f ) * math.Pi / 180 - startH
    val diffS = s1 - s0
    val diffL = l1 - l0
    val h = startH + diffH * t
    val l = math.pow( l0 + diffL * t, gamma )
    val a = ( s0 + diffS * t ) * l * ( 1f - l )
    val cosh = math.cos( h )
    val sinh = math.sin( h )
    ( ( l + a * (-0.14861 * cosh + 1.78277 * sinh ) ).toFloat
    , ( l + a * (-0.29227 * cosh - 0.90649 * sinh ) ).toFloat
    , ( l + a * 1.97294 * cosh  ).toFloat )
  }

  // less angry rainbow, from: http://bl.ocks.org/mbostock/310c99e53880faec2434
  private def lessAngry0 = cubeHelixInterpolationFct( 1, -100, 0.75f, 0.35f, 80,  1.5f,  0.8f) _
  private def lessAngry1 = cubeHelixInterpolationFct( 1, 80,   1.5f,  0.8f,  260, 0.75f, 0.3f) _
  // hues: how many hues in the rainbow
  // freq: rainbow repetition frequency. for instance, 'length' 0.5 yields 2 full rainbows when 'at' ranges from 0 to 1
  // 0 <= at <= 1
  def lessAngryRainbow( hues: Int, freq: Float )( at: Float ) = {
    val half = hues / 2f
    val d = ( freq * at * hues ) % hues
    if ( d < half )
      lessAngry0( d / half )
    else
      lessAngry1( ( d - half ) / half )
  }

  def cubeHelixRainbow = cubeHelixInterpolationFct( 1, -240, 0.5f, 1, 300, 0.5f, 0 ) _

  def matteoNiccoliRainbow = cubeHelixInterpolationFct( 1, 300, 5f/12, 0.1f, 60, 25f/24, 0.9f ) _
}
