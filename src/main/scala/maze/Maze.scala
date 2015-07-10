package maze

import geometry.voronoi.Face

import scala.annotation.tailrec
import scala.util.Random

/**
 * Created by markus on 07/07/2015.
 */
object Maze {
  @tailrec
  private def toDepthMap0[T]( at: Seq[Maze[T]], acc: Map[T, Int], max: Int ): ( Map[T, Int], Int) = at match {
    case Nil =>
      ( acc, max )
    case h :: t =>
      val d = h.depth
      toDepthMap0( h.branches ++ t, acc + ( ( h.value, d ) ), math.max( max, d ) )
  }

  @tailrec
  private def childrenMap[T]( remaining: Seq[Maze[T]], acc: Map[( T, Int ), Set[( T, Int )]] ): Map[( T, Int ), Set[( T, Int )]] = remaining match {
    case Nil => acc
    case h :: t =>
      val newRem = h.branches ++ t
      val newAcc = acc + ( ( ( h.value, h.depth ), h.branches.map( m => ( m.value, m.depth ) ).toSet ) )
      childrenMap( newRem, newAcc )
  }

  def depthFirstMaze( faces: Array[Face] )( implicit rnd: Random ): Option[Maze[Int]] = {
    if ( faces.isEmpty )
      None
    else {
      val id0 = rnd.nextInt( faces.length )
      Some( depthFirstMaze0( Set( id0 ), Seq.empty, Seq( ( id0, 0 ) ) )( faces, rnd ) )
    }
  }
  @tailrec
  private def depthFirstMaze0( visited: Set[Int], acc: Seq[Maze[Int]], parents: Seq[( Int, Int )])
                             ( implicit faces: Array[Face], rnd: Random ): Maze[Int] = parents match {
    case Nil =>
      acc.head
    case ( id, depth ) :: rParents =>
      val explorable = faces( id ).neighbours.diff( visited ).toArray
      if ( explorable.isEmpty ) {
        val newAcc = acc match {
          case Nil =>
            Seq( MazeImpl( id, depth, Seq.empty ) )
          case _   =>
            val ( tails, parallelBranches ) = acc.partition( m => m.depth == depth+1 )
            tails match {
              case Nil =>
                MazeImpl( id, depth, Seq.empty ) +: acc
              case _ =>
                MazeImpl( id, depth, tails ) +: parallelBranches
            }
        }
        depthFirstMaze0( visited, newAcc, rParents )
      } else {
        val l = explorable.length
        val newId = rnd.nextInt( l )
        val j = explorable( newId )
        val newVisited = visited + j
        depthFirstMaze0( newVisited, acc, ( j, depth+1 ) +: ( id, depth ) +: rParents )
      }
  }
}

import maze.Maze._

trait Maze[T] {
  def value: T
  def depth: Int
  def branches: Seq[Maze[T]]

  def size: Int = branches.foldLeft( 1 ){ _ + _.size }

  // returns a map of each node and its depth, the min depth, the max depth
  def toDepthMap: ( Map[T, Int], Int ) = toDepthMap0( Seq( this ), Map.empty, 0 )

  // map each (value, depth) pair to its parent and children
  def relativesMap: Map[( T, Int ), Set[( T, Int )]] = {
    val children = childrenMap( Seq( this ), Map.empty: Map[( T, Int ), Set[( T, Int )]] )
    children.foldLeft( children ) { case ( oldNeighboursMap, ( parent, itsChildren ) ) =>
      // add the parent of each child in the global neighbour map
      itsChildren.foldLeft( oldNeighboursMap ) { case ( neighboursMap, v ) =>
        val oldNeighbours = neighboursMap.getOrElse( v, Set.empty: Set[( T, Int )] )
        neighboursMap.updated( v, oldNeighbours + parent )
      }
    }
  }

}

private case class MazeImpl[T]( value: T, depth: Int, branches: Seq[Maze[T]] ) extends Maze[T]
