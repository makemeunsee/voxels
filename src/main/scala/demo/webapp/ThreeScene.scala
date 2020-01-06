package demo.webapp

import demo.Colors
import geometry.{Matrix4, Vec3}
import org.denigma.threejs._
import voxels.Voxel
import voxels.Voxel.Face

import scala.scalajs.js
import scala.scalajs.js.Array
import scala.scalajs.js.annotation.{JSExport, JSName}
import scala.scalajs.js.typedarray.{Float32Array, Uint16Array, Uint8Array}

/**
 * Created by markus on 17/06/2015.
 */
object ThreeScene {
  // BufferGeometry from org.denigma.threejs does not extends Geometry, has to be redefined
  @JSName( "THREE.BufferGeometry" )
  class MyBufferGeometry extends Geometry {
    var attributes: js.Array[BufferAttribute] = js.native
    var drawcalls: js.Any = js.native
    var offsets: js.Any = js.native
    def addAttribute( name: String, attribute: BufferAttribute ): js.Dynamic = js.native
    def addAttribute( name: String, array: js.Any, itemSize: Double ): js.Dynamic = js.native
    def getAttribute( name: String ): js.Dynamic = js.native
    def addDrawCall( start: Double, count: Double, index: Double ): Unit = js.native
    def applyMatrix( matrix: Matrix4 ): Unit = js.native
    def fromGeometry( geometry: Geometry, settings: js.Any = js.native ): BufferGeometry = js.native
    def computeVertexNormals(): Unit = js.native
    def computeOffsets( indexBufferSize: Double ): Unit = js.native
    def merge(): Unit = js.native
    def normalizeNormals(): Unit = js.native
    def reorderBuffers( indexBuffer: Double, indexMap: js.Array[Double], vertexCount: Double ): Unit = js.native
  }

  object ReadableWebGLRendererParameters extends WebGLRendererParameters {
    preserveDrawingBuffer = false
  }

  // zip faces of a voxel with their offset and size as in the meshes data buffers
  def withOffsetsAndSizes( faces: Iterable[Face] ): Seq[( Face, Int, Int )] = {
    faces.foldLeft( ( 0, List.empty[( Face, Int, Int )] ) ) { case ( ( offset, acc ), f ) =>
      // counting the centers which is automatically added to the meshes
      val l = f.vertices.length + 1
      val newOffset = offset + l
      ( newOffset, ( f, offset, l ) :: acc )
    }._2.reverse
  }

  def colorCode( voxelId: Int, faceId: Int ) = ( faceId << 17 ) + ( voxelId+1 )

  def revertColorCode( colorCode: Int ): ( Int, Int ) = {
    val faceId = colorCode >>> 17
    val voxelId = colorCode - ( faceId << 17 ) - 1
    ( voxelId, faceId )
  }

  import scala.language.implicitConversions
  implicit def toJsMatrix( m: Matrix4 ): org.denigma.threejs.Matrix4 = {
    val r = new org.denigma.threejs.Matrix4()
    r.set( m.a00, m.a01, m.a02, m.a03
         , m.a10, m.a11, m.a12, m.a13
         , m.a20, m.a21, m.a22, m.a23
         , m.a30, m.a31, m.a32, m.a33
    )
    r
  }
}

import demo.webapp.ThreeScene._

class ThreeScene {

  // ******************** three scene basics ********************

  // dummy cam as projections are handled manually
  private val dummyCam = new Camera
  private val scene = new Scene
  private val pickScene = new Scene

  @JSExport
  val renderer = new WebGLRenderer( ReadableWebGLRendererParameters )

  // ******************** view management ********************

  private val zoomMax = 16
  private val zoomMin = 0.0625
  private val zoomSpeed = 1.05
  private var zoom = 0.2d

  @JSExport
  def zoom( delta: Double ): Unit = {
    zoom = Math.min( Math.max( zoom * Math.pow( zoomSpeed, delta ), zoomMin ), zoomMax )
    updateMVP()
  }

  private var projMat = Matrix4.unit
  private val viewMat = Matrix4.unit
  private var modelMat = Matrix4.naiveRotMat( 0.5, 0.3 )
  private var transMat = Matrix4.unit
  private var mvp = Matrix4.unit

  private var innerWidth: Int = 0
  private var innerHeight: Int = 0

