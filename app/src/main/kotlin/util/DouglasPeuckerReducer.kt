package util

import java.util.ArrayList

import android.location.Location

/**
 * Reduces the number of points in a shape using the Douglas-Peucker algorithm.
 * From:
 * http://www.phpriot.com/articles/reducing-map-path-douglas-peucker-algorithm/4
 * Ported from PHP to Java. "marked" array added to optimize.

 * @author M.Kergall
 * *
 * * Modified by baldur@mapzen.com
 */
public object DouglasPeuckerReducer {

    /**
     * Reduce the number of points in a shape using the Douglas-Peucker
     * algorithm

     * @param shape
     * *            The shape to reduce
     * *
     * @param tolerance
     * *            The tolerance to decide whether or not to keep a point, in the
     * *            coordinate system of the points (micro-degrees here)
     * *
     * @return the reduced shape
     */
    public fun reduceWithTolerance(shape: List<Location>, tolerance: Double): List<Location> {
        val n = shape.size()
        // if a shape has 2 or less points it cannot be reduced
        if (tolerance <= 0 || n < 3) {
            return shape
        }

        val marked = BooleanArray(n) //vertex indexes to keep will be marked as "true"
        for (i in 1..n - 1 - 1) {
            marked[i] = false
        }
        // automatically add the first and last point to the returned shape
        marked[0] = true
        marked[n - 1] = true

        // the first and last points in the original shape are
        // used as the entry point to the algorithm.
        douglasPeuckerReduction(shape, // original shape
                marked, // reduced shape
                tolerance, // tolerance
                0, // index of first point
                n - 1 // index of last point
        )

        // all done, return the reduced shape
        val newShape = ArrayList<Location>(n) // the new shape to return
        for (i in 0..n - 1) {
            if (marked[i]) {
                newShape.add(shape.get(i))
            }
        }
        return newShape
    }

    /**
     * Reduce the points in shape between the specified first and last index.
     * Mark the points to keep in marked[]

     * @param shape
     * *            The original shape
     * *
     * @param marked
     * *            The points to keep (marked as true)
     * *
     * @param tolerance
     * *            The tolerance to determine if a point is kept
     * *
     * @param firstIdx
     * *            The index in original shape's point of the starting point for
     * *            this line segment
     * *
     * @param lastIdx
     * *            The index in original shape's point of the ending point for
     * *            this line segment
     */
    private fun douglasPeuckerReduction(shape: List<Location>, marked: BooleanArray, tolerance: Double, firstIdx: Int, lastIdx: Int) {
        if (lastIdx <= firstIdx + 1) {
            // overlapping indexes, just return
            return
        }

        // loop over the points between the first and last points
        // and find the point that is the farthest away

        var maxDistance = 0.0
        var indexFarthest = 0

        val firstPoint = shape.get(firstIdx)
        val lastPoint = shape.get(lastIdx)

        for (idx in firstIdx + 1..lastIdx - 1) {
            val point = shape.get(idx)

            val distance = orthogonalDistance(point, firstPoint, lastPoint)

            // keep the point with the greatest distance
            if (distance > maxDistance) {
                maxDistance = distance
                indexFarthest = idx
            }
        }

        if (maxDistance > tolerance) {
            //The farthest point is outside the tolerance: it is marked and the algorithm continues.
            marked[indexFarthest] = true

            // reduce the shape between the starting point to newly found point
            douglasPeuckerReduction(shape, marked, tolerance, firstIdx, indexFarthest)

            // reduce the shape between the newly found point and the finishing point
            douglasPeuckerReduction(shape, marked, tolerance, indexFarthest, lastIdx)
        }
        //else: the farthest point is within the tolerance, the whole segment is discarded.
    }

    /**
     * Calculate the orthogonal distance from the line joining the lineStart and
     * lineEnd points to point

     * @param point
     * *            The point the distance is being calculated for
     * *
     * @param lineStart
     * *            The point that starts the line
     * *
     * @param lineEnd
     * *            The point that ends the line
     * *
     * @return The distance in points coordinate system
     */
    public fun orthogonalDistance(point: Location, lineStart: Location, lineEnd: Location): Double {
        val area = Math.abs((1.0 * lineStart.getLatitude() * 1e6 * lineEnd.getLongitude() * 1e6+1.0 * lineEnd.getLatitude() * 1e6 * point.getLongitude() * 1e6+1.0 * point.getLatitude() * 1e6 * lineStart.getLongitude() * 1e6-1.0 * lineEnd.getLatitude() * 1e6 * lineStart.getLongitude() * 1e6-1.0 * point.getLatitude() * 1e6* lineEnd.getLongitude() * 1e6-1.0 * lineStart.getLatitude() * 1e6 * point.getLongitude() * 1e6)/2.0)

        val bottom = Math.hypot(lineStart.getLatitude() * 1e6 - lineEnd.getLatitude() * 1e6, lineStart.getLongitude() * 1e6-lineEnd.getLongitude() * 1e6)

        return (area / bottom * 2.0)
    }
}