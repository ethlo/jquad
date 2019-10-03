package com.ethlo.quadkey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.assertj.core.data.Percentage;
import org.junit.Test;

public class QuadKeyTest {
    private final Coordinate coordinate1 = new Coordinate(40.757678985595703, -73.969558715820312);
    private final Coordinate coordinate2 = new Coordinate(-44.902066, 168.091782);

    private final long quadInt1 = 1013670064444553679L;

    @Test
    public void quadInt2Coordinate() {
        QuadKey k = new QuadKey(coordinate1, QuadKey.MAX_ZOOM);
        final Coordinate coord = k.getAsCoordinate();

        final Percentage deviation = Percentage.withPercentage(0.000001D);
        assertThat(coord.getLat()).isCloseTo(coordinate1.getLat(), deviation);
        assertThat(coord.getLon()).isCloseTo(coordinate1.getLon(), deviation);
    }

    @Test
    public void coordinate2QuadInt() {
        QuadKey k = new QuadKey(coordinate1, QuadKey.MAX_ZOOM);
        assertThat(k.getAsLong()).isEqualTo(quadInt1);
    }


    @Test
    public void coordinate2QuadKey() {
        QuadKey k = new QuadKey(coordinate1, 16);
        String quadKey1 = "0320101101320312032222203213033";
        assertThat(k.getAsString()).isEqualTo(quadKey1.substring(0, 16));
    }


    @Test
    public void quadKey2QuadInt() {
        QuadKey k = new QuadKey(coordinate1, QuadKey.MAX_ZOOM);
        assertThat(k.getAsLong()).isEqualTo(quadInt1);
    }

    @Test
    public void containsChild() {
        QuadKey k1 = new QuadKey(coordinate1, 10);
        QuadKey k2 = new QuadKey(coordinate1, 14);
        assertTrue(k1.contains(k2.getAsCoordinate()));
    }

    @Test
    public void isParent() {
        QuadKey k1 = new QuadKey(coordinate1, 10);
        QuadKey k2 = new QuadKey(coordinate1, 11);
        assertTrue(k1.isParentOf(k2));
    }

    @Test
    public void setZoom() {
        QuadKey k1 = new QuadKey(coordinate1, QuadKey.MAX_ZOOM);
        QuadKey k2 = new QuadKey(coordinate1, QuadKey.MAX_ZOOM);
        k2.setZoom(10);
        assertThat(k2.getAsString().length()).isEqualTo(10);
        assertThat(k1).isNotEqualTo(k2);
    }

    @Test
    public void setInvalidZoom() {
        QuadKey k = new QuadKey(coordinate1, 10);
        try {
            k.setZoom(11);
            fail("Exception not thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void quadKeyContains1() {
        final Coordinate other = new Coordinate(40.756800, -73.970411);
        QuadKey k = new QuadKey(coordinate1, 16);
        assertTrue(k.contains(other));
    }

    @Test
    public void quadKeyNotContains1() {
        final Coordinate other = new Coordinate(40.759740, -73.965454);
        QuadKey k = new QuadKey(coordinate1, 16);
        assertFalse(k.contains(other));
    }

    @Test
    public void quadKeyContains2() {
        final Coordinate other = new Coordinate(-44.900874, 168.094146);
        QuadKey k = new QuadKey(coordinate2, 16);
        assertTrue(k.contains(other));
    }

    @Test
    public void quadKeyNotContains2() {
        final Coordinate other = new Coordinate(-44.902303, 168.098953);
        QuadKey k = new QuadKey(coordinate2, 16);
        assertFalse(k.contains(other));
    }

}