  @JSExport
  def updateViewport( width: Int, height: Int ): Unit = {
    println( "viewport", width, height )
    innerWidth = width
    innerHeight = height
    projMat = Matrix4.orthoMatrixFromScreen( width, height, 1.75 )
    updateMVP()
  }

  @JSExport
  def rotateView( deltaX: Int, deltaY: Int ): Unit = {
    modelMat = Matrix4.naiveRotMat( deltaX * 0.002, deltaY * 0.002 ) * modelMat
    updateMVP()
  }

  def centerOn( voxel: Voxel ): Unit = {
    transMat = Matrix4.translationMatrix( voxel.faces.foldLeft( Vec3( 0,0,0 ) )( _ - _.center ) / voxel.faces.length )
    updateMVP()
  }

  private def updateMVP(): Unit = {
    mvp = projMat * Matrix4.zoomMatrix( zoom ) * viewMat * modelMat * transMat
  }
  
  // ******************** axis management ********************

  private lazy val axisMesh: Mesh = makeAxisMesh
  private var showAxis: Boolean = false

  @JSExport
  def toggleAxis(): Unit = {
    showAxis = !showAxis
    if ( showAxis )
      scene.add( axisMesh )
    else
      scene.remove( axisMesh )
  }
  
  // ******************** mesh management ********************

  private var meshes = Map.empty[Int, ( Mesh, Mesh )]

  def addVoxel( id: Int, voxel: Voxel ): Unit = {
    val newMeshes = makeMesh( voxel, id )
    meshes = meshes + ( ( id, newMeshes ) )
    scene.add( newMeshes._1 )
    pickScene.add( newMeshes._2 )
  }

  def removeVoxel( voxelId: Int ): Unit = {
    val removedMeshes = meshes( voxelId )
    meshes = meshes - voxelId
    scene.remove( removedMeshes._1 )
    pickScene.remove( removedMeshes._2 )
    removedMeshes._1.geometry.dispose()
    removedMeshes._2.geometry.dispose()
  }

  def clear(): Unit = {
    meshes.foreach { case ( _, ( m, pm ) ) =>
      scene.remove( m )
      pickScene.remove( pm )
      m.geometry.dispose()
      pm.geometry.dispose()
    }
    scene.remove( axisMesh )
    showAxis = false
    meshes = Map.empty
  }

