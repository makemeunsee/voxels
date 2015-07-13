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

  def depthFirstMaze( faces: Array[Face] )( implicit rnd: Random ): Maze[Int] = {
    if ( faces.isEmpty )
      empty( -1 )
    else {
      val id0 = rnd.nextInt( faces.length )
      depthFirstMaze0( Set( id0 ), Seq.empty, Seq( ( id0, 0 ) ) )( faces, rnd )
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

  def empty[T]( t: T ): Maze[T] = MazeImpl( t, 0, Seq.empty )

  // walk randomly until a visited cell is reached
  // upon a loop, it is erased, and walking continues
  @tailrec
  def randomWalk( faces: Array[Face], visited: Set[Int], acc: Seq[Int] )
                ( implicit rnd: Random ): Seq[Int] = acc match {
    // reached a visited cell
    case h :: _ if visited.contains( h ) =>
      acc

    case h :: t =>
      val maybeI = t.find( h == _ )
      val ( options, newAcc ) = maybeI match {
        // loop, erase it and go on
        case Some( i ) =>
          ( faces( h ).neighbours.toSeq, t.drop( i ) )
        // no loop
        case None =>
          ( faces( h ).neighbours.toSeq, acc )
      }
      // take another step
      val newStep = options( rnd.nextInt( options.length ) )
      randomWalk( faces, visited, newStep +: newAcc )

    case _ =>
      throw new Error( "Illegal argument" )
  }

  @tailrec
  private def wilsonMazeRec( faces: Array[Face], visited: Set[Int], notVisited: Set[Int], maze: Maze[Int] )( implicit rnd: Random ): Maze[Int] =
    if ( notVisited.isEmpty )
      maze
    else {
      val nextCell = notVisited.head
      val newPath = randomWalk( faces, visited, Seq( nextCell ) )
      // slow because of maze.plug: O( size of maze )
      wilsonMazeRec( faces, visited ++ newPath, notVisited -- newPath, maze.plug( newPath ) )
    }

  def wilsonMaze( faces: Array[Face] )( implicit rnd: Random ): Maze[Int] = {
    val l = faces.length
    val first = rnd.nextInt( l )
    wilsonMazeRec( faces, Set( first ), ( 0 until l ).toSet - first, MazeImpl( first, 0, Seq.empty ) )
  }

}

import maze.Maze._

trait Maze[T] {
  def value: T
  def depth: Int
  def branches: Seq[Maze[T]]

  def size: Int = branches.foldLeft( 1 ){ _ + _.size }

  def plug( branch: Seq[T] ): Maze[T]

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

//TODO: mutable maze

private case class MazeImpl[T]( value: T, depth: Int, branches: Seq[Maze[T]] ) extends Maze[T] {
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
      copy( branches = branches.reverse.map( _.plug( branch) ) )
  }

  @tailrec
  private def toMaze( depth: Int, branch: Seq[T], acc: Seq[MazeImpl[T]] ): Maze[T] = branch match {
    // dont accept empty branches
    case Nil =>
      throw new Error( "Illegal argument" )
    // on the last element of the branch, link the maze nodes together
    case h :: Nil =>
      acc.foldLeft( MazeImpl( h, depth, Seq.empty ) ) { case ( child, parent ) =>
        parent.copy( branches = Seq( child ) )
      }
    // turn each branch element into a disconnected maze node
    case h :: t =>
      toMaze( depth+1, t, MazeImpl( h, depth, Seq.empty ) +: acc )
  }
}
