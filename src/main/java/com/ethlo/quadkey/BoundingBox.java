package com.ethlo.quadkey;

/*-
 * #%L
 * jquad
 * %%
 * Copyright (C) 2018 - 2026 Morten Haraldsen (ethlo)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Objects;

/**
 * An area between two latitudes and two longitudes, edges included.
 *
 * <p>A box whose western longitude is greater than its eastern one crosses the antimeridian: from 170 to -170 is the
 * 20 degrees around longitude 180, not the 340 degrees in between.</p>
 *
 * @param southWest the corner with the lowest latitude and the western longitude
 * @param northEast the corner with the highest latitude and the eastern longitude
 */
public record BoundingBox(Coordinate southWest, Coordinate northEast)
{
    /**
     * Mean earth radius in meters, as used by the haversine formula
     */
    private static final double EARTH_RADIUS = 6_371_008.8;

    public BoundingBox
    {
        Objects.requireNonNull(southWest, "southWest cannot be null");
        Objects.requireNonNull(northEast, "northEast cannot be null");
        if (southWest.lat() > northEast.lat())
        {
            throw new IllegalArgumentException("The south latitude " + southWest.lat() + " is north of the north latitude " + northEast.lat());
        }
    }

    /**
     * The smallest box that contains every point within the given distance of a center. Near the poles the box spans
     * all longitudes, and near the antimeridian it crosses it.
     *
     * @param center the center
     * @param meters the distance from the center, on a sphere with the earth's mean radius
     * @return the box
     */
    public static BoundingBox around(Coordinate center, double meters)
    {
        Objects.requireNonNull(center, "center cannot be null");
        if (!(meters >= 0) || Double.isInfinite(meters))
        {
            throw new IllegalArgumentException("Distance must be a non-negative number of meters, was " + meters);
        }

        // Bounding box of a spherical cap, after J. P. Matuschek, "Finding Points Within a Distance of a
        // Latitude/Longitude Using Bounding Coordinates"
        final double angle = meters / EARTH_RADIUS;
        final double lat = Math.toRadians(center.lat());
        final double south = lat - angle;
        final double north = lat + angle;
        if (south <= -Math.PI / 2 || north >= Math.PI / 2)
        {
            // A pole is inside the circle, so every longitude is
            return new BoundingBox(
                    new Coordinate(Math.toDegrees(Math.max(south, -Math.PI / 2)), -180),
                    new Coordinate(Math.toDegrees(Math.min(north, Math.PI / 2)), 180));
        }

        final double deltaLon = Math.toDegrees(Math.asin(Math.sin(angle) / Math.cos(lat)));
        final double west = wrap(center.lon() - deltaLon);
        final double east = wrap(center.lon() + deltaLon);
        return new BoundingBox(new Coordinate(Math.toDegrees(south), west), new Coordinate(Math.toDegrees(north), east));
    }

    private static double wrap(double lon)
    {
        if (lon < -180)
        {
            return lon + 360;
        }
        if (lon > 180)
        {
            return lon - 360;
        }
        return lon;
    }

    /**
     * @return whether the box crosses the antimeridian, at longitude 180
     */
    public boolean crossesAntimeridian()
    {
        return southWest.lon() > northEast.lon();
    }

    /**
     * @param coordinate the position
     * @return whether the position is inside the box or on its edge
     */
    public boolean contains(Coordinate coordinate)
    {
        final double lat = coordinate.lat();
        final double lon = coordinate.lon();
        if (lat < southWest.lat() || lat > northEast.lat())
        {
            return false;
        }
        return crossesAntimeridian()
                ? lon >= southWest.lon() || lon <= northEast.lon()
                : lon >= southWest.lon() && lon <= northEast.lon();
    }
}