  private def makeMesh( v: Voxel, vId: Int ): ( Mesh, Mesh ) = {
    val customUniforms = js.Dynamic.literal(
      "u_time" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_borderWidth" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_mvpMat" -> js.Dynamic.literal( "type" -> "m4", "value" -> new org.denigma.threejs.Matrix4() ),
      "u_borderColor" -> js.Dynamic.literal( "type" -> "v3", "value" -> new Vector4( 0, 0, 0, 1 ) ),
      "u_highlightFlag" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_faceHighlightFlag" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 )
    )

    val geom = new MyBufferGeometry()

    val attrs = js.Dynamic.literal(
      "a_color" -> js.Dynamic.literal(
        "type" -> "v3",
        "value" -> new Array[Float]
      ),
      "a_normal" -> js.Dynamic.literal(
        "type" -> "v3",
        "value" -> new Array[Float]
      ),
      "a_pickColor" -> js.Dynamic.literal(
        "type" -> "f",
        "value" -> new Array[Float]
      ),
      "a_centerFlag" -> js.Dynamic.literal(
        "type" -> "f",
        "value" -> new Array[Float]
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
    val colors = new Float32Array( count )
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
        val pickColor = colorCode( vId, fId )
        val col = v.colors.lift( fId ).getOrElse( Colors.WHITE )
        val ( r, g, b ) = Colors.intColorToFloatsColors( col )
        
        val triOffset = offset*3
        val vSize = f.vertices.length

        ( 0 until vSize ).foreach { i =>
          vertices.set( triOffset+3*i,   f.rawVertices( 3*i ).toFloat )
          vertices.set( triOffset+3*i+1, f.rawVertices( 3*i+1 ).toFloat )
          vertices.set( triOffset+3*i+2, f.rawVertices( 3*i+2 ).toFloat )
          normals.set( triOffset+3*i,   nx.toFloat )
          normals.set( triOffset+3*i+1, ny.toFloat )
          normals.set( triOffset+3*i+2, nz.toFloat )
          colors.set( triOffset+3*i,   r )
          colors.set( triOffset+3*i+1, g )
          colors.set( triOffset+3*i+2, b )
          centerFlags.set( offset+i, 0f )
          pickColors.set( offset+i, pickColor )
          indices.set( indicesOffset+3*i,   offset+vSize )
          indices.set( indicesOffset+3*i+1, offset+i )
          indices.set( indicesOffset+3*i+2, offset+( i+1 )%vSize )
        }
        vertices.set( triOffset+3*vSize,   cx.toFloat )
        vertices.set( triOffset+3*vSize+1, cy.toFloat )
        vertices.set( triOffset+3*vSize+2, cz.toFloat )
        normals.set( triOffset+3*vSize,   nx.toFloat )
        normals.set( triOffset+3*vSize+1, ny.toFloat )
        normals.set( triOffset+3*vSize+2, nz.toFloat )
        colors.set( triOffset+3*vSize,   r )
        colors.set( triOffset+3*vSize+1, g )
        colors.set( triOffset+3*vSize+2, b )
        centerFlags.set( offset+vSize, 1f )
        pickColors.set( offset+vSize, pickColor )

        offset = offset + vSize + 1
        indicesOffset = indicesOffset + vSize*3
    }

    geom.addAttribute( "index", new BufferAttribute( indices, 1 ) )
    geom.addAttribute( "a_centerFlag", new BufferAttribute( centerFlags, 1 ) )
    geom.addAttribute( "a_normal", new BufferAttribute( normals, 3 ) )
    geom.addAttribute( "a_color", new BufferAttribute( colors, 3 ) )
    geom.addAttribute( "a_pickColor", new BufferAttribute( pickColors, 1 ) )
    geom.addAttribute( "position", new BufferAttribute( vertices, 3 ) )

    ( assembleMesh( geom, shaderMaterial, "baseMesh" ), assembleMesh( geom, pickShaderMaterial, "pickMesh" ) )
  }

  private def makeAxisMesh: Mesh = {
    val customUniforms = js.Dynamic.literal(
      "u_mvpMat" -> js.Dynamic.literal( "type" -> "m4", "value" -> new org.denigma.threejs.Matrix4() )
    )

    val geom = new MyBufferGeometry()

    val attrs = js.Dynamic.literal(
      "a_color" -> js.Dynamic.literal(
        "type" -> "v3",
        "value" -> new Array[Float]
      )
    )

    val shaderMaterial = new ShaderMaterial
    shaderMaterial.attributes = attrs
    shaderMaterial.uniforms = customUniforms
    shaderMaterial.vertexShader = Shaders.axisVertexShader
    shaderMaterial.fragmentShader = Shaders.axisFragmentShader
    shaderMaterial.wireframe = true
    //    shaderMaterial.side = org.denigma.threejs.THREE.DoubleSide

    val count = 18
    val indicesCount = 9

    val vertices = new Float32Array( count )
    val colors = new Float32Array( count )
    val indices = new Uint16Array( indicesCount )

    // 0
    vertices.set( 0, 0f )
    vertices.set( 1, 0f )
    vertices.set( 2, 0f )
    // 1
    vertices.set( 3, 3f )
    vertices.set( 4, 0f )
    vertices.set( 5, 0f )
    // 2
    vertices.set( 6, 0f )
    vertices.set( 7, 0f )
    vertices.set( 8, 0f )
    // 3
    vertices.set( 9, 0f )
    vertices.set( 10, 3f )
    vertices.set( 11, 0f )
    // 4
    vertices.set( 12, 0f )
    vertices.set( 13, 0f )
    vertices.set( 14, 3f )
    // 5
    vertices.set( 15, 0f )
    vertices.set( 16, 0f )
    vertices.set( 17, 0f )

    // 0
    colors.set( 0, 1f )
    colors.set( 1, 0f )
    colors.set( 2, 0f )
    // 1
    colors.set( 3, 1f )
    colors.set( 4, 0f )
    colors.set( 5, 0f )
    // 2
    colors.set( 6, 0f )
    colors.set( 7, 1f )
    colors.set( 8, 0f )
    // 3
    colors.set( 9, 0f )
    colors.set( 10, 1f )
    colors.set( 11, 0f )
    // 4
    colors.set( 12, 0f )
    colors.set( 13, 0f )
    colors.set( 14, 1f )
    // 5
    colors.set( 15, 0f )
    colors.set( 16, 0f )
    colors.set( 17, 1f )

    indices.set( 0, 0 )
    indices.set( 1, 1 )
    indices.set( 2, 0 )
    indices.set( 3, 2 )
    indices.set( 4, 3 )
    indices.set( 5, 2 )
    indices.set( 6, 4 )
    indices.set( 7, 5 )
    indices.set( 8, 4 )

    geom.addAttribute( "index", new BufferAttribute( indices, 1 ) )
    geom.addAttribute( "a_color", new BufferAttribute( colors, 3 ) )
    geom.addAttribute( "position", new BufferAttribute( vertices, 3 ) )

    assembleMesh( geom, shaderMaterial, "axisMesh" )
  }

  private def assembleMesh( geometry: Geometry, material: Material, name: String ): Mesh = {
    val mesh = new Mesh
    mesh.geometry = geometry
    mesh.material = material
    mesh.frustumCulled = false
    mesh.name = name
    mesh
  }

  // ******************** special effects ********************

  def colorFace( voxelId: Int
               , faceOffset: Int
               , faceSize: Int
               , color: ( Float, Float, Float ) ): Unit = {
    val mesh = meshes( voxelId )._1
    val attrs = mesh
      .geometry.asInstanceOf[MyBufferGeometry]
      .attributes
    val colorAttr = attrs
      .asInstanceOf[scala.scalajs.js.Dynamic]
      .selectDynamic( "a_color" )
    val colorData = colorAttr.selectDynamic( "array" ).asInstanceOf[Array[Float]]
    // apply color until face center offset
    ( 0 until faceSize-1 ).foreach { i =>
      colorData.update( 3*faceOffset+3*i,   color._1 )
      colorData.update( 3*faceOffset+3*i+1, color._2 )
      colorData.update( 3*faceOffset+3*i+2, color._3 )
    }
    // apply specific color at face center offset
    colorData.update( 3*faceOffset+3*faceSize-3, color._1 )
    colorData.update( 3*faceOffset+3*faceSize-2, color._2 )
    colorData.update( 3*faceOffset+3*faceSize-1, color._3 )
    colorAttr.updateDynamic( "needsUpdate" )( true )
  }

  private var bordersWidth = 1f

  @JSExport
  def setBordersWidth( brdrsWdth: Float ): Unit = {
    bordersWidth = math.min( 2, math.max( 0, brdrsWdth/10f ) )
  }

  // ******************** rendering ********************

  private def updateMeshMaterialValue( mesh: Mesh ) ( field: String, value: js.Any ): Unit = {
    mesh.material.asInstanceOf[ShaderMaterial].uniforms.asInstanceOf[scala.scalajs.js.Dynamic]
      .selectDynamic( field )
      .updateDynamic( "value" )( value )
  }

  private val pixels = new Uint8Array( 4 )

  @JSExport
  def pickRender( mouseX: Int, mouseY: Int ): Int = {
    renderer.render( pickScene, dummyCam )
    import js.DynamicImplicits._
    val gl = renderer.getContext().asInstanceOf[js.Dynamic]
    gl.readPixels( mouseX, innerHeight - mouseY, 1, 1, gl.RGBA, gl.UNSIGNED_BYTE, pixels )
    256 * 256 * pixels( 0 ) + 256 * pixels( 1 ) + pixels( 2 )
  }

  @JSExport
  def render( highlighted: Int ): Unit = {
    meshes.values.foreach { case ( m, pm ) =>
      val updateThis = updateMeshMaterialValue( m ) _
      updateThis( "u_time", 0f )
      updateThis( "u_borderWidth", bordersWidth )
      updateThis( "u_borderColor", new org.denigma.threejs.Vector3( 0.05,0.05,0.05 ) )
      updateThis( "u_highlightFlag", ( highlighted % 131072 ).toFloat )
      updateThis( "u_faceHighlightFlag", highlighted.toFloat )
      updateThis( "u_mvpMat", mvp )
    }
    updateMeshMaterialValue( axisMesh )( "u_mvpMat", mvp )
    renderer.render( scene, dummyCam )
  }
}
