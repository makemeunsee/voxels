package maze

import scala.annotation.tailrec

/**
 * Created by markus on 13/07/15.
 */
case class ImmutableMaze[T]( value: T, depth: Int = 0, branches: Seq[Maze[T]] = Seq.empty ) extends Maze[T] {
  // slow: O( size of maze )
  def plug( branch: Seq[T] ): Maze[T] = branch match {
    // plug nothing, returns self
    case Nil =>
      this
    // plug just 1 element, returns self
    case h :: Nil =>
      this
    // plug an actual branch, find the plug point and add the branch
    case h :: t if value == h =>
      // plug the branch here
      copy( branches = toMaze( depth+1, t, Seq.empty ) +: branches )
    case _ =>
      // look further to plug the branch
      copy( branches = branches.map( _.plug( branch ) ) )
  }

  @tailrec
  private def toMaze( depth: Int, branch: Seq[T], acc: Seq[ImmutableMaze[T]] ): Maze[T] = branch match {
    // dont accept empty branches
    case Nil =>
      throw new Error( "Illegal argument" )
    // on the last element of the branch, link the maze nodes together
    case h :: Nil =>
      acc.foldLeft( ImmutableMaze( h, depth, Seq.empty ) ) { case ( child, parent ) =>
        parent.copy( branches = Seq( child ) )
      }
    // turn each branch element into a disconnected maze node
    case h :: t =>
      toMaze( depth+1, t, ImmutableMaze( h, depth, Seq.empty ) +: acc )
  }
}
