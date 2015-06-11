package demo.webapp

/**
 * Created by markus on 16/02/2015.
 */

import geometry.{Matrix4, Vec3}
import geometry.Matrix4.{rotationMatrix, translationMatrix}
import voxels._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.{JSName, JSExport}
import scala.scalajs.js.typedarray.{Uint16Array, Float32Array}
import scala.scalajs.js.{Array, Dictionary}

import org.denigma.threejs._

object TutoMain extends JSApp {
  def main(): Unit = {
    println("start")
  }

  private val dummyCam = new Camera
  private val scene = new Scene
  private val pickScene = new Scene

  private object ReadableWebGLRendererParameters extends WebGLRendererParameters {
    preserveDrawingBuffer = true
  }

  @JSExport
  val renderer = new WebGLRenderer( ReadableWebGLRendererParameters )

  private var meshes = Map.empty[Int, (Mesh, Mesh)]
  @JSExport
  def currentMeshes() = meshes.values.map { case ( m, pm ) => Seq( m, pm ).toJSArray }.toJSArray

  @JSExport
  def pickRender() = renderer.render( pickScene, dummyCam )

  @JSExport
  def render() = {
    meshes.values.foreach { case ( m, pm ) =>
      m.material.asInstanceOf[ShaderMaterial].uniforms.asInstanceOf[scala.scalajs.js.Dynamic].selectDynamic( "u_time" ).updateDynamic( "value" )( 0f )
      m.material.asInstanceOf[ShaderMaterial].uniforms.asInstanceOf[scala.scalajs.js.Dynamic].selectDynamic( "u_borderWidth" ).updateDynamic( "value" )( 1f )
      m.material.asInstanceOf[ShaderMaterial].uniforms.asInstanceOf[scala.scalajs.js.Dynamic].selectDynamic( "u_color" ).updateDynamic( "value" )( new Vector4( 1, 1, 1, 1 ) )
      m.material.asInstanceOf[ShaderMaterial].uniforms.asInstanceOf[scala.scalajs.js.Dynamic].selectDynamic( "u_borderColor" ).updateDynamic( "value" )( new Vector4( 0.05,0.05,0.05,1 ) )
      m.material.asInstanceOf[ShaderMaterial].uniforms.asInstanceOf[scala.scalajs.js.Dynamic].selectDynamic( "u_highlightFlag" ).updateDynamic( "value" )( colorCode( selectedVoxel, selectedFace ).toFloat )
    }
    renderer.render( scene, dummyCam )
  }

  @JSExport
  def naiveRotMat(theta: Double, phi: Double): Array[Array[Double]] = {
    ( rotationMatrix( theta, 0, 1, 0 ) * rotationMatrix( phi, 1, 0, 0 ) ).toSeqs.map( _.toJSArray ).toJSArray
  }

  def latLongPosition(theta: Double, phi: Double, distance: Double): Vec3 = {
    val cosP = math.cos( phi )
    val sinP = math.sin( phi )
    val cosT = math.cos( theta )
    val sinT = math.sin( theta )
    Vec3( distance * sinP * cosT, distance * cosP, distance * sinP * sinT )
  }

  def lookAtMatrix( eye: Vec3, target: Vec3, up: Vec3 ): Array[Array[Double]] = {
    val forward = (target minus eye).normalize
    val right = (forward cross up).normalize
    val up0 =  right cross forward
    Array( Array( right.x, right.y, right.z, -(right dot eye) ),
           Array( up0.x, up0.y, up0.z, -(up0 dot eye) ),
           Array( -forward.x, -forward.y, -forward.z, forward dot eye ),
           Array( 0, 0, 0, 1 ) )
  }

  @JSExport
  def viewMatOf( theta: Double, phi: Double, distance: Double): Array[Array[Double]] = {
    val p = latLongPosition ( theta, math.max( 0.00001, math.min( math.Pi - 0.00001, phi ) ), distance )
    lookAtMatrix( p, Vec3( 0, 0, 0 ), Vec3( 0, 1, 0 ) )
  }

  def orthoMatrix( left: Double, right: Double, bottom: Double, top: Double, near: Double, far: Double ): Array[Array[Double]] = {
    val x_orth = 2 / (right - left)
    val y_orth = 2 / (top - bottom)
    val z_orth = -2 / (far - near)
    val tx = -(right + left) / (right - left)
    val ty = -(top + bottom) / (top - bottom)
    val tz = -(far + near) / (far - near)
    Array( Array( x_orth, 0, 0, tx ),
           Array( 0, y_orth, 0, ty ),
           Array( 0, 0, z_orth, tz ),
           Array( 0, 0, 0, 1 ) )
  }

