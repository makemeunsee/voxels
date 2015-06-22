package demo

import geometry.Matrix4
import voxels.{Voxel, AntiPrism}

/**
  * Created by markus on 26/05/15.
  */
object DebugMain {
  def main( args: Array[String] ) {
    println( voxels.standards )
    Voxel(AntiPrism(6),Matrix4.unit,( 0 until 14 ).map( _ => ( Colors.WHITE, Colors.WHITE ) )).faces.foreach { f =>
      (0 until f.vertices.length).foreach { i =>
        val p0 = f.vertices(i)
        val p1 = f.vertices((i+1)%f.vertices.length)
        println((p1-p0).norm)
      }
    }
  }
}
