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

public class BoundingRectangle
{
    private final Coordinate lowerLeft;
    private final Coordinate upperRight;

    public BoundingRectangle(Coordinate lowerRight, Coordinate upperLeft)
    {
        this.lowerLeft = lowerRight;
        this.upperRight = upperLeft;
    }

    public Coordinate getLowerLeft()
    {
        return lowerLeft;
    }

    public Coordinate getUpperRight()
    {
        return upperRight;
    }

    /* Todo: Make sure this holds for all points on the sphere */
    public boolean contains(Coordinate coordinate)
    {
        return lowerLeft.getLat() <= coordinate.getLat() &&
            lowerLeft.getLon() <= coordinate.getLon() &&
            upperRight.getLat() >= coordinate.getLat() &&
            upperRight.getLon() >= coordinate.getLon();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(lowerLeft, upperRight);
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
            return Objects.equals(lowerLeft, b.lowerLeft) && Objects.equals(upperRight, b.upperRight);
        }

        return false;
    }

    @Override
    public String toString()
    {
        return "BoundingRectangle [lowerLeft=" + lowerLeft + ", upperRight=" + upperRight + "]";
    }
}
