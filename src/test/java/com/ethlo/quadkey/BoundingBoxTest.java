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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.util.Random;

import org.junit.jupiter.api.Test;

class BoundingBoxTest
{
    private static final double METERS_PER_DEGREE = 6_371_008.8 * Math.PI / 180;

    @Test
    void contains()
    {
        final BoundingBox box = new BoundingBox(new Coordinate(49.512503, 9.719324), new Coordinate(49.558402, 9.803266));
        assertThat(box.contains(new Coordinate(49.528439, 9.754514))).isTrue();
        assertThat(box.contains(new Coordinate(49.528439, 9.9))).isFalse();
        assertThat(box.contains(new Coordinate(49.6, 9.754514))).isFalse();
        assertThat(box.contains(box.southWest())).isTrue();
        assertThat(box.contains(box.northEast())).isTrue();
    }

    @Test
    void around()
    {
        final Coordinate center = new Coordinate(59.9139, 10.7522);
        final BoundingBox box = BoundingBox.around(center, 1000);
        assertThat(box.northEast().lat() - box.southWest().lat()).isCloseTo(2000 / METERS_PER_DEGREE, within(1e-9));
        assertThat(box.contains(center)).isTrue();
        // 999 m north, south, east and west of the center
        final double dLat = 999 / METERS_PER_DEGREE;
        final double dLon = dLat / Math.cos(Math.toRadians(center.lat()));
        assertThat(box.contains(new Coordinate(center.lat() + dLat, center.lon()))).isTrue();
        assertThat(box.contains(new Coordinate(center.lat() - dLat, center.lon()))).isTrue();
        assertThat(box.contains(new Coordinate(center.lat(), center.lon() + dLon))).isTrue();
        assertThat(box.contains(new Coordinate(center.lat(), center.lon() - dLon))).isTrue();
        assertThat(box.contains(new Coordinate(center.lat() + 2 * dLat, center.lon()))).isFalse();
        assertThat(box.crossesAntimeridian()).isFalse();
    }

    @Test
    void aroundContainsItsCenter()
    {
        // Also for a distance of 0, where converting the latitude to radians and back used to move both edges
        final Coordinate oslo = new Coordinate(59.9139, 10.7522);
        assertThat(BoundingBox.around(oslo, 0).contains(oslo)).isTrue();

        final Random random = new Random(1);
        for (int i = 0; i < 100_000; i++)
        {
            final Coordinate center = new Coordinate(random.nextDouble() * 180 - 90, random.nextDouble() * 360 - 180);
            final double meters = i % 2 == 0 ? 0 : random.nextDouble() * 1_000_000;
            assertThat(BoundingBox.around(center, meters).contains(center)).as("%s, %s m", center, meters).isTrue();
        }
    }

    @Test
    void aroundTheAntimeridian()
    {
        final Coordinate center = new Coordinate(0, 179.999);
        final BoundingBox box = BoundingBox.around(center, 1000);
        assertThat(box.crossesAntimeridian()).isTrue();
        assertThat(box.contains(center)).isTrue();
        assertThat(box.contains(new Coordinate(0, -179.995))).isTrue();
        assertThat(box.contains(new Coordinate(0, 0))).isFalse();
    }

    @Test
    void aroundAPole()
    {
        final BoundingBox box = BoundingBox.around(new Coordinate(89.99, 25), 5000);
        assertThat(box.northEast().lat()).isEqualTo(90);
        assertThat(box.southWest().lon()).isEqualTo(-180);
        assertThat(box.northEast().lon()).isEqualTo(180);
        assertThat(box.contains(new Coordinate(89.99, -155))).isTrue();
    }

    @Test
    void rejectsInvalidBoxes()
    {
        assertThatThrownBy(() -> new BoundingBox(new Coordinate(10, 0), new Coordinate(5, 1))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BoundingBox.around(new Coordinate(0, 0), -1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BoundingBox.around(new Coordinate(0, 0), Double.NaN)).isInstanceOf(IllegalArgumentException.class);
    }
}
