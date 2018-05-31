package com.ethlo.quadkey;

import java.util.Objects;

public class Coordinate
{
    private final double lat;
    private final double lon;

    public Coordinate(double lat, double lon)
    {
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