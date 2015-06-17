package demo.webapp

import demo.Rnd
import geometry.{Vec3, Matrix4}
import org.denigma.threejs._
import voxels.Voxel
import voxels.Voxel.Face

import scala.scalajs.js
import scala.scalajs.js.Array
import scala.scalajs.js.annotation.{JSExport, JSName}
import scala.scalajs.js.typedarray.{Uint16Array, Float32Array}

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
    preserveDrawingBuffer = true
  }

  // zip faces of a voxel with their offset and size as jn the meshes data buffers
  def withOffsetsAndSizes( faces: List[Face] ): List[( Face, Int, Int )] = {
    faces.foldLeft( ( 0, List.empty[( Face, Int, Int )] ) ) { case ( ( offset, acc ), f ) =>
      // counting the centers which is automatically added to the meshes
      val l = f.vertices.length + 1
      val newOffset = offset + l
      ( newOffset, ( f, offset, l ) :: acc )
    }._2
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

import ThreeScene._

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
  private var zoom = 1d

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

  @JSExport
  def updateViewport( width: Int, height: Int ): Unit = {
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

  // ******************** mesh management ********************

  private var meshes = Map.empty[Int, ( Mesh, Mesh )]

  def addVoxel( id: Int, voxel: Voxel )( implicit rndColors: Boolean ): Unit = {
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
  }

  def clear(): Unit = {
    meshes.foreach { case ( _, ( m, pm ) ) =>
      scene.remove( m )
      pickScene.remove( pm )
      // TODO check if m.geometry.dispose() and such are useful
    }
    meshes = Map.empty
  }

  private def makeMesh( v: Voxel, vId: Int )( implicit rndColors: Boolean ): ( Mesh, Mesh ) = {
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
      val ( r, g, b ) = if ( rndColors ) Rnd.rndColor() else ( 1f, 1f, 1f )
      val ( cr, cg, cb ) = if ( rndColors ) Rnd.rndColor() else ( 1f, 1f, 1f )

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
      colors.set( triOffset+3*vSize,   cr )
      colors.set( triOffset+3*vSize+1, cg )
      colors.set( triOffset+3*vSize+2, cb )
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

    ( assembleMesh( geom, shaderMaterial ), assembleMesh( geom, pickShaderMaterial ) )
  }

  private def assembleMesh( geometry: MyBufferGeometry, material: ShaderMaterial ): Mesh = {
    val mesh = new Mesh
    mesh.geometry = geometry
    mesh.material = material
    mesh.frustumCulled = false
    mesh
  }

  // ******************** special effects ********************

  def colorFace( voxelId: Int
                 , faceOffset: Int
                 , faceSize: Int
                 , color: ( Float, Float, Float )
                 , centerColor: ( Float, Float, Float ) ): Unit = {
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
    colorData.update( 3*faceOffset+3*faceSize-3, centerColor._1 )
    colorData.update( 3*faceOffset+3*faceSize-2, centerColor._2 )
    colorData.update( 3*faceOffset+3*faceSize-1, centerColor._3 )
    colorAttr.updateDynamic( "needsUpdate" )( true )
  }

  private var showBorders = true

  @JSExport
  def toggleBorders(): Unit = {
    showBorders = !showBorders
  }

  // ******************** rendering ********************

  @JSExport
  def pickRender() = renderer.render( pickScene, dummyCam )

  private def updateMeshMaterialValue( mesh: Mesh ) ( field: String, value: js.Any ): Unit = {
    mesh.material.asInstanceOf[ShaderMaterial].uniforms.asInstanceOf[scala.scalajs.js.Dynamic]
      .selectDynamic( field )
      .updateDynamic( "value" )( value )
  }

  @JSExport
  def render( highlighted: Int ) = {
    meshes.values.foreach { case ( m, pm ) =>
      val updateThis = updateMeshMaterialValue( m ) _
      updateThis( "u_time", 0f )
      updateThis( "u_borderWidth", if ( showBorders ) 1f else 0f )
      updateThis( "u_borderColor", new Vector3( 0.05,0.05,0.05 ) )
      updateThis( "u_highlightFlag", ( highlighted % 131072 ).toFloat )
      updateThis( "u_faceHighlightFlag", highlighted.toFloat )
      updateThis( "u_mvpMat", mvp )
    }
    renderer.render( scene, dummyCam )
  }
}
