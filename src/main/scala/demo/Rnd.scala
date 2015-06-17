package demo

import scala.util.Random

/**
 * Created by markus on 17/06/2015.
 */
object Rnd {
  private val rnd = new Random

  def rndColor(): ( Float, Float, Float ) = {
    val rndInt = rnd.nextInt()
    val r = ( ( rndInt >> 16 ) & 0xff ).toFloat / 255f
    val g = ( ( rndInt >> 8 ) & 0xff ).toFloat / 255f
    val b = ( rndInt & 0xff ).toFloat / 255f
    ( r, g, b )
  }
}
