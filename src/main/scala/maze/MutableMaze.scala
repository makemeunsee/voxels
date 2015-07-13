package maze

import scala.annotation.tailrec

/**
 * Created by markus on 13/07/15.
 */

class MutableNode[T]( val value: T, val depth: Int = 0, var branches: Seq[MutableNode[T]] = Seq.empty ) extends Maze[T] {
  // do not use, use MutableMaze.plug
  def plug( branch: Seq[T] ): Maze[T] = ???
}

class MutableMaze[T]( value: T ) extends MutableNode[T]( value ) {

  private var nodesMap: Map[T, MutableNode[T]] = Map( ( value, this ) )

  override def plug( branch: Seq[T] ): Maze[T] = branch match {
    // plug nothing, returns self
    case Nil =>
      this
    // plug just 1 element, returns self
    case h :: Nil =>
      this
    // plug an actual branch, find the plug point and add the branch
    case h :: t =>
      nodesMap.get( h ).foreach { node =>
        val newBranch = toMazes( node.depth+1, t, Seq.empty )
        node.branches = newBranch.head +: node.branches
        registerNewBranch( newBranch )
      }
      this
  }

  @tailrec
  private def registerNewBranch( branch: Seq[MutableNode[T]] ): Unit = branch match {
    case x :: y :: t =>
      x.branches = y +: x.branches
      nodesMap = nodesMap + ( ( x.value, x ) )
      registerNewBranch( y +: t )
    case x :: Nil =>
      nodesMap = nodesMap + ( ( x.value, x ) )
    case Nil =>
      ()
  }

  @tailrec
  private def toMazes( depth: Int, branch: Seq[T], acc: Seq[MutableNode[T]] ): Seq[MutableNode[T]] = branch match {
    // branch built backward, reverse it
    case Nil =>
      acc.reverse
    // turn each branch element into a disconnected maze node
    case h :: t =>
      toMazes( depth+1, t, new MutableNode( h, depth, Seq.empty ) +: acc )
  }
}
