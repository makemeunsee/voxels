package demo.webapp

import scala.scalajs.js

/**
 * Created by markus on 10/07/15.
 */

@scala.scalajs.js.annotation.JSName("dat.GUI")
@scala.scalajs.js.annotation.RawJSType
class DatGUI( obj: js.Dynamic ) {
  @scala.scalajs.js.annotation.JSName("add")
  def addBoolean( container: js.Any, name: String ): DatController[Boolean] = js.native
  @scala.scalajs.js.annotation.JSName("add")
  def addRange( container: js.Any, name: String, min: Float, max: Float, step: Float = 1f ): DatController[Float] = js.native
  @scala.scalajs.js.annotation.JSName("add")
  def addList( container: js.Any, name: String, names: js.Array[String] ): DatController[String] = js.native
  def addColor( container: js.Any, name: String ): DatController[String] = js.native
  def addFolder( name: String ): DatGUI = js.native

  def remove( controller: DatController[_] ): js.Any = js.native

  def open(): js.Any = js.native
  def close(): js.Any = js.native

  def remember( container: js.Any ): js.Any = js.native
}
