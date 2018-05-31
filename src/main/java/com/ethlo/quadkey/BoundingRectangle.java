package com.ethlo.quadkey;

import java.util.Objects;

public class BoundingRectangle
{
    private final Coordinate lower;
    private final Coordinate upper;

    public BoundingRectangle(Coordinate lower, Coordinate upper)
    {
        this.lower = lower;
        this.upper = upper;
    }

    public Coordinate getLower()
    {
        return lower;
    }

    public Coordinate getUpper()
    {
        return upper;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(lower, upper);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        
        if (obj instanceof BoundingRectangle)
        {
            final BoundingRectangle b = (BoundingRectangle) obj;
            return Objects.equals(lower, b.lower) && Objects.equals(upper, b.upper);
        }
        
        return false;
    }

    @Override
    public String toString()
    {
        return "BoundingRectangle [lower=" + lower + ", upper=" + upper + "]";
    }
}
