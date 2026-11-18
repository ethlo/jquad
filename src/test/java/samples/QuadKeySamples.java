package samples;

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

import org.junit.Test;

import com.ethlo.quadkey.Coordinate;
import com.ethlo.quadkey.QuadKey;

public class QuadKeySamples
{
    /**
     * Create a quadkey from a coordinate and a zoom level. Zoom level 18 is about 0.6 m per pixel. A quadkey has a
     * 64-bit integer form and a string form with one digit per zoom level.
     */
    @Test
    public void fromCoordinate()
    {
        final Coordinate c = new Coordinate(40.757678, -73.969558);
        final QuadKey qk = new QuadKey(c, 18);

        assertThat(qk.getAsLong()).isEqualTo(15104861027L);
        assertThat(qk.getAsString()).isEqualTo("032010110132031203");
    }

    /**
     * Check whether a coordinate lies within the tile of a quadkey.
     */
    @Test
    public void containment()
    {
        final Coordinate c1 = new Coordinate(40.757678, -73.969558);
        final Coordinate c2 = new Coordinate(40.757608, -73.969436);
        final QuadKey qk = new QuadKey(c1, 16);

        assertThat(qk.contains(c2)).isTrue();
    }

    /**
     * A tile at a lower zoom level contains the tiles at higher zoom levels whose quadkeys start with its own.
     */
    @Test
    public void parentTile()
    {
        final Coordinate c = new Coordinate(40.757678, -73.969558);
        final QuadKey parent = new QuadKey(c, 12);
        final QuadKey child = new QuadKey(c, 18);

        assertThat(parent.getAsString()).isEqualTo("032010110132");
        assertThat(parent.isParentOf(child)).isTrue();
        assertThat(child.isParentOf(parent)).isFalse();
    }
}
