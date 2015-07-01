package demo

import scala.util.Random

/**
 * Created by markus on 17/06/2015.
 */
class Rnd( seed: Long ) {
  private val rnd = new Random( seed )

  def rndInt(): Int = rnd.nextInt()

  def rndUniformDouble(): Double = rnd.nextDouble()
}
