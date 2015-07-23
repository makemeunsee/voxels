package demo.webapp

import scala.scalajs.js

/**
 * Created by markus on 10/07/15.
 */
@scala.scalajs.js.annotation.JSName("dat.Controller")
@scala.scalajs.js.annotation.RawJSType
class DatController[T] {
  def onChange( callback: js.Function1[ T, Unit ] ): DatController[T] = js.native
  def onFinishChange( callback: js.Function1[ T, Unit ] ): DatController[T] = js.native
  def listen(): js.Any = js.native

  def step( step: Float ): DatController[Float] = js.native
}
