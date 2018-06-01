package com.ethlo.quadkey;

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

    private QuadKey()
    {
    }

    public static long xy2quadint(Point point)
    {
        final long[] b =
        { 0x5555555555555555L, 0x3333333333333333L, 0x0F0F0F0F0F0F0F0FL, 0x00FF00FF00FF00FFL, 0x0000FFFF0000FFFFL };
        final long[] s =
        { 1, 2, 4, 8, 16 };

        long x = (point.getX() | (point.getX() << s[4])) & b[4];
        long y = (point.getY() | (point.getY() << s[4])) & b[4];

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

    /**
     *
     * decimal
     * places   degrees          max-distance
     * -------  -------          --------
     * 0        1                111  km
     * 1        0.1              11.1 km
     * 2        0.01             1.11 km
     * 3        0.001            111  m
     * 4        0.0001           11.1 m
     * 5        0.00001          1.11 m
     * 6        0.000001         11.1 cm
     * 7        0.0000001        1.11 cm
     * 8        0.00000001       1.11 mm
     *
     * @param quadInt
     * @return
     */
    public static Coordinate quadInt2Coordinate(long quadInt)
    {
        int zeroBits = 0;
        final Point p = quadInt2Point(quadInt);
        long x = p.getX();
        long y = p.getY();
        x >>= zeroBits;
        y >>= zeroBits;
        double xMin = -0.5 + (x * 1.0 / (1L << MAX_ZOOM));
        double xMax = -0.5 + ((x + 1) * 1.0 / (1L << MAX_ZOOM));
        double yMin = -0.5 + ((y + 1) * 1.0 / (1L << MAX_ZOOM));
        double yMax = -0.5 + (y * 1.0 / (1L << MAX_ZOOM));

        double lonMin = 360.0 * xMin;
        double lonMax = 360.0 * xMax;

        double latMin = 90.0 - 360.0 * Math.atan(Math.exp(-2 * Math.PI * (-yMin))) / Math.PI;
        double latMax = 90.0 - 360.0 * Math.atan(Math.exp(-2 * Math.PI * (-yMax))) / Math.PI;

        return new Coordinate((latMin + latMax) / 2D, (lonMin + lonMax)/ 2D);
    }

    public static Point coordinate2Point(Coordinate coordinate, int zoom)
    {
        final double lon = Math.min(MAX_LONGITUDE, Math.max(MIN_LONGITUDE, coordinate.getLon()));
        final double lat = Math.min(MAX_LATITUDE, Math.max(MIN_LATITUDE, coordinate.getLat()));

        double fx = (lon + 180.0) / 360.0;
        double sinlat = Math.sin(lat * Math.PI / 180.0);
        double fy = 0.5 - Math.log((1 + sinlat) / (1 - sinlat)) / (4 * Math.PI);

        long mapsize = (1 << zoom) & 0xFFFFFFFFL;
        long x = ((long) Math.floor(fx * mapsize)) & 0xFFFFFFFFL;
        long y = ((long) Math.floor(fy * mapsize)) & 0xFFFFFFFFL;
        x = Math.min(mapsize - 1, Math.max(0, x));
        y = Math.min(mapsize - 1, Math.max(0, y));
        return new Point(x, y);
    }

    public static Coordinate point2WebMercator(Point p)
    {
        final double x = (p.getX() * INV_XY_SCALE - 0.5) * WM_RANGE;
        final double y = (0.5 - p.getY() * INV_XY_SCALE) * WM_RANGE;
        return new Coordinate(x, y);
    }

    public static Coordinate quadInt2WebMercator(long quadint)
    {
        final Point p = quadInt2Point(quadint);
        return point2WebMercator(p);
    }

    public static Point webMercator2Point(Coordinate coordinate)
    {
        final double x = (coordinate.getLat() * INV_WM_RANGE + 0.5) * XY_SCALE;
        final double y = (0.5 - coordinate.getLon() * INV_WM_RANGE) * XY_SCALE;
        return new Point((long) x, (long) y);
    }

    public static long coordinate2quadInt(Coordinate coordinate)
    {
        final Point point = coordinate2Point(coordinate, MAX_ZOOM);
        return xy2quadint(point);
    }

    public static BoundingRectangle tile2bbox(long quadint, int zoom)
    {
        final Range<Coordinate> r = tile2bboxScaled(1.0, 1.0, -0.5, -0.5, quadint, zoom);

        final double xMin = r.getLower().getLat();
        final double yMin = r.getLower().getLon();
        final double xMmax = r.getUpper().getLat();
        final double yMax = r.getUpper().getLon();

        double lonMin = 360.0 * xMin;
        double lonMax = 360.0 * xMmax;

        double latMin = 90.0 - 360.0 * Math.atan(Math.exp(-2 * Math.PI * (-yMin))) / Math.PI;
        double latMax = 90.0 - 360.0 * Math.atan(Math.exp(-2 * Math.PI * (-yMax))) / Math.PI;
        return new BoundingRectangle(new Coordinate(latMin, lonMin), new Coordinate(latMax, lonMax));
    }

    public static Range<Coordinate> tile2bboxScaled(double scaleX, double scaleY, double offsetX, double offsetY, long quadint, int zoom)
    {
        int zeroBits = MAX_ZOOM - zoom;
        final Point p = quadInt2Point(quadint);
        long x = p.getX();
        long y = p.getY();
        x >>= zeroBits;
        y >>= zeroBits;
        double xMin = offsetX + (x * 1.0 / (1L << zoom)) * scaleX;
        double xMax = offsetX + ((x + 1) * 1.0 / (1L << zoom)) * scaleX;
        double yMin = offsetY + ((y + 1) * 1.0 / (1L << zoom)) * scaleY;
        double yMax = offsetY + (y * 1.0 / (1L << zoom)) * scaleY;
        return new Range<>(new Coordinate(xMin, yMin), new Coordinate(xMax, yMax));
    }

    public static QuadKeyRange quadIntRange(Coordinate coordinate, int distanceInMeters)
    {
        final BoundingRectangle rect = Geoutil.getBoundingRectangle(coordinate, distanceInMeters);
        return new QuadKeyRange(coordinate2quadInt(rect.getLower()), coordinate2quadInt(rect.getUpper()));
    }
}
