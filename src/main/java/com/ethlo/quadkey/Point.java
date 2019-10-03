package com.ethlo.quadkey;

import java.util.Objects;

public class Point
{
    private final long x;
    private final long y;

    public Point(long x, long y)
    {
        this.x = x;
        this.y = y;
    }

    public long getX()
    {
        return x;
    }

    public long getY()
    {
        return y;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(x, y);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Point)
        {
            final Point b = (Point) obj;
            return Objects.equals(x, b.x) && Objects.equals(y, b.y);
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "Point [x=" + x + ", y=" + y + "]";
    }
}