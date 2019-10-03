package com.ethlo.quadkey;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.data.Percentage;
import org.junit.Test;

public class BoundingRectangleTest {
    private final double lon = 9.754514;
    private final double lat = 49.528439;
    private final Coordinate coordinate = new Coordinate(lat, lon);

    @Test
    public void contains() {
        Coordinate lowerLeft = new Coordinate(49.512503, 9.719324);
        Coordinate upperRight = new Coordinate(49.558402, 9.803266);
        BoundingRectangle bbox = new BoundingRectangle(lowerLeft, upperRight);
        assertThat(bbox.contains(coordinate));
    }

}
