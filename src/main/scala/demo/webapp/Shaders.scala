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
  private var p_axisVertexShader: String = ""
  private var p_axisFragmentShader: String = ""
  private var p_mazeVertexShader: String = ""
  private var p_mazeFragmentShader: String = ""

  def vertexShader = p_vertexShader
  def fragmentShader = p_fragmentShader
  def pickVertexShader = p_pickVertexShader
  def pickFragmentShader = p_pickFragmentShader
  def axisVertexShader = p_axisVertexShader
  def axisFragmentShader = p_axisFragmentShader
  def mazeVertexShader = p_mazeVertexShader
  def mazeFragmentShader = p_mazeFragmentShader

  @JSExport
  def loadShaders( vs: String, fs: String, pvs: String, pfs: String, avs: String, afs: String, mvs: String, mfs: String ): Unit = {
    p_vertexShader = vs
    p_fragmentShader = fs
    p_pickVertexShader = pvs
    p_pickFragmentShader = pfs
    p_axisVertexShader = avs
    p_axisFragmentShader = afs
    p_mazeVertexShader = mvs
    p_mazeFragmentShader = mfs
  }

}
