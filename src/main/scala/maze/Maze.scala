package maze

import scala.annotation.tailrec

/**
 * Created by markus on 07/07/2015.
 */
trait Maze[T] {
  def value: T
  def depth: Int
  def branches: Seq[Maze[T]]

  def size: Int = branches.foldLeft( 1 ){ _ + _.size }

  // returns a map of each node and its depth, the min depth, the max depth
  def toDepthMap: ( Map[T, Int], Int, Int ) = toDepthMap0( Seq( this ), Map.empty, Int.MaxValue, Int.MinValue )
  @tailrec
  private def toDepthMap0( at: Seq[Maze[T]], acc: Map[T, Int], min: Int, max: Int ): ( Map[T, Int], Int, Int ) = at match {
    case Nil =>
      ( acc, min, max )
    case h :: t =>
      val d = h.depth
      toDepthMap0( h.branches ++ t, acc + ( ( h.value, d ) ), math.min( min, d ), math.max( max, d ) )
  }
}
