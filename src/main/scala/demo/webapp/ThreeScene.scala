package demo.webapp

import demo.Colors
import geometry.Matrix4
import geometry.voronoi.{Face, VoronoiModel}
import maze.Maze
import org.denigma.threejs._

import scala.scalajs.js
import scala.scalajs.js.Array
import scala.scalajs.js.annotation.{JSExport, JSName}
import scala.scalajs.js.typedarray.{Float32Array, Uint32Array, Uint8Array}

/**
 * Created by markus on 17/06/2015.
 */
object ThreeScene {

  private val textureW = 256
  private val textureH = 128

  @JSName( "THREE.PlaneBufferGeometry" )
  class PlaneBufferGeometry( width: Float, height: Float ) extends Geometry

  // BufferGeometry from org.denigma.threejs does not extends Geometry, has to be redefined
  @JSName( "THREE.BufferGeometry" )
  class MyBufferGeometry extends Geometry {
    override def clone(): MyBufferGeometry = js.native
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

  def colorCode( faceId: Int ) = faceId +1

  def revertColorCode( colorCode: Int ): Int = {
    colorCode -1
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
  private val rtScene = new Scene

  @JSExport
  val renderer = new WebGLRenderer( ReadableWebGLRendererParameters )
//  renderer.setClearColor( new org.denigma.threejs.Color( 0.7, 0.7, 0.9 ) )

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

    adjustTexturing( innerWidth, innerHeight )
  }

  @JSExport
  def rotateView( deltaX: Int, deltaY: Int ): Unit = {
    modelMat = Matrix4.naiveRotMat( deltaX * 0.002, deltaY * 0.002 ) * modelMat
    updateMVP()
  }

  private def updateMVP(): Unit = {
    mvp = projMat * Matrix4.zoomMatrix( zoom ) * viewMat * modelMat
  }

  // ******************** axis management ********************

  private lazy val axisMesh: Mesh = makeAxisMesh
  private var showAxis: Boolean = false

  @JSExport
  def toggleAxis(): Unit = {
    showAxis = !showAxis
    if ( showAxis )
      rtScene.add( axisMesh )
    else
      rtScene.remove( axisMesh )
  }

  // ******************** mesh management ********************

  private var meshes: Option[( Mesh, Mesh )] = None

  def addModel( model: VoronoiModel, maze: Maze[Int] ): Unit = {
    meshes = Some( makeMesh( model, maze ) )
    updateMeshCulling()
    scene.add( meshes.get._1 )
    pickScene.add( meshes.get._2 )
  }

//  def clear(): Unit = {
//    for ( ( m, pm ) <- meshes ) {
//      scene.remove( m )
//      pickScene.remove( pm )
//      m.geometry.dispose()
//      pm.geometry.dispose()
//    }
//    meshes = None
//    rtScene.remove( axisMesh )
//    screenMesh.foreach( rtScene.remove( _ ) )
//    showAxis = false
//  }

  private def updateMeshCulling(): Unit = {
    for( ( m, pm ) <- meshes ) {
      val culling = if ( cullback ) 0 else 2 // 2 = org.denigma.threejs.THREE.DoubleSide
      m.material.asInstanceOf[js.Dynamic].updateDynamic( "side" )( culling )
      pm.material.asInstanceOf[js.Dynamic].updateDynamic( "side" )( culling )
    }
  }

  private def makeScreenMesh: Mesh = {
    val plane = new PlaneBufferGeometry( 2, 2 )

    val customUniforms = js.Dynamic.literal(
      "texture" -> js.Dynamic.literal("type" -> "t", "value" -> renderingTexture )
    )
    val shaderMaterial = new ShaderMaterial
    shaderMaterial.uniforms = customUniforms
    shaderMaterial.vertexShader =
      """varying vec2 vUv;
        |void main() {
        |    vUv = position.xy;
        |    gl_Position = vec4( 2.0*position-1.0, 1.0 );
        |}
      """.stripMargin
    shaderMaterial.fragmentShader =
      """uniform sampler2D texture;
        |varying vec2 vUv;
        |void main(){
        |    gl_FragColor = texture2D( texture, vUv );
        |}
      """.stripMargin
    shaderMaterial.depthWrite = false

    assembleMesh( plane, shaderMaterial, "screenMesh" )
  }

  private def makeWireFrameMesh( m: VoronoiModel ): ( Mesh, Mesh ) = {
    val customUniforms = js.Dynamic.literal(
      "u_time" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_borderWidth" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_mvpMat" -> js.Dynamic.literal( "type" -> "m4", "value" -> new org.denigma.threejs.Matrix4() ),
      "u_borderColor" -> js.Dynamic.literal( "type" -> "v3", "value" -> new Vector4( 0, 0, 0, 1 ) ),
      "u_faceHighlightFlag" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 )
    )

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
    shaderMaterial.wireframe = true
    // useless until acceptance and release of https://github.com/mrdoob/three.js/pull/6778
//    shaderMaterial.asInstanceOf[js.Dynamic].updateDynamic( "wireframeLinewidth" )( 3f )

    val pickShaderMaterial = new ShaderMaterial
    pickShaderMaterial.attributes = attrs
    pickShaderMaterial.uniforms = customUniforms
    pickShaderMaterial.vertexShader = Shaders.pickVertexShader
    pickShaderMaterial.fragmentShader = Shaders.pickFragmentShader

    val count = m.faces.map( _.vertices.length*3 ).sum
    val indicesCount = m.faces.map( _.vertices.length*2 ).sum
    val pickIndicesCount = m.faces.map( f => ( f.vertices.length - 2 )*3 ).sum

    val vertices = new Float32Array( count )
    val normals = new Float32Array( count )
    val colors = new Float32Array( count )
    val pickColors = new Float32Array( count / 3 )
    val centerFlags = new Float32Array( count / 3 )
    val indices = new Uint32Array( indicesCount )
    val pickIndices = new Uint32Array( pickIndicesCount )
    var offset = 0
    var indicesOffset = 0
    var pickIndicesOffset = 0

    for ( i <- m.faces.indices ; f = m.faces( i ) ) {
      val n = f.seed
      val pickColor = colorCode( i )
      val ( r, g, b ) = Colors.intColorToFloatsColors( f.color )

      val triOffset = offset*3
      val vSize = f.vertices.length

      for ( j <- 0 until vSize ) {
        vertices.set( triOffset+3*j,   f.vertices( j ).x.toFloat )
        vertices.set( triOffset+3*j+1, f.vertices( j ).y.toFloat )
        vertices.set( triOffset+3*j+2, f.vertices( j ).z.toFloat )
        normals.set( triOffset+3*j,   n.x.toFloat )
        normals.set( triOffset+3*j+1, n.y.toFloat )
        normals.set( triOffset+3*j+2, n.z.toFloat )
        colors.set( triOffset+3*j,   r )
        colors.set( triOffset+3*j+1, g )
        colors.set( triOffset+3*j+2, b )
        centerFlags.set( offset+j, 0f )
        pickColors.set( offset+j, pickColor )
        indices.set( indicesOffset+2*j,   offset+j )
        indices.set( indicesOffset+2*j+1, offset+( j+1 )%vSize )
        if ( j < vSize-1 ) {
          pickIndices.set( pickIndicesOffset + 3 * j, offset )
          pickIndices.set( pickIndicesOffset + 3 * j + 1, offset + j + 1 )
          pickIndices.set( pickIndicesOffset + 3 * j + 2, offset + j + 2 )
        }
      }

      offset = offset + vSize
      indicesOffset = indicesOffset + vSize*2
      pickIndicesOffset = pickIndicesOffset + ( vSize - 2 )*3
    }

    val geom = new MyBufferGeometry()
    geom.addAttribute( "index", new BufferAttribute( indices, 1 ) )
    geom.addAttribute( "a_centerFlag", new BufferAttribute( centerFlags, 1 ) )
    geom.addAttribute( "a_normal", new BufferAttribute( normals, 3 ) )
    geom.addAttribute( "a_color", new BufferAttribute( colors, 3 ) )
    geom.addAttribute( "a_pickColor", new BufferAttribute( pickColors, 1 ) )
    geom.addAttribute( "position", new BufferAttribute( vertices, 3 ) )

    val pickGeom = geom.clone()
    pickGeom.addAttribute( "index", new BufferAttribute( pickIndices, 1 ) )

    ( assembleMesh( geom, shaderMaterial, "baseMesh" ), assembleMesh( pickGeom, pickShaderMaterial, "pickMesh" ) )
  }