  @JSExport
  def orthoMatrixFromScreen( w: Double, h: Double, k: Double ) = {
    val hh = if ( h < 0 ) 1 else h
    val aspect = w / hh
    val far = 100
    val near = -100
    val right = k * aspect
    val top = k
    val left = -right
    val bottom = -top
    orthoMatrix( left, right, bottom, top, near, far )
  }

  // BufferGeometry from org.denigma.threejs does not extends Geometry, has to be redefined
  @JSName("THREE.BufferGeometry")
  class MyBufferGeometry extends Geometry {
    var attributes: js.Array[BufferAttribute] = js.native
    var drawcalls: js.Any = js.native
    var offsets: js.Any = js.native
    def addAttribute(name: String, attribute: BufferAttribute): js.Dynamic = js.native
    def addAttribute(name: String, array: js.Any, itemSize: Double): js.Dynamic = js.native
    def getAttribute(name: String): js.Dynamic = js.native
    def addDrawCall(start: Double, count: Double, index: Double): Unit = js.native
    def applyMatrix(matrix: Matrix4): Unit = js.native
    def fromGeometry(geometry: Geometry, settings: js.Any = js.native): BufferGeometry = js.native
    def computeVertexNormals(): Unit = js.native
    def computeOffsets(indexBufferSize: Double): Unit = js.native
    def merge(): Unit = js.native
    def normalizeNormals(): Unit = js.native
    def reorderBuffers(indexBuffer: Double, indexMap: js.Array[Double], vertexCount: Double): Unit = js.native
  }

  @JSExport
  val voxelTypeCount = standards.size

  private var freeVoxelIds: Set[Int] = Set.empty
  private var lastDockedId: Int = -1

  private var voxels: Map[Int, Voxel] = Map.empty
  private var selectedVoxel: Int = -1
  private var selectedFace: Int = -1
  // ( voxelStd id, face id, rotation step )
  private var dockingOptions = Seq.empty[(Int,Int,Int)]

  private def makeMesh( v: Voxel, vId: Int ): ( Mesh, Mesh ) = {
    val customUniforms = js.Dynamic.literal(
      "u_time" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_borderWidth" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_mvpMat" -> js.Dynamic.literal( "type" -> "m4", "value" -> new org.denigma.threejs.Matrix4() ),
      "u_color" -> js.Dynamic.literal( "type" -> "v4", "value" -> new Vector4( 1, 1, 1, 1 ) ),
      "u_borderColor" -> js.Dynamic.literal( "type" -> "v4", "value" -> new Vector4( 0, 0, 0, 1 ) ),
      "u_highlightFlag" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 )
    )

    val geom = new MyBufferGeometry()

    // create attributes for each vertex
    // currently 2 colors are given as vertex attributes
    val attrs = js.Dynamic.literal(
      "a_normal" -> js.Dynamic.literal(
        "type" -> "v3",
        "value" -> new Array
      ),
      "a_pickColor" -> js.Dynamic.literal(
        "type" -> "f",
        "value" -> new Array
      ),
      "a_centerFlag" -> js.Dynamic.literal(
        "type" -> "f",
        "value" -> new Array
      )
    )

    val shaderMaterial = new ShaderMaterial
    shaderMaterial.attributes = attrs
    shaderMaterial.uniforms = customUniforms
    shaderMaterial.vertexShader = Shaders.vertexShader
    shaderMaterial.fragmentShader = Shaders.fragmentShader
//    shaderMaterial.side = org.denigma.threejs.THREE.DoubleSide

    val pickShaderMaterial = new ShaderMaterial
    pickShaderMaterial.attributes = attrs
    pickShaderMaterial.uniforms = customUniforms
    pickShaderMaterial.vertexShader = Shaders.pickVertexShader
    pickShaderMaterial.fragmentShader = Shaders.pickFragmentShader
//    pickShaderMaterial.side = org.denigma.threejs.THREE.DoubleSide

    val count = v.faces.map( _.rawVertices.length+3 ).sum
    val indicesCount = v.faces.map( _.vertices.length*3 ).sum

