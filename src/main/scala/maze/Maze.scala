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
            Seq( ImmutableMaze( id, depth, Seq.empty ) )
          case _   =>
            val ( tails, parallelBranches ) = acc.partition( m => m.depth == depth+1 )
            tails match {
              case Nil =>
                ImmutableMaze( id, depth, Seq.empty ) +: acc
              case _ =>
                ImmutableMaze( id, depth, tails ) +: parallelBranches
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

  def empty[T]( t: T ): Maze[T] = ImmutableMaze( t, 0, Seq.empty )

  // walk randomly until a visited cell is reached
  // upon a loop, it is erased, and walking continues
  @tailrec
  def randomWalk( faces: Array[Face], visited: Set[Int], acc: Seq[Int] )
                ( implicit rnd: Random ): Seq[Int] = acc match {
    // reached a visited cell
    case h :: _ if visited.contains( h ) =>
      acc

    case h :: t =>
      val ( options, newAcc ) = t.find( h == _ ) match {
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
      wilsonMazeRec( faces, visited ++ newPath, notVisited -- newPath, maze.plug( newPath ) )
    }

  def wilsonMaze( faces: Array[Face] )( implicit rnd: Random ): Maze[Int] = {
    val l = faces.length
    val first = rnd.nextInt( l )
    wilsonMazeRec( faces, Set( first ), ( 0 until l ).toSet - first, new MutableMaze( first ) )
  }

  @tailrec
  private def randomTraversalRec( faces: Array[Face], visited: Set[Int], options: Seq[( Int, Int )], maze: Maze[Int] )
                                ( implicit rnd: Random ): Maze[Int] = {
    if ( options.isEmpty )
      maze
    else {
      val i = rnd.nextInt( options.size )
      val ( from, to ) = options( i )
      // slowness here
      val optionsClean = options.take( i ) ++ options.drop( i+1 )
      if ( visited.contains( to ) )
        randomTraversalRec( faces, visited, optionsClean, maze )
      else {
        val newMaze = maze.plug( Seq( from, to ) )
        val newOptions =
          ( faces( to ).neighbours - from ).foldLeft( optionsClean ) { case ( acc, n ) =>
            ( to, n ) +: acc
          }
        randomTraversalRec( faces, visited + to, newOptions, newMaze )
      }
    }
  }

  // slow
  def randomTraversal( faces: Array[Face] )
                     ( implicit rnd: Random ): Maze[Int] = {
    val l = faces.length
    val first = rnd.nextInt( l )
    randomTraversalRec( faces, Set( first ), faces( first ).neighbours.map( ( first, _ ) ).toSeq, new MutableMaze( first ) )
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

  def toNiceString( indent: Int = 0 ): String = {
    s"${"*"*indent}( $value, $depth )${branches.map( b => s"|${"*"*indent}${b.toNiceString( indent+1 )}" ).mkString}"
  }
}