  private def makeMesh( m: VoronoiModel, maze: Maze[Int] ): ( Mesh, Mesh ) = {
    val ( depthMap, minDepth, maxDepth ) = maze.toDepthMap
    val depthSpan = maxDepth - minDepth
//    def rainbow = Colors.matteoNiccoliRainbow
    def rainbow = Colors.lessAngryRainbow( 256, m.faces.length.toFloat / 100 ) _

    val customUniforms = js.Dynamic.literal(
      "u_time" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_borderWidth" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_mvpMat" -> js.Dynamic.literal( "type" -> "m4", "value" -> new org.denigma.threejs.Matrix4() ),
      "u_borderColor" -> js.Dynamic.literal( "type" -> "v3", "value" -> new Vector4( 0, 0, 0, 1 ) ),
      "u_faceHighlightFlag" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_explosionFactor" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_depthScale" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 )
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
      ),
      "a_depth" -> js.Dynamic.literal(
        "type" -> "f",
        "value" -> new Array[Float]
      )
    )

    val shaderMaterial = new ShaderMaterial
    shaderMaterial.attributes = attrs
    shaderMaterial.uniforms = customUniforms
    shaderMaterial.vertexShader = Shaders.vertexShader
    shaderMaterial.fragmentShader = Shaders.fragmentShader

    val pickShaderMaterial = new ShaderMaterial
    pickShaderMaterial.attributes = attrs
    pickShaderMaterial.uniforms = customUniforms
    pickShaderMaterial.vertexShader = Shaders.pickVertexShader
    pickShaderMaterial.fragmentShader = Shaders.pickFragmentShader

    val count = m.faces.map( _.vertices.length*3+3 ).sum
    val indicesCount = m.faces.map( _.vertices.length*3 ).sum

    val vertices = new Float32Array( count )
    val normals = new Float32Array( count )
    val colors = new Float32Array( count )
    val pickColors = new Float32Array( count / 3 )
    val centerFlags = new Float32Array( count / 3 )
    val depths = new Float32Array( count / 3 )
    val indices = new Uint32Array( indicesCount )
    var offset = 0
    var indicesOffset = 0

    for ( i <- m.faces.indices ; f = m.faces( i ) ) {
      val n = f.seed
      val c = f.barycenter
      val uniformD = ( depthMap( i ).toFloat - minDepth ) / depthSpan

      val pickColor = colorCode( i )
      val ( r, g, b ) = rainbow( uniformD ) //Colors.intColorToFloatsColors( f.color )
      val ( cr, cg, cb ) = ( r, g, b ) //Colors.intColorToFloatsColors( f.centerColor )

      val triOffset = offset*3
      val vSize = f.vertices.length


      for ( j <- 0 until vSize ) {
        vertices.set( triOffset+3*j,   f.vertices( j ).x.toFloat )
        vertices.set( triOffset+3*j+1, f.vertices( j ).y.toFloat )
        vertices.set( triOffset+3*j+2, f.vertices( j ).z.toFloat )
        normals.set( triOffset+3*j,   n.x.toFloat )
        normals.set( triOffset+3*j+1, n.y.toFloat )
        normals.set( triOffset+3*j+2, n.z.toFloat )
        colors.set( triOffset+3*j,   r )
        colors.set( triOffset+3*j+1, g )
        colors.set( triOffset+3*j+2, b )
        centerFlags.set( offset+j, 0f )
        depths.set( offset+j, uniformD )
        pickColors.set( offset+j, pickColor )
        indices.set( indicesOffset+3*j,   offset+vSize )
        indices.set( indicesOffset+3*j+1, offset+j )
        indices.set( indicesOffset+3*j+2, offset+( j+1 )%vSize )
      }
      vertices.set( triOffset+3*vSize,   c.x.toFloat )
      vertices.set( triOffset+3*vSize+1, c.y.toFloat )
      vertices.set( triOffset+3*vSize+2, c.z.toFloat )
      normals.set( triOffset+3*vSize,   n.x.toFloat )
      normals.set( triOffset+3*vSize+1, n.y.toFloat )
      normals.set( triOffset+3*vSize+2, n.z.toFloat )
      colors.set( triOffset+3*vSize,   cr )
      colors.set( triOffset+3*vSize+1, cg )
      colors.set( triOffset+3*vSize+2, cb )
      centerFlags.set( offset+vSize, 1f )
      depths.set( offset+vSize, uniformD )
      pickColors.set( offset+vSize, pickColor )

      offset = offset + vSize + 1
      indicesOffset = indicesOffset + vSize*3
    }

    geom.addAttribute( "index", new BufferAttribute( indices, 1 ) )
    geom.addAttribute( "a_centerFlag", new BufferAttribute( centerFlags, 1 ) )
    geom.addAttribute( "a_depth", new BufferAttribute( depths, 1 ) )
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

    val count = 18
    val indicesCount = 9

    val vertices = new Float32Array( count )
    val colors = new Float32Array( count )
    val indices = new Uint32Array( indicesCount )

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

  private var downsampling = 1

  @JSExport
  def setDownsampling( dwnSmplng: Int ): Unit = {
    downsampling = math.pow( 2, math.max( 0, math.min( 7, dwnSmplng ) ) ).toInt
    adjustTexturing( innerWidth, innerHeight )
  }

  private val screenMesh: Mesh = makeScreenMesh
  rtScene.add( screenMesh )

  private var renderingTexture = makeTexture( textureW, textureH )

  private def makeTexture( w: Int, h: Int ): WebGLRenderTarget = {
    val t = new WebGLRenderTarget( w, h )
    //    t.asInstanceOf[js.Dynamic].updateDynamic( "format" )( 1020d ) // RGB
    t.asInstanceOf[js.Dynamic].updateDynamic( "minFilter" )( 1006d ) // Linear, needed for non power of 2 sizes
    t.asInstanceOf[js.Dynamic].updateDynamic( "magFilter" )( 1003d ) // Nearest. Comment for smoother rendering
    t
  }

  private def adjustTexturing( w: Int, h: Int ): Unit = {
    renderingTexture = makeTexture( w / downsampling, h / downsampling )
  }

  def colorFace( faceOffset: Int
               , faceSize: Int
               , color: ( Float, Float, Float )
               , centerColor: ( Float, Float, Float ) ): Unit = {

    for ( ( mesh, _ ) <- meshes ) {
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
  }

  private var bordersWidth = 1f

  @JSExport
  def setBordersWidth( brdrsWdth: Float ): Unit = {
    bordersWidth = math.min( 2, math.max( 0, brdrsWdth/10f ) )
  }

  private var explosionFactor = 0f

  @JSExport
  def setExplosion( explosionFctr: Float ): Unit = {
    val f = math.min( 100, math.max( 0, explosionFctr ) ) / 100
    explosionFactor = f*f*10
  }

  private var cullback = false

  @JSExport
  def toggleCullback(): Unit = {
    cullback = !cullback
    updateMeshCulling()
  }

  private var depthScale = 0.5f

  @JSExport
  def setDepthScale( depthScl: Float ): Unit = {
    depthScale = math.min( 100, math.max( -100, depthScl ) ) / 100
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
    for ( ( _, m ) <- meshes ) {
      val updateThis = updateMeshMaterialValue( m ) _
      updateThis( "u_time", 0f )
      updateThis( "u_explosionFactor", explosionFactor )
      updateThis( "u_depthScale", depthScale )
      updateThis( "u_mvpMat", mvp )
    }
    renderer.render( pickScene, dummyCam )
    import js.DynamicImplicits._
    val gl = renderer.getContext().asInstanceOf[js.Dynamic]
    gl.readPixels( mouseX, innerHeight - mouseY, 1, 1, gl.RGBA, gl.UNSIGNED_BYTE, pixels )
    256 * 256 * pixels( 0 ) + 256 * pixels( 1 ) + pixels( 2 )
  }

  @JSExport
  def render( highlighted: Int ): Unit = {
    for ( ( m, _ ) <- meshes ) {
      val updateThis = updateMeshMaterialValue( m ) _
      updateThis( "u_time", 0f )
      updateThis( "u_explosionFactor", explosionFactor )
      updateThis( "u_depthScale", depthScale )
      updateThis( "u_mvpMat", mvp )
      updateThis( "u_borderWidth", bordersWidth )
      updateThis( "u_borderColor", new org.denigma.threejs.Vector3( 0,0,0 ) )
      updateThis( "u_faceHighlightFlag", highlighted.toFloat )
    }
    updateMeshMaterialValue( axisMesh )( "u_mvpMat", mvp )

    renderer.clearColor()
    renderer.render( scene, dummyCam, renderingTexture )
    updateMeshMaterialValue( screenMesh )( "texture", renderingTexture )
    renderer.render( rtScene, dummyCam )
  }
}
