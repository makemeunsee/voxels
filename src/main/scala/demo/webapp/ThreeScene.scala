package demo.webapp

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
    faces.foldLeft( ( 0, Seq.empty[( Face, Int, Int )] ) ) { case ( ( offset, acc ), f ) =>
      // counting the centers which is automatically added to the meshes
      val l = f.vertices.length + 1
      val newOffset = offset + l
      ( newOffset, ( f, offset, l ) +: acc )
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

  // ******************** mesh building functions ********************

  private def makeMesh( m: VoronoiModel, depthsMap: Map[Int, Int], depthMax: Int ): Mesh = {
    val customUniforms = js.Dynamic.literal(
      "u_borderWidth" -> js.Dynamic.literal( "type" -> "1f", "value" -> 0 ),
      "u_mMat" -> js.Dynamic.literal( "type" -> "m4", "value" -> new org.denigma.threejs.Matrix4() ),
      "u_borderColor" -> js.Dynamic.literal( "type" -> "v3", "value" -> new Vector3( 0, 0, 0 ) ),
      "u_faceHighlightFlag" -> js.Dynamic.literal( "type" -> "1i", "value" -> 0 )
    )

    val geom = new MyBufferGeometry()

    val attrs = js.Dynamic.literal(
      "a_color" -> js.Dynamic.literal(
        "type" -> "v3",
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
    shaderMaterial.vertexShader = Shaders.cells_vertex_hammer_aitoff
    shaderMaterial.fragmentShader = Shaders.cells_fragment_projs
    shaderMaterial.transparent = true

    // 0 = cullback
    // 1 = cullfront
    // 2 = cullnone
    shaderMaterial.asInstanceOf[js.Dynamic].updateDynamic( "side" )( 0 )

    val count = m.faces.map( _.vertices.length*3+3 ).sum
    val indicesCount = m.faces.map( _.vertices.length*3 ).sum

    val vertices = new Float32Array( count )
    val colors = new Float32Array( count )
    val centerFlags = new Float32Array( count / 3 )
    val indices = new Uint32Array( indicesCount )
    var offset = 0
    var indicesOffset = 0

    for ( i <- m.faces.indices ; f = m.faces( i ) ) {
      val ( r, g, b ) = ( 1f, 1f, 1f )
      val ( cr, cg, cb ) = ( r, g, b )

      val triOffset = offset*3
      val vSize = f.vertices.length

      // edge vertices

      for ( j <- 0 until vSize ) {
        vertices.set( triOffset+3*j,   f.vertices( j ).x.toFloat )
        vertices.set( triOffset+3*j+1, f.vertices( j ).y.toFloat )
        vertices.set( triOffset+3*j+2, f.vertices( j ).z.toFloat )
        colors.set( triOffset+3*j,   r )
        colors.set( triOffset+3*j+1, g )
        colors.set( triOffset+3*j+2, b )
        centerFlags.set( offset+j, 0f )
        indices.set( indicesOffset+3*j,   offset+vSize )
        indices.set( indicesOffset+3*j+1, offset+j )
        indices.set( indicesOffset+3*j+2, offset+( j+1 )%vSize )
      }

      val c = f.barycenter

      // centers, one per face
      vertices.set( triOffset+3*vSize,   c.x.toFloat )
      vertices.set( triOffset+3*vSize+1, c.y.toFloat )
      vertices.set( triOffset+3*vSize+2, c.z.toFloat )
      colors.set( triOffset+3*vSize,   cr )
      colors.set( triOffset+3*vSize+1, cg )
      colors.set( triOffset+3*vSize+2, cb )
      centerFlags.set( offset+vSize, 1f )

      offset = offset + vSize + 1
      indicesOffset = indicesOffset + vSize*3
    }

    geom.addAttribute( "index", new BufferAttribute( indices, 1 ) )
    geom.addAttribute( "a_centerFlag", new BufferAttribute( centerFlags, 1 ) )
    geom.addAttribute( "a_color", new BufferAttribute( colors, 3 ) )
    geom.addAttribute( "position", new BufferAttribute( vertices, 3 ) )

    assembleMesh( geom, shaderMaterial, "baseMesh" )
  }

  private def junction( face0: Face, face1: Face ): geometry.Vector3 = {
    face0.vertices.intersect( face1.vertices ) match {
      case v0 :: v1 :: Nil =>
        ( v0 + v1 ) * 0.5
      case _ =>
        throw new Error( "Illegal argument" )
    }
  }

  private def makeMazeMesh( m: VoronoiModel, flatMaze: Map[( Int, Int ), Set[( Int, Int )]], depthMax: Int ): Mesh = {
    val customUniforms = js.Dynamic.literal(
      "u_mMat" -> js.Dynamic.literal( "type" -> "m4", "value" -> new org.denigma.threejs.Matrix4() ),
      "u_color" -> js.Dynamic.literal( "type" -> "v3", "value" -> new Vector3() )
    )

    val geom = new MyBufferGeometry()

    val attrs = js.Dynamic.literal()

    val shaderMaterial = new ShaderMaterial
    shaderMaterial.attributes = attrs
    shaderMaterial.uniforms = customUniforms
    shaderMaterial.vertexShader = Shaders.maze_vertex_hammer_aitoff
    shaderMaterial.fragmentShader = Shaders.maze_fragment_projs
    shaderMaterial.wireframe = true
    // useless until acceptance and release of https://github.com/mrdoob/three.js/pull/6778
    //    shaderMaterial.asInstanceOf[js.Dynamic].updateDynamic( "wireframeLinewidth" )( 3f )

    val size = flatMaze.size
    // each maze node has 1 parent, except the root of the maze
    val relations = size - 1
    // there are as many vertices as node, plus 1 for each relation (the junction point)
    val count = 3*( relations + size )
    // there 2 segments = 4 indices for each relation
    val indicesCount = 4 * relations

    val id: ( () => Int ) = {
      var counter = 0
      () => {
        counter = counter+1
        counter-1
      }
    }

    // assign an indice to each node and each relation
    val mazeWithIndices = flatMaze.map { case ( p, children ) =>
      ( p._1, ( p._2, id(), children.map { case child =>
        ( child._1, child._2, id() )
      } ) )
    }

    val vertices = new Float32Array( count )
    val indices = new Uint32Array( indicesCount )

    var indicesOffset = 0

    for ( ( node, ( d, id, children ) ) <- mazeWithIndices ) {
      val f = m.faces( node )

      // face center point
      val bary = f.barycenter * 1.001
      vertices.set( id*3,   bary.x.toFloat )
      vertices.set( id*3+1, bary.y.toFloat )
      vertices.set( id*3+2, bary.z.toFloat )

      for( ( child, childD, relationId ) <- children ) {
        val ( _, childId, _ ) = mazeWithIndices( child )

        // junction point
        val j = junction( f, m.faces( child ) ) * 1.001
        vertices.set( relationId*3,   j.x.toFloat )
        vertices.set( relationId*3+1, j.y.toFloat )
        vertices.set( relationId*3+2, j.z.toFloat )

        indices.set( indicesOffset, id )
        indices.set( indicesOffset+1, relationId )
        indices.set( indicesOffset+2, relationId )
        indices.set( indicesOffset+3, childId )

        indicesOffset += 4
      }
    }

    geom.addAttribute( "index", new BufferAttribute( indices, 1 ) )
    geom.addAttribute( "position", new BufferAttribute( vertices, 3 ) )

    assembleMesh( geom, shaderMaterial, "mazeMesh" )
  }

  private def assembleMesh( geometry: Geometry, material: Material, name: String ): Mesh = {
    val mesh = new Mesh
    mesh.geometry = geometry
    mesh.material = material
    mesh.frustumCulled = false
    mesh.name = name
    mesh
  }
}

import demo.webapp.ThreeScene._

class ThreeScene( cfg: Config ) {

  // ******************** three scene basics ********************

  // dummy cam as projections are handled manually
  private val dummyCam = new Camera
  private val scene = new Scene
  private val rtScene = new Scene

  @JSExport
  val renderer = new WebGLRenderer( ReadableWebGLRendererParameters )
  import demo.Colors.jsStringToFloats
  renderer.setClearColor( new org.denigma.threejs.Color( cfg.`Background color`._1
                                                       , cfg.`Background color`._2
                                                       , cfg.`Background color`._3 ) )

  // ******************** view management ********************

  private var modelMat = Matrix4.naiveRotMat( 0.5, 0.3 )

  private var innerWidth: Int = 0
  private var innerHeight: Int = 0

  @JSExport
  def updateViewport( width: Int, height: Int ): Unit = {
    println( "viewport", width, height )
    innerWidth = width
    innerHeight = height

    adjustTexturing( innerWidth, innerHeight )
  }

  @JSExport
  def rotateView( deltaX: Int, deltaY: Int ): Unit = {
    val speed = 1f / 500f
    modelMat = Matrix4.naiveRotMat( deltaX * speed, deltaY * speed ) * modelMat
  }

  // ******************** mesh management ********************

  private var mesh: Option[Mesh] = None
  private var mazeMesh: Option[Mesh] = None

  def setModel( model: VoronoiModel, maze: Maze[Int], depthsMap: Map[Int, Int], depthMax: Int ): Unit = {
    mesh.foreach { case oldM =>
      scene.remove( oldM )
      oldM.geometry.dispose()
    }
    val m = makeMesh( model, depthsMap, depthMax )
    mesh = Some( m )
    if ( cfg.`Draw cells` ) {
      scene.add( m )
    }
    setCellsShader( cfg.`Projection` )
    setMaze( model, maze, depthMax )
  }

  def setMaze( model: VoronoiModel, maze: Maze[Int], depthMax: Int ): Unit = {
    mazeMesh.foreach { m =>
      scene.remove( m )
      m.geometry.dispose()
    }
    val mm = makeMazeMesh( model, maze.childrenMap, depthMax )
    mazeMesh = Some( mm )
    setMazeShader( cfg.`Projection` )
    if ( cfg.`Draw path` )
      scene.add( mm )
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

  // ******************** special effects ********************

  private def setCellsShader( str: String ): Unit = {
    mesh.foreach { m =>
      val shader = str match {
        case Config.hammerAitoff =>
          Shaders.cells_vertex_hammer_aitoff
        case _ =>
          Shaders.cells_vertex_spherical
      }
      m.material.asInstanceOf[ShaderMaterial].vertexShader = shader
      m.material.needsUpdate = true
    }
  }

  private def setMazeShader( str: String ): Unit = {
    mazeMesh.foreach { mm =>
      val shader = str match {
        case Config.hammerAitoff =>
          Shaders.maze_vertex_hammer_aitoff
        case _ =>
          Shaders.maze_vertex_spherical
      }
      mm.material.asInstanceOf[ShaderMaterial].vertexShader = shader
      mm.material.needsUpdate = true
    }
  }

  def setShader( str: String ): Unit = {
    setCellsShader( str )
    setMazeShader( str )
  }

  def toggleMazePath(): Unit = {
    if ( cfg.`Draw path` )
      mazeMesh.foreach( scene.add )
    else
      mazeMesh.foreach( scene.remove )
  }

  def toggleCells(): Unit = {
    if ( cfg.`Draw cells` )
      mesh.foreach { scene.add }
    else
      mesh.foreach { scene.remove }
  }

  def setBackground( color: Int ): Unit = {
    import demo.Colors.intColorToFloatsColors
    val ( r, g, b ): ( Float, Float, Float ) = color
    renderer.setClearColor( new org.denigma.threejs.Color( r, g, b ) )
  }

  def udpateDownsampling(): Unit = {
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
    val downsampling = cfg.safeDownsamplingFactor
    renderingTexture.dispose()
    renderingTexture = makeTexture( w / downsampling, h / downsampling )
  }

  def updateFaceColor( faceOffset: Int
                     , faceSize: Int
                     , color: ( Float, Float, Float )
                     , centerColor: ( Float, Float, Float ) ): Unit = {

    for ( m <- mesh ) {
      val attrs = m
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

  // ******************** rendering ********************

  private def updateMeshMaterialValue( mesh: Mesh ) ( field: String, value: js.Any ): Unit = {
    mesh.material.asInstanceOf[ShaderMaterial].uniforms.asInstanceOf[scala.scalajs.js.Dynamic]
      .selectDynamic( field )
      .updateDynamic( "value" )( value )
  }

  @JSExport
  def renderNoTexture(): Unit = {
    renderToTexture( None )
  }

  @JSExport
  def render(): Unit = {
    renderToTexture( Some( renderingTexture ) )
    updateMeshMaterialValue( screenMesh )( "texture", renderingTexture )
    renderer.render( rtScene, dummyCam )
  }

  private def renderToTexture( texture: Option[WebGLRenderTarget] ): Unit = {
    if ( cfg.`Draw cells` )
      for ( m <- mesh ) {
        val updateThis = updateMeshMaterialValue( m ) _
        updateThis( "u_mMat", modelMat )
        updateThis( "u_borderWidth", cfg.safeBordersWidth )
        import demo.Colors.jsStringToFloats
        val ( r, g, b ): ( Float, Float, Float ) = cfg.`Borders color`
        updateThis( "u_borderColor", new org.denigma.threejs.Vector3( r, g, b ) )
      }

    if ( cfg.`Draw path` )
      for( mm <- mazeMesh ) {
        val updateThis = updateMeshMaterialValue( mm ) _
        updateThis( "u_mMat", modelMat )
        import demo.Colors.jsStringToFloats
        val ( r, g, b ): ( Float, Float, Float ) = cfg.`Path color`
        updateThis( "u_color", new org.denigma.threejs.Vector3( r, g, b ) )
      }

    renderer.clearColor()
    texture match {
      case Some( t ) => renderer.render( scene, dummyCam, t )
      case None      => renderer.render( scene, dummyCam )
    }

  }
}