    val vertices = new Float32Array( count )
    val normals = new Float32Array( count )
    val pickColors = new Float32Array( count / 3 )
    val centerFlags = new Float32Array( count / 3 )
    val indices = new Uint16Array( indicesCount )
    var offset = 0
    var indicesOffset = 0
    v.faces
      .zipWithIndex
      .foreach { case ( f, fId ) =>
        val Vec3( nx, ny, nz ) = f.normal
        val Vec3( cx, cy, cz ) = f.center
        val color = colorCode( vId, fId )

        val triOffset = offset*3
        val vSize = f.vertices.length

        ( 0 until vSize).foreach { i =>
          vertices.set( triOffset+3*i,   f.rawVertices( 3*i ).toFloat )
          vertices.set( triOffset+3*i+1, f.rawVertices( 3*i+1 ).toFloat )
          vertices.set( triOffset+3*i+2, f.rawVertices( 3*i+2 ).toFloat )
          normals.set( triOffset+3*i,   nx.toFloat )
          normals.set( triOffset+3*i+1, ny.toFloat )
          normals.set( triOffset+3*i+2, nz.toFloat )
          centerFlags.set( offset+i, 0 )
          pickColors.set( offset+i, color )
          indices.set( indicesOffset+3*i,   offset+vSize )
          indices.set( indicesOffset+3*i+1, offset+i )
          indices.set( indicesOffset+3*i+2, offset+(i+1)%vSize )
        }
        vertices.set( triOffset+3*vSize,   cx.toFloat )
        vertices.set( triOffset+3*vSize+1, cy.toFloat )
        vertices.set( triOffset+3*vSize+2, cz.toFloat )
        normals.set( triOffset+3*vSize,   nx.toFloat )
        normals.set( triOffset+3*vSize+1, ny.toFloat )
        normals.set( triOffset+3*vSize+2, nz.toFloat )
        centerFlags.set( offset+vSize, 1 )
        pickColors.set( offset+vSize, color )

        offset = offset + vSize + 1
        indicesOffset = indicesOffset + vSize*3
      }

    geom.addAttribute( "index", new BufferAttribute( indices, 1 ) )
    geom.addAttribute( "a_centerFlag", new BufferAttribute( centerFlags, 1 ) )
    geom.addAttribute( "a_normal", new BufferAttribute( normals, 3 ) )
    geom.addAttribute( "a_pickColor", new BufferAttribute( pickColors, 1 ) )
    geom.addAttribute( "position", new BufferAttribute( vertices, 3 ) )

