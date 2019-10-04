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

import java.util.Objects;

public class Coordinate
{
    private final double lat;
    private final double lon;

    public Coordinate(double lat, double lon)
    {
        Assert.isTrue(lat <= 90.0, "Latitude must be less or equal to 180.0, was " + lat);
        Assert.isTrue(lat >= -90.0, "Latitude must be greater or equal to -180.0, was " + lat);
        Assert.isTrue(lon <= 180.0, "Longitude must be less or equal to 90.0, was " + lon);
        Assert.isTrue(lon >= -180.0, "Longitude must be greater or equal to -90.0, was " + lon);


        this.lat = lat;
        this.lon = lon;
    }

    public double getLat()
    {
        return lat;
    }

    public double getLon()
    {
        return lon;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(lat, lon);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Coordinate)
        {
            final Coordinate b = (Coordinate) obj;
            return Objects.equals(lat, b.lat) && Objects.equals(lon, b.lon);
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "Coordinate [lat=" + lat + ", lon=" + lon + "]";
    }
}
