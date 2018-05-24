package com.ethlo.quadkey;

import java.util.Objects;

public class QuadKey
{
    public static final int MAX_ZOOM = 31;
    private static final double MAX_LONGITUDE = 180.0;

    /* (2*atan(exp(M_PI))*180.0/M_PI - 90.0) */
    private static final double MAX_LATITUDE = 85.05112877980659;
    private static final double MIN_LONGITUDE = -MAX_LONGITUDE;
    private static final double MIN_LATITUDE = -MAX_LATITUDE;

    private static final double WEBMERCATOR_R = 6378137.0;

    /* (double)((uint32)1 << MAX_ZOOM) */
    private static final double XY_SCALE = 2147483648.0;
    private static final double INV_XY_SCALE = 1.0 / XY_SCALE;
    private static final double WM_RANGE = 2.0 * Math.PI * WEBMERCATOR_R;
    private static final double INV_WM_RANGE = 1.0 / WM_RANGE;
    
    private QuadKey(){}

    public static long xy2quadint(Point point)
    {
        final long[] b =
        { 0x5555555555555555L, 0x3333333333333333L, 0x0F0F0F0F0F0F0F0FL, 0x00FF00FF00FF00FFL, 0x0000FFFF0000FFFFL };
        final long[] s =
        { 1, 2, 4, 8, 16 };

        long x = (point.x | (point.x << s[4])) & b[4];
        long y = (point.y | (point.y << s[4])) & b[4];

        x = (x | (x << s[3])) & b[3];
        y = (y | (y << s[3])) & b[3];

        x = (x | (x << s[2])) & b[2];
        y = (y | (y << s[2])) & b[2];

        x = (x | (x << s[1])) & b[1];
        y = (y | (y << s[1])) & b[1];

        x = (x | (x << s[0])) & b[0];
        y = (y | (y << s[0])) & b[0];

        return x | (y << 1);
    }

    public static Point quadInt2Point(long quadint)
    {
        final long[] b =
        { 0x5555555555555555L, 0x3333333333333333L, 0x0F0F0F0F0F0F0F0FL, 0x00FF00FF00FF00FFL, 0x0000FFFF0000FFFFL, 0x00000000FFFFFFFFL };
        final int[] s =
        { 0, 1, 2, 4, 8, 16 };

        long x = quadint;
        long y = quadint >> 1;

        x = (x | (x >> s[0])) & b[0];
        y = (y | (y >> s[0])) & b[0];

        x = (x | (x >> s[1])) & b[1];
        y = (y | (y >> s[1])) & b[1];

        x = (x | (x >> s[2])) & b[2];
        y = (y | (y >> s[2])) & b[2];

        x = (x | (x >> s[3])) & b[3];
        y = (y | (y >> s[3])) & b[3];

        x = (x | (x >> s[4])) & b[4];
        y = (y | (y >> s[4])) & b[4];

        x = (x | (x >> s[5])) & b[5];
        y = (y | (y >> s[5])) & b[5];

        return new Point(x, y);
    }

    public static Point coordinate2Point(Coordinate coordinate, int zoom)
    {
        final double lon = Math.min(MAX_LONGITUDE, Math.max(MIN_LONGITUDE, coordinate.lon));
        final double lat = Math.min(MAX_LATITUDE, Math.max(MIN_LATITUDE, coordinate.lat));

        double fx = (lon + 180.0) / 360.0;
        double sinlat = Math.sin(lat * Math.PI / 180.0);
        double fy = 0.5 - Math.log((1 + sinlat) / (1 - sinlat)) / (4 * Math.PI);

        long mapsize = (1 << zoom) & 0xFFFFFFFFL;
        long x = ((long) Math.floor(fx * mapsize));
        long y = ((long) Math.floor(fy * mapsize));
        x = Math.min(mapsize - 1, Math.max(0, x));
        y = Math.min(mapsize - 1, Math.max(0, y));
        return new Point(x, y);
    }

    public static Coordinate point2WebMercator(Point p)
    {
        final double x = (p.x * INV_XY_SCALE - 0.5) * WM_RANGE;
        final double y = (0.5 - p.y * INV_XY_SCALE) * WM_RANGE;
        return new Coordinate(x, y);
    }

    public static Coordinate quadInt2WebMercator(long quadint)
    {
        final Point p = quadInt2Point(quadint);
        return point2WebMercator(p);
    }

    public static Point webMercator2Point(Coordinate coordinate)
    {
        final double x = (coordinate.lat * INV_WM_RANGE + 0.5) * XY_SCALE;
        final double y = (0.5 - coordinate.lon * INV_WM_RANGE) * XY_SCALE;
        return new Point((long)x, (long)y);
    }

    public static long coordinate2quadInt(Coordinate coordinate)
    {
        final Point point = coordinate2Point(coordinate, MAX_ZOOM);
        return xy2quadint(point);
    }

    public static class Point
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

    public static class Coordinate
    {
        private final double lat;
        private final double lon;

        public Coordinate(double lat, double lon)
        {
            this.lat = lat;
            this.lon = lon;
        }

        public double getLat()
        {
            return lat;
        }

        public double getL()
        {
            return lon;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(lat, lon);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof Coordinate)
            {
                final Coordinate b = (Coordinate) obj;
                return Objects.equals(lat, b.lat) && Objects.equals(lon, b.lon);
            }
            return false;
        }

        @Override
        public String toString()
        {
            return "Coordinate [lat=" + lat + ", lon=" + lon + "]";
        }
    }
}
