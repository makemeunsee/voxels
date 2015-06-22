import scala.collection.immutable.TreeMap

/**
 * Created by markus on 26/05/15.
 */
package object voxels {
  val standards = TreeMap( (
    Tetrahedron ::
    Cube ::
    Octahedron ::
    Dodecahedron ::
    Icosahedron ::
    TruncatedTetrahedron ::
    TruncatedCube ::
    TruncatedOctahedron ::
    TruncatedIcosahedron ::
    TriangularCupola ::
    Icosidodecahedron ::
    RhombicDodecahedron ::
    // ElongatedDodecahedron // incompatible rhombi ::
    TrigonalTrapezohedron ::
    SquareCupola ::
    RhombicPrism ::
    Nil ++
    List( 3, 5, 6, 8 ).map( RegularPrism ) ++ // 4 prism = cube
    List( 4, 5, 6, 8 ).map( AntiPrism ) ++ // 3 antiprism = octahedron
    ( SquarePyramid :: PentagonalPyramid :: Nil )
  ).zipWithIndex.map { case ( v, i ) => ( i, v ) }: _* )
}
