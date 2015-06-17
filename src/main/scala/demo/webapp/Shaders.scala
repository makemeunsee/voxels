package demo.webapp

import scala.scalajs.js.annotation.JSExport

/**
 * Created by markus on 11/06/15.
 */
@JSExport
object Shaders {
  private var p_vertexShader: String = ""
  private var p_fragmentShader: String = ""
  private var p_pickVertexShader: String = ""
  private var p_pickFragmentShader: String = ""

  def vertexShader = p_vertexShader
  def fragmentShader = p_fragmentShader
  def pickVertexShader = p_pickVertexShader
  def pickFragmentShader = p_pickFragmentShader

  @JSExport
  def loadShaders( vs: String, fs: String, pvs: String, pfs: String ): Unit = {
    p_vertexShader = vs
    p_fragmentShader = fs
    p_pickVertexShader = pvs
    p_pickFragmentShader = pfs
  }

}
