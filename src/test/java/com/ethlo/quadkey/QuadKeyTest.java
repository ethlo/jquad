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

import java.util.List;

import org.junit.jupiter.api.Test;

class QuadKeyTest
{
    private static final Coordinate TIMES_SQUARE = new Coordinate(40.757678985595703, -73.969558715820312);
    private static final Coordinate QUEENSTOWN = new Coordinate(-44.902066, 168.091782);

    @Test
    void bingExample()
    {
        // From the Bing Maps tile system documentation: tile (3, 5) at level 3 has quadkey 213
        assertThat(QuadKey.ofTile(3, 5, 3)).hasToString("213");
        final QuadKey parsed = QuadKey.parse("213");
        assertThat(parsed.tileX()).isEqualTo(3);
        assertThat(parsed.tileY()).isEqualTo(5);
        assertThat(parsed.zoom()).isEqualTo(3);
    }

    @Test
    void knownValues()
    {
        assertThat(QuadKey.of(TIMES_SQUARE, QuadKey.MAX_ZOOM).toLong()).isEqualTo(1013670064444553679L);
        assertThat(QuadKey.of(TIMES_SQUARE, QuadKey.MAX_ZOOM)).hasToString("0320101101320312032222203213033");
        assertThat(QuadKey.of(TIMES_SQUARE, 16)).hasToString("0320101101320312");
    }

    @Test
    void roundTrips()
    {
        final QuadKey key = QuadKey.of(QUEENSTOWN, 23);
        assertThat(QuadKey.parse(key.toString())).isEqualTo(key);
        assertThat(QuadKey.fromLong(key.toLong(), 23)).isEqualTo(key);
        assertThat(QuadKey.ofTile(key.tileX(), key.tileY(), 23)).isEqualTo(key);
    }

    @Test
    void equalityIncludesTheZoomLevel()
    {
        // Same integer form, different tiles
        assertThat(QuadKey.parse("0").toLong()).isEqualTo(QuadKey.parse("00").toLong());
        assertThat(QuadKey.parse("0")).isNotEqualTo(QuadKey.parse("00"));
        assertThat(QuadKey.parse("0").hashCode()).isNotEqualTo(QuadKey.parse("00").hashCode());
    }

