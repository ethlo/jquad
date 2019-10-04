package com.ethlo.quadkey;

/*-
 * #%L
 * jquad
 * %%
 * Copyright (C) 2018 - 2019 Morten Haraldsen (ethlo)
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

public class Geoutil
{
    /**
     * Get bouding rectangle using Drupal Earth Algorithm
     *
     * @see https://www.rit.edu/drupal/api/drupal/sites%21all%21modules%21location%21earth.inc/7.54
     * @param lat
     * @param lng
     * @param distance
     * @return
     */
    public static BoundingRectangle getBoundingRectangle(final Coordinate coordinate, int distance)
    {
        final double lat = Math.toRadians(coordinate.getLat());
        final double lng = Math.toRadians(coordinate.getLon());
        final double radius = earthRadiusInMeters(lat);
        final Range<Double> retLats = earthLatitudeRange(lat, radius, distance);
        final Range<Double> retLngs = earthLongitudeRange(lat, lng, radius, distance);
        return new BoundingRectangle(new Coordinate(retLats.getLower(), retLngs.getLower()), new Coordinate(retLats.getUpper(), retLngs.getUpper()));
    }

    /**
     * Calculate latitude range based on earths radius at a given point
     *
     * @param latitude
     * @param longitude
     * @param distance
     * @return
     */
    private static Range<Double> earthLatitudeRange(double radiansLat, double earthRadiusAtLatitude, double distance)
    {
        // Estimate the min and max latitudes within distance of a given
        // location.

        double angle = distance / earthRadiusAtLatitude;
        double minlat = radiansLat - angle;
        double maxlat = radiansLat + angle;
        double rightangle = Math.PI / 2;
        // Wrapped around the south pole.
        if (minlat < -rightangle)
        {
            double overshoot = -minlat - rightangle;
            minlat = -rightangle + overshoot;
            if (minlat > maxlat)
            {
                maxlat = minlat;
            }
            minlat = -rightangle;
        }
        // Wrapped around the north pole.
        if (maxlat > rightangle)
        {
            double overshoot = maxlat - rightangle;
            maxlat = rightangle - overshoot;
            if (maxlat < minlat)
            {
                minlat = maxlat;
            }
            maxlat = rightangle;
        }
        return new Range<Double>(Math.toDegrees(minlat), Math.toDegrees(maxlat));
    }

    private static Range<Double> earthLongitudeRange(double radianLat, double radianlng, double earthRadius, int distance)
    {
        final double radius = earthRadius * Math.cos(radianLat);

        double angle;
        if (radius > 0)
        {
            angle = Math.abs(distance / radius);
            angle = Math.min(angle, Math.PI);
        }
        else
        {
            angle = Math.PI;
        }
        double minLon = radianlng - angle;
        double maxLon = radianlng + angle;
        if (minLon < -Math.PI)
        {
            minLon = minLon + Math.PI * 2;
        }
        if (maxLon > Math.PI)
        {
            maxLon = maxLon - Math.PI * 2;
        }

        return new Range<Double>(Math.toDegrees(minLon), Math.toDegrees(maxLon));
    }

    /**
     * Calculate earth radius at given latitude
     */
    public static Double earthRadiusInMeters(final double latitude)
    {
        final double lat = Math.toRadians(latitude);
        final double x = Math.cos(lat) / 6378137.0;
        final double y = Math.sin(lat) / (6378137.0 * (1 - (1 / 298.257223563)));
        return (1 / (Math.sqrt(x * x + y * y)));
    }
}
