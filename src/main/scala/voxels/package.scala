/**
 * Created by markus on 26/05/15.
 */
package object voxels {
  // add: prisms, pyramids (triangle, square, pentagon based), more?
  val standards = Map( 0 -> Tetrahedron
                     , 1 -> Cube
                     , 2 -> Octahedron
                     , 3 -> Dodecahedron
                     , 4 -> Icosahedron
                     , 5 -> TruncatedTetrahedron
                     , 6 -> TruncatedOctahedron
                     )
}
