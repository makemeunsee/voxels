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
}
