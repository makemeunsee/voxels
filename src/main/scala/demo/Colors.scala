package demo

import scala.language.implicitConversions

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

  def rndColor(): Int = Rnd.rndInt() & WHITE

  val WHITE = 0xFFFFFF
}
