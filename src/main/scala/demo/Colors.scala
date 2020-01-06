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

  def rndColor(): Int = PRIMARIES(Rnd.nextInt(4))

  val WHITE = 0xFFFFFF
  val RED = 0xFF0000
  val GREEN = 0x00FF00
  val YELLOW = 0xFFFF00
  val BLUE = 0x0000FF

  val PRIMARIES = Seq(RED, GREEN, YELLOW, BLUE)
}