    ( combineMesh( geom, shaderMaterial ), combineMesh( geom, pickShaderMaterial ) )
  }

  private def combineMesh( geometry: MyBufferGeometry, material: ShaderMaterial ): Mesh = {
    val mesh = new Mesh
    mesh.geometry = geometry
    mesh.material = material
    mesh.frustumCulled = false
    mesh
  }

  @JSExport
  def loadVoxel( i: Int ): Unit = {
    freeVoxelIds = Set.empty
    voxels = Map( 0 -> Voxel( standards.getOrElse( i, Cube ), Matrix4.unit ) )
    clearSelection()

    meshes.foreach { case ( _, ( m, pm ) ) =>
      scene.remove( m )
      pickScene.remove( pm )
    }
    meshes = voxels.map { case ( k, v ) => ( k, makeMesh( v, k ) ) }
    scene.add( meshes( 0 )._1 )
    pickScene.add( meshes( 0 )._2 )
  }

  private def colorCode( voxelId: Int, faceId: Int ) = ( faceId << 17 ) + ( voxelId+1 )
  private def revertColorCode( colorCode: Int ): (Int, Int) = {
    val faceId = colorCode >>> 17
    val voxelId = colorCode - ( faceId << 17 ) - 1
    ( voxelId, faceId )
  }

  @JSExport
  def selectFace( colorCode: Int ): Dictionary[String] = {
    val ( vId, fId ) = revertColorCode( colorCode )
    dockingOptions = listDockingOptions( vId, fId )
    selectedVoxel = vId
    selectedFace = fId
    val selection = voxels.lift( vId ).flatMap( _.faces.lift( fId) ).fold( -1, -1, "" )( f => ( vId, fId, s"Voxel: $vId, face: ${f.faceType}" ) )
    Map( ( "voxelId", selection._1.toString )
       , ( "faceId", selection._2.toString )
       , ( "text", selection._3 )
       )
      .toJSDictionary
  }

  @JSExport
  def showDockingOptions(): Dictionary[String] = dockingOptions
    .zipWithIndex
    .map { case ( ( stdId, _, _ ), i ) =>
      val std = standards( stdId )
      ( i.toString
      , s"${std.name}"
      )
    }
    .toMap
    .toJSDictionary

  private def listDockingOptions( voxelId: Int, faceId: Int ): Seq[(Int,Int,Int)] = {
    voxels.lift( voxelId ).flatMap( _.faces.lift( faceId ) ).fold( Seq.empty[(Int,Int,Int)] ) { f =>
      val faceType = f.faceType
      standards
        .flatMap { case ( stdId, std ) =>
        std.uniquePositionings
          .collect { case ( fId, fType, rot ) if fType == faceType => ( stdId, fId, rot ) }
      }.toSeq
    }
  }

  @JSExport
  def dockVoxel( dockingID: Int ): Unit = {
    assert( dockingID >= 0 && dockingID < dockingOptions.length )

    val newVId = dockingOptions( dockingID )._1
    val fId = dockingOptions( dockingID )._2
    val rot = dockingOptions( dockingID )._3

    val newVoxelStd = standards.getOrElse( newVId, Cube )
    val newVoxelUntransformed = Voxel( newVoxelStd, Matrix4.unit )
    val sourceFace = newVoxelUntransformed.faces( fId )
    val targetFace = voxels( selectedVoxel ).faces( selectedFace )

    // rotation so docking faces actually face each other
    val targetN@Vec3( nx, ny, nz ) = targetFace.normal
    val sourceN = sourceFace.normal
    val rM = rotationMatrix( sourceN, targetN.negate )

    // rotated voxel
    val newVoxelRotated = Voxel( newVoxelStd, rM )
    val sourceFace1 = newVoxelRotated.faces( fId )

    val to = targetFace.center

    // spin so docking faces actually fit each other
    val from = sourceFace1.center
    val preSpinTranslation = translationMatrix( from.negate )
    val postSpinTranslation = translationMatrix( from )
    val spinRotation = rotationMatrix( ( sourceFace1.vertices.head - from ).normalize
                                     , ( targetFace.vertices.head - to ).normalize
                                     , Some( targetN ) )
    // spin shift (rotation variations)
    val bonusSpinRotation = rotationMatrix( rot*2*math.Pi/sourceFace.vertices.length, nx, ny, nz )

    // translation so docking faces actually touch each other
    val tM = translationMatrix( to - from )

    val newVoxel = Voxel( newVoxelStd, tM * postSpinTranslation * spinRotation * bonusSpinRotation * preSpinTranslation * rM  )

    lastDockedId = if ( freeVoxelIds.isEmpty) voxels.size
                   else { val r = freeVoxelIds.head
                          freeVoxelIds = freeVoxelIds - r
                          r }

    voxels = voxels + ( ( lastDockedId, newVoxel ) )

    val newMeshes = makeMesh( newVoxel, lastDockedId )
    meshes = meshes + ( ( lastDockedId, newMeshes ) )
    scene.add( newMeshes._1 )
    pickScene.add( newMeshes._2 )
  }

  @JSExport
  def undockLastVoxel(): Unit = {
    voxels = voxels - lastDockedId
    val removedMeshes = meshes( lastDockedId )
    meshes = meshes - lastDockedId
    scene.remove( removedMeshes._1 )
    pickScene.remove( removedMeshes._2 )
    freeVoxelIds = freeVoxelIds + lastDockedId
  }

  @JSExport
  def deleteSelected(): Unit = {
    if ( voxels.size > 1 ) {
      voxels.get( selectedVoxel ).foreach { v =>
        voxels = voxels - selectedVoxel
        val removedMeshes = meshes( selectedVoxel )
        meshes = meshes - selectedVoxel
        scene.remove( removedMeshes._1 )
        pickScene.remove( removedMeshes._2 )
        freeVoxelIds = freeVoxelIds + selectedVoxel
        clearSelection()
      }
    }
  }

  @JSExport
  def clearSelection(): Unit = {
    selectedVoxel = -1
    selectedFace = -1
    dockingOptions = Seq.empty
    lastDockedId = -1
  }

  @JSExport
  def getVoxelName( id: Int ): String = standards.lift( id ).fold( "" )( _.name )
}
