package com.ethlo.quadkey;

import org.junit.Test;

import com.ethlo.quadkey.QuadKey.Coordinate;
import com.ethlo.quadkey.QuadKey.Point;

import static org.assertj.core.api.Assertions.assertThat;

public class QuadKeyTest
{
    private final double lon = -73.969558715820312;
    private final double lat = 40.757678985595703;
    private final Coordinate coordinate = new Coordinate(lat, lon);
    private final Point point = new Point(632496219, 807059307);
    private final long quadInt = 1013670064444553679L;
    
    @Test
    public void coordinate2Point()
    {
        final Point p = QuadKey.coordinate2Point(coordinate, QuadKey.MAX_ZOOM);
        assertThat(p).isEqualTo(point);
    }
    
    @Test
    public void coordinate2quadInt()
    {
        final long l = QuadKey.coordinate2quadInt(coordinate);
        assertThat(l).isEqualTo(quadInt);
    }
    
    @Test
    public void quadInt2Point()
    {
        final Point p = QuadKey.quadInt2Point(quadInt);
        assertThat(p).isEqualTo(point);
    }
    
    @Test
    public void point2WebMercator()
    {
        final Coordinate c = QuadKey.point2WebMercator(new Point(123456, 789));
        assertThat(c).isEqualTo(new Coordinate(-20035204.482983585, 20037493.618957378));
    }
    
    @Test
    public void webMercator2Point()
    {
        assertThat(QuadKey.webMercator2Point(new Coordinate(-20035204.482983585, 20037493.618957378))).isEqualTo(new Point(123456, 789));
    }
    
    @Test
    public void quadInt2WebMercator()
    {
        final Coordinate c = QuadKey.quadInt2WebMercator(quadInt);
        assertThat(c).isEqualTo(new Coordinate(-8234253.610862966, 4976664.81813745));
    }
}
