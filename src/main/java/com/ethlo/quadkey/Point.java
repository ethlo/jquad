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