    @Test
    void rejectsInvalidInput()
    {
        assertThatThrownBy(() -> QuadKey.parse("0324")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("'4' at position 3");
        assertThatThrownBy(() -> QuadKey.parse("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QuadKey.parse("0".repeat(32))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QuadKey.fromLong(QuadKey.of(TIMES_SQUARE, 20).toLong(), 18)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QuadKey.fromLong(-1, 31)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QuadKey.ofTile(8, 0, 3)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QuadKey.of(TIMES_SQUARE, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QuadKey.of(TIMES_SQUARE, 32)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void hierarchy()
    {
        final QuadKey key = QuadKey.parse("0320101101");
        assertThat(key.parent()).hasToString("032010110");
        assertThat(key.atZoom(4)).hasToString("0320");
        assertThat(key.atZoom(10)).isEqualTo(key);
        assertThat(key.children()).extracting(QuadKey::toString)
                .containsExactly("03201011010", "03201011011", "03201011012", "03201011013");
        assertThat(key.children()).allSatisfy(child -> assertThat(child.parent()).isEqualTo(key));
        assertThatThrownBy(() -> QuadKey.parse("1").parent()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> key.atZoom(11)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QuadKey.of(TIMES_SQUARE, QuadKey.MAX_ZOOM).children()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void containsTiles()
    {
        final QuadKey key = QuadKey.parse("0320101101");
        assertThat(key.contains(key)).isTrue();
        assertThat(key.contains(QuadKey.parse("03201011012"))).isTrue();
        assertThat(key.contains(QuadKey.parse("032010110"))).isFalse();
        assertThat(key.contains(QuadKey.parse("0320101102"))).isFalse();
    }

    @Test
    void descendantsFormOneRange()
    {
        final QuadKey key = QuadKey.of(TIMES_SQUARE, 12);
        final long first = key.firstDescendant(QuadKey.MAX_ZOOM).toLong();
        final long last = key.lastDescendant(QuadKey.MAX_ZOOM).toLong();
        final long inside = QuadKey.of(TIMES_SQUARE, QuadKey.MAX_ZOOM).toLong();
        assertThat(inside).isBetween(first, last);
        assertThat(key.contains(QuadKey.fromLong(first, QuadKey.MAX_ZOOM))).isTrue();
        assertThat(key.contains(QuadKey.fromLong(last, QuadKey.MAX_ZOOM))).isTrue();
        assertThat(key.contains(QuadKey.fromLong(first - 1, QuadKey.MAX_ZOOM))).isFalse();
        assertThat(key.contains(QuadKey.fromLong(last + 1, QuadKey.MAX_ZOOM))).isFalse();
        assertThat(key.firstDescendant(12)).isEqualTo(key);
        assertThat(key.lastDescendant(12)).isEqualTo(key);
        assertThat(QuadKey.parse("3").lastDescendant(QuadKey.MAX_ZOOM)).hasToString("3".repeat(31));
    }

    @Test
    void containsCoordinates()
    {
        assertThat(QuadKey.of(TIMES_SQUARE, 16).contains(new Coordinate(40.756800, -73.970411))).isTrue();
        assertThat(QuadKey.of(TIMES_SQUARE, 16).contains(new Coordinate(40.759740, -73.965454))).isFalse();
        assertThat(QuadKey.of(QUEENSTOWN, 16).contains(new Coordinate(-44.900874, 168.094146))).isTrue();
        assertThat(QuadKey.of(QUEENSTOWN, 16).contains(new Coordinate(-44.902303, 168.098953))).isFalse();
    }

    @Test
    void edgePositionIsInExactlyOneTile()
    {
        final QuadKey west = QuadKey.of(TIMES_SQUARE, 10);
        final QuadKey east = QuadKey.ofTile(west.tileX() + 1, west.tileY(), 10);
        final BoundingBox box = west.boundingBox();
        final Coordinate onEdge = new Coordinate((box.southWest().lat() + box.northEast().lat()) / 2, box.northEast().lon());
        assertThat(List.of(west.contains(onEdge), east.contains(onEdge))).containsExactlyInAnyOrder(true, false);
        assertThat(east.contains(onEdge)).isTrue();
    }

    @Test
    void boundingBoxAndCenter()
    {
        final QuadKey key = QuadKey.of(TIMES_SQUARE, 12);
        final BoundingBox box = key.boundingBox();
        assertThat(box.contains(TIMES_SQUARE)).isTrue();
        assertThat(box.contains(key.center())).isTrue();
        assertThat(key.parent().boundingBox().contains(box.southWest())).isTrue();
        assertThat(key.parent().boundingBox().contains(box.northEast())).isTrue();
        assertThat(QuadKey.of(key.center(), 12)).isEqualTo(key);

        final Coordinate center = QuadKey.of(TIMES_SQUARE, QuadKey.MAX_ZOOM).center();
        assertThat(center.lat()).isCloseTo(TIMES_SQUARE.lat(), within(1e-7));
        assertThat(center.lon()).isCloseTo(TIMES_SQUARE.lon(), within(1e-7));
    }

    @Test
    void edgesOfTheMap()
    {
        // Beyond the Mercator limit, positions go to the top and bottom rows
        assertThat(QuadKey.of(89.9, 0, 3).tileY()).isZero();
        assertThat(QuadKey.of(-89.9, 0, 3).tileY()).isEqualTo(7);
        // Longitude 180 is in the last column, -180 in the first
        assertThat(QuadKey.of(0, 180, 3).tileX()).isEqualTo(7);
        assertThat(QuadKey.of(0, -180, 3).tileX()).isZero();
        assertThat(QuadKey.parse("0").boundingBox().northEast().lat()).isCloseTo(QuadKey.MAX_LATITUDE, within(1e-9));
    }
}
