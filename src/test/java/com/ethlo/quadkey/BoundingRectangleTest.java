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

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.data.Percentage;
import org.junit.Test;

public class BoundingRectangleTest
{
    private final double lon = 9.754514;
    private final double lat = 49.528439;
    private final Coordinate coordinate = new Coordinate(lat, lon);

    @Test
    public void contains()
    {
        Coordinate lowerLeft = new Coordinate(49.512503, 9.719324);
        Coordinate upperRight = new Coordinate(49.558402, 9.803266);
        BoundingRectangle bbox = new BoundingRectangle(lowerLeft, upperRight);
        assertThat(bbox.contains(coordinate));
    }

}
