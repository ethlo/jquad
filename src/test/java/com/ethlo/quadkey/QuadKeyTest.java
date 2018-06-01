package com.ethlo.quadkey;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.data.Percentage;
import org.junit.Ignore;
import org.junit.Test;

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
    public void quadInt2Coordinate()
    {
        final Coordinate coord = QuadKey.quadInt2Coordinate(quadInt);

        final Percentage deviation = Percentage.withPercentage(0.000001D);
        assertThat(coord.getLat()).isCloseTo(coordinate.getLat(), deviation);
        assertThat(coord.getLon()).isCloseTo(coordinate.getLon(), deviation);
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
        //final Coordinate c = QuadKey.point2WebMercator(new Point(123456, 789));
        //assertThat(c).isEqualTo(new Coordinate(-20035204.482983585, 20037493.618957378));
    }
    
    @Test
    public void webMercator2Point()
    {
        //assertThat(QuadKey.webMercator2Point(new Coordinate(-20035204.482983585, 20037493.618957378))).isEqualTo(new Point(123456, 789));
    }
    
    @Test
    public void quadInt2WebMercator()
    {
        //final Coordinate c = QuadKey.quadInt2WebMercator(quadInt);
        //assertThat(c).isEqualTo(new Coordinate(-8234253.610862966, 4976664.81813745));
    }
    
    @Test
    public void testCoordinate2Point90_90()
    {
        final Point p = QuadKey.coordinate2Point(new Coordinate(90.0, 90.0), QuadKey.MAX_ZOOM);
        assertThat(p).isEqualTo(new Point(1610612736, 2147483647));
    }
    
    @Ignore
    @Test
    public void boundingBox()
    {
        final Coordinate coordinate = new Coordinate(40.75896, -73.985195);
        System.out.println(coordinate);
        
        final long quadInt = QuadKey.coordinate2quadInt(coordinate);
        //final BoundingRectangle bbox = QuadKey.tile2bbox(quadInt, QuadKey.MAX_ZOOM);
        //System.out.println(bbox);
        
        final int distanceInMeters = 30;
        
        final BoundingRectangle rect = QuadKey.tile2bbox(quadInt, QuadKey.MAX_ZOOM);
        System.out.println(rect);
        
        System.out.println("x: " + QuadKey.coordinate2quadInt(coordinate));
        final long a = QuadKey.coordinate2quadInt(rect.getLower());
        final long b = QuadKey.coordinate2quadInt(rect.getUpper());
        System.out.println("a: " + a);
        System.out.println("b: " + b);
        //System.out.println(quadInt - range.getLower());
        //System.out.println(range.getUpper() - quadInt);
        
    }
}
