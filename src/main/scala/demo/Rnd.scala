package demo

import scala.util.Random

/**
 * Created by markus on 17/06/2015.
 */
object Rnd {
  private val rnd = new Random

  def rndInt(): Int = rnd.nextInt()

  def rndUniformDouble(): Double = rnd.nextDouble()
}
