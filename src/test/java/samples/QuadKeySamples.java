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

import org.junit.jupiter.api.Test;

import com.ethlo.quadkey.BoundingBox;
import com.ethlo.quadkey.Coordinate;
import com.ethlo.quadkey.QuadKey;

class QuadKeySamples
{
    /**
     * The tile that contains a position at a zoom level, from 1 to 31. Zoom level 18 is about 0.6 m per pixel. A
     * quadkey has a string form with one digit per zoom level, and a 64-bit integer form.
     */
    @Test
    void fromCoordinate()
    {
        final QuadKey key = QuadKey.of(40.757678, -73.969558, 18);

        assertThat(key.toString()).isEqualTo("032010110132031203");
        assertThat(key.toLong()).isEqualTo(15104861027L);
        assertThat(key.zoom()).isEqualTo(18);
    }

    /**
     * Read either form back. The integer form does not say which zoom level it is at, so it needs one.
     */
    @Test
    void parse()
    {
        assertThat(QuadKey.parse("032010110132031203")).isEqualTo(QuadKey.fromLong(15104861027L, 18));

        // Map tile coordinates: column from the west, row from the north
        final QuadKey tile = QuadKey.ofTile(3, 5, 3);
        assertThat(tile.toString()).isEqualTo("213");
        assertThat(tile.tileX()).isEqualTo(3);
        assertThat(tile.tileY()).isEqualTo(5);
    }

    /**
     * A tile contains the positions inside it. A position on the edge between two tiles is in exactly one of them,
     * the same one that `QuadKey.of` returns.
     */
    @Test
    void containment()
    {
        final QuadKey key = QuadKey.of(40.757678, -73.969558, 16);

        assertThat(key.contains(new Coordinate(40.757608, -73.969436))).isTrue();
        assertThat(key.contains(new Coordinate(40.759740, -73.965454))).isFalse();
    }

    /**
     * Move between zoom levels. A tile contains itself and every tile whose key starts with its own.
     */
    @Test
    void hierarchy()
    {
        final QuadKey key = QuadKey.parse("032010110132");

        assertThat(key.parent().toString()).isEqualTo("03201011013");
        assertThat(key.atZoom(4).toString()).isEqualTo("0320");
        assertThat(key.children()).hasSize(4).allSatisfy(child -> assertThat(key.contains(child)).isTrue());
        assertThat(key.contains(QuadKey.parse("032010110132031203"))).isTrue();
    }

    /**
     * Store positions as quadkeys at the deepest zoom level. All positions inside a tile are then one contiguous
     * range of numbers, so finding them is a single range query on an index, for example
     * `WHERE quadkey BETWEEN ? AND ?`.
     */
    @Test
    void databaseRange()
    {
        final QuadKey area = QuadKey.parse("032010110132");
        final long from = area.firstDescendant(QuadKey.MAX_ZOOM).toLong();
        final long to = area.lastDescendant(QuadKey.MAX_ZOOM).toLong();

        final long stored = QuadKey.of(40.757678, -73.969558, QuadKey.MAX_ZOOM).toLong();
        assertThat(stored).isBetween(from, to);
    }

    /**
     * The area of a tile, and a search area around a position. A search area near longitude 180 crosses it, and one
     * near a pole covers every longitude.
     */
    @Test
    void areas()
    {
        final BoundingBox tile = QuadKey.parse("032010110132").boundingBox();
        assertThat(tile.contains(new Coordinate(40.757678, -73.969558))).isTrue();

        final BoundingBox nearby = BoundingBox.around(new Coordinate(59.9139, 10.7522), 500);
        assertThat(nearby.contains(new Coordinate(59.9127, 10.7461))).isTrue();

        assertThat(BoundingBox.around(new Coordinate(-17.7134, 179.999), 1000).crossesAntimeridian()).isTrue();
    }
}
