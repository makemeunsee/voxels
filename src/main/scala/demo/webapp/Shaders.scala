package demo.webapp

import scala.scalajs.js.annotation.JSExport

/**
 * Created by markus on 11/06/15.
 */
@JSExport
object Shaders {
  private var p_vertexShader: String = ""
  private var p_fragmentShader: String = ""
  private var p_mazeVertexShader: String = ""
  private var p_mazeFragmentShader: String = ""

  def vertexShader = p_vertexShader
  def fragmentShader = p_fragmentShader
  def mazeVertexShader = p_mazeVertexShader
  def mazeFragmentShader = p_mazeFragmentShader

  @JSExport
  def loadShaders( vs: String, fs: String, mvs: String, mfs: String ): Unit = {
    p_vertexShader = vs
    p_fragmentShader = fs
    p_mazeVertexShader = mvs
    p_mazeFragmentShader = mfs
  }

}
