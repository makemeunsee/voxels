import scala.collection.immutable.TreeMap

/**
 * Created by markus on 26/05/15.
 */
package object voxels {
  val standards = TreeMap( (
    Tetrahedron ::
    Cube ::
    Octahedron ::   // made of 2 square pyramids
    Dodecahedron ::
    Icosahedron ::  // made of 1 5-antiprism and 2 pentagonal pyramids
    TruncatedTetrahedron ::
    TruncatedCube ::  // made of 6 square cupolas, 1 cube and 8 tetrahedrons
    TruncatedOctahedron ::
    TruncatedIcosahedron ::
    TriangularCupola ::
    Icosidodecahedron ::
    RhombicDodecahedron ::
    TrigonalTrapezohedron ::
    SquareCupola ::
    RhombicPrism ::
    Nil ++
    List( 3, 5, 6, 8 ).map( RegularPrism ) ++ // 4-prism = cube
    List( 4, 5, 6, 8 ).map( AntiPrism ) ++ // 3-antiprism = octahedron
    ( SquarePyramid :: PentagonalPyramid :: Nil )
  ).zipWithIndex.map { case ( v, i ) => ( i, v ) }: _* )

  // ElongatedDodecahedron: incompatible rhombi, cant join the fun
}
