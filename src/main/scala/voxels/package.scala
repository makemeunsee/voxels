/**
 * Created by markus on 26/05/15.
 */
package object voxels {
  // add: prisms, pyramids (triangle, square, pentagon based), more?
  val standards = List( Tetrahedron,
                        Cube,
                        Octahedron,
                        Dodecahedron,
                        Icosahedron,
                        TruncatedTetrahedron,
                        TruncatedCube,
                        TruncatedOctahedron,
                        Cuboctahedron,
                        RhombicDodecahedron,
                        // ElongatedDodecahedron
                        TrigonalTrapezohedron
                        ).zipWithIndex.map { case ( v, i ) => ( i, v ) }.toMap
}
