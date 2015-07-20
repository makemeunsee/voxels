package demo

import geometry.Normal3
import geometry.voronoi.VoronoiModel.cubeModel
import maze.Maze

import scala.util.Random

/**
 * Created by markus on 15/07/15.
 */
object PerfMain {
  def main ( args: Array[String] ) {
    implicit val rnd = new Random( 0 )
    val cutCount = 10000

    val cutNormals = ( 0 until cutCount ).map { _ =>
      val ( theta, phi ) = geometry.uniformToSphericCoords( rnd.nextDouble(), rnd.nextDouble() )
      Normal3.fromSphericCoordinates( theta, phi )
    }

    // 'warm up', sic
    cubeModel.cut( cutNormals )

    val t0 = System.currentTimeMillis()

    ( 0 until 9 ) foreach { i => println( i ) ; cubeModel.cut( cutNormals ) }
    val model = cubeModel
    model.cut( cutNormals )

    val t1 = System.currentTimeMillis()

    // generate maze
    val maze = Maze.depthFirstMaze( model.faces )
//    val maze = Maze.prim( model.faces)
//    val maze = Maze.wilsonMaze( model.faces)
//    val maze = Maze.randomTraversal( model.faces)

    val mazeMetrics = maze.metrics
    println( mazeMetrics._1.size, mazeMetrics._2 )

    val t2 = System.currentTimeMillis()

    println( "cut time", ( t1 - t0 ) / 10 )
    println( "maze time", t2 - t1 )
  }
}
