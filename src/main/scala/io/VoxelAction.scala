package io

/**
 * Created by markus on 14/06/2015.
 */
object VoxelAction {
  val insertPattern = "I([0-9]+)".r
  val dockPattern = "D([0-9]+)-([0-9]+)-([0-9]+)-([0-9]+)-([0-9]+)".r
  val deletePattern = "Z([0-9]+)".r
  val centerOnPattern = "C([0-9]+)".r
  val colorFace = "CF([0-9]+)-([0-9]+)-([A-F0-9]{6})".r
}

sealed trait VoxelAction {
  def toCode: String
  override def toString = toCode
}

case class Insert( stdId: Int ) extends VoxelAction {
  def toCode = s"I$stdId"
}

case class Dock( stdId: Int
               , faceId: Int
               , rot: Int
               , onVoxelId: Int
               , onFaceId: Int ) extends VoxelAction {
  def toCode = s"D$stdId-$faceId-$rot-$onVoxelId-$onFaceId"
}

case class Delete( voxelId: Int ) extends VoxelAction {
  def toCode = s"Z$voxelId"
}

case class CenterOn( voxelId: Int ) extends VoxelAction {
  def toCode = s"C$voxelId"
}

case class ColorFace( voxelId: Int, faceId: Int, color: String ) extends VoxelAction {
  def toCode = s"CF$voxelId-$faceId-$color".toUpperCase
}