package maze

import geometry.voronoi.Face

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Random

/**
 * Created by markus on 07/07/2015.
 */
object Maze {
  @tailrec
  private def toDepthMap0[T]( at: Seq[Maze[T]], acc: Map[T, Int] = Map.empty[T, Int], max: Int = -1 ): ( Map[T, Int], Int ) = at match {
    case Nil =>
      ( acc, max )
    case h :: t =>
      val d = h.depth
      toDepthMap0( h.branches ++ t, acc + ( ( h.value, d ) ), math.max( max, d ) )
  }

  @tailrec
  private def childrenMap[T]( remaining: Seq[Maze[T]]
                            , acc: Map[( T, Int ), Set[( T, Int )]] = Map.empty[( T, Int ), Set[( T, Int )]] ): Map[( T, Int ), Set[( T, Int )]] = remaining match {
    case Nil => acc
    case h :: t =>
      val newRem = h.branches ++ t
      val newAcc = acc + ( ( ( h.value, h.depth ), h.branches.map( m => ( m.value, m.depth ) ).toSet ) )
      childrenMap( newRem, newAcc )
  }

  private def depthFirstMaze0[T]( visited: Set[Int], acc: Seq[Maze[Int]], parents: Seq[( Int, Int )])
                                ( implicit faces: Array[Face]
                                , rnd: Random
                                , progressHandler: ( Int, () => T ) => T
                                , continuation: Maze[Int] => T ): T = parents match {
    case Nil =>
      continuation( acc.head )
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
        progressHandler( newVisited.size, () => depthFirstMaze0( newVisited, acc, ( j, depth+1 ) +: ( id, depth ) +: rParents ) )
      }
  }
  // randomized depth first traversal
  def depthFirstMaze[T]( faces: Array[Face]
                       , progressHandler: ( Int, () => T ) => T = { ( _: Int, eval: ( () => T ) ) => eval() }
                       , continuation: Maze[Int] => T )( implicit rnd: Random ): T = {
    if ( faces.isEmpty )
      continuation( empty( -1 ) )
    else {
      val id0 = rnd.nextInt( faces.length )
      depthFirstMaze0( Set( id0 ), Seq.empty, Seq( ( id0, 0 ) ) )( faces, rnd, progressHandler, continuation )
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
      val newAcc = t.indexOf( h ) match {
        // no loop
        case -1 =>
          acc
        // loop, erase it and go on
        case i =>
          t.drop( i )
      }
      // take another step
      val options = faces( h ).neighbours.toSeq
      val newStep = options.toSeq( rnd.nextInt( options.length ) )
      randomWalk( faces, visited, newStep +: newAcc )

    case _ =>
      throw new Error( "Illegal argument" )
  }

  private def wilsonMazeRec[T]( faces: Array[Face], visited: Set[Int], notVisited: Set[Int], maze: Maze[Int] )
                              ( implicit rnd: Random
                              , progressHandler: ( Int, () => T ) => T
                              , continuation: Maze[Int] => T ): T = {
    if ( notVisited.isEmpty )
      continuation( maze )
    else {
      val nextCell = notVisited.head
      val newPath = randomWalk( faces, visited, Seq( nextCell ) )
      val newVisited = visited ++ newPath
      progressHandler( newVisited.size, () => wilsonMazeRec( faces, visited ++ newPath, notVisited -- newPath, maze.plug( newPath ) ) )
    }
  }

  def wilsonMaze[T]( faces: Array[Face]
                   , progressHandler: ( Int, () => T ) => T = { ( _: Int, eval: ( () => T ) ) => eval() }
                   , continuation: Maze[Int] => T )( implicit rnd: Random ): T = {
    val l = faces.length
    val first = rnd.nextInt( l )
    progressHandler( 0, () => wilsonMazeRec( faces, Set( first ), ( 0 until l ).toSet - first, new MutableMaze( first ) ) ( rnd, progressHandler, continuation ) )
  }

  private def randomTraversalRec[T]( faces: Array[Face], visited: Set[Int], options: mutable.Buffer[( Int, Int )], maze: Maze[Int] )
                                   ( implicit rnd: Random
                                   , progressHandler: ( Int, () => T ) => T
                                   , continuation: Maze[Int] => T ): T = {
    if ( options.isEmpty )
      continuation( maze )
    else {
      val l = options.size
      val i = rnd.nextInt( l )
      val ( from, to ) = options( i )
      options( i ) = options.last
      options.remove( l - 1 )
      if ( visited.contains( to ) )
        randomTraversalRec( faces, visited, options, maze )
      else {
        val newMaze = maze.plug( Seq( from, to ) )
        ( faces( to ).neighbours - from ).foreach { n =>
          options += ( ( to, n ) )
        }
        val newVisited = visited + to
        progressHandler( newVisited.size, () => randomTraversalRec( faces, newVisited, options, newMaze ) )
      }
    }
  }

  def randomTraversal[T]( faces: Array[Face]
                        , progressHandler: ( Int, () => T ) => T = { ( _: Int, eval: ( () => T ) ) => eval() }
                        , continuation: Maze[Int] => T )( implicit rnd: Random ): T = {
    val l = faces.length
    val first = rnd.nextInt( l )
    randomTraversalRec( faces
                      , Set( first )
                      , faces( first ).neighbours.map( ( first, _ ) ).toBuffer, new MutableMaze( first ) )(
                        rnd
                      , progressHandler
                      , continuation )
  }

  private def primRec[T]( faces: Array[Face], visited: Set[Int], heap: mutable.PriorityQueue[( Int, ( Int, Int ) )], maze: Maze[Int] )
                        ( implicit rnd: Random
                        , progressHandler: ( Int, () => T ) => T
                        , continuation: Maze[Int] => T ): T = {
    if ( heap.isEmpty )
      continuation( maze )
    else {
      val ( _, ( from, to ) ) = heap.dequeue()
      if ( visited.contains( to ) )
        primRec( faces, visited, heap, maze )
      else {
        ( faces( to ).neighbours - from ).foreach { n =>
          heap.enqueue( ( rnd.nextInt(), ( to, n ) ) )
        }
        val newVisited = visited + to
        progressHandler( newVisited.size, () => primRec( faces, newVisited, heap, maze.plug( Seq( from, to ) ) ) )
      }
    }
  }

  def prim[T]( faces: Array[Face]
             , progressHandler: ( Int, () => T ) => T = { ( _: Int, eval: ( () => T ) ) => eval() }
             , continuation: Maze[Int] => T )( implicit rnd: Random ): T = {
    val l = faces.length
    val first = rnd.nextInt( l )
    implicit def heapOrdering[T <: ( Int, ( Int, Int ) )]: Ordering[T] = Ordering.by( _._1 )
    val heap = new mutable.PriorityQueue[( Int, (Int, Int) )]
    faces( first ).neighbours.foreach { to =>
      heap.enqueue( ( rnd.nextInt(), ( first, to ) ) )
    }
    primRec( faces, Set( first ), heap, new MutableMaze( first ) )( rnd, progressHandler, continuation )
  }
}

import maze.Maze._

trait Maze[T] {
  def value: T
  def depth: Int
  def branches: Seq[Maze[T]]

  def size: Int = branches.foldLeft( 1 ){ _ + _.size }

  def plug( branch: Seq[T] ): Maze[T]

  // returns a map of each node and its depth, the max depth
  def metrics: ( Map[T, Int], Int ) = toDepthMap0( Seq( this ) )

  def childrenMap: Map[( T, Int ), Set[( T, Int )]] = Maze.childrenMap( Seq( this ) )

  def toNiceString( indent: Int = 0 ): String = {
    s"${"*"*indent}( $value, $depth )${branches.map( b => s"|${"*"*indent}${b.toNiceString( indent+1 )}" ).mkString}"
  }
}