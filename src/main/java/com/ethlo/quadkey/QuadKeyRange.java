package com.ethlo.quadkey;

import java.util.Objects;

public class QuadKeyRange
{
    private final long lower;
    private final long upper;

    public QuadKeyRange(long lower, long upper)
    {
        this.lower = lower;
        this.upper = upper;
    }

    public long getLower()
    {
        return lower;
    }

    public long getUpper()
    {
        return upper;
    }
    
    public boolean contains(Coordinate coordinate)
    {
        final long quadKey = QuadKey.coordinate2quadInt(coordinate);
        return quadKey >= lower && quadKey <= upper;
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
        
        if (obj instanceof QuadKeyRange)
        {
            final QuadKeyRange b = (QuadKeyRange) obj;
            return Objects.equals(lower, b.lower) && Objects.equals(upper, b.upper);
        }
        
        return false;
    }

    @Override
    public String toString()
    {
        return "QuadKeyRange [lower=" + lower + ", upper=" + upper + "]";
    }
}
