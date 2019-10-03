package com.ethlo.quadkey;

import java.util.Objects;

public class QuadKey {
    public static final int MAX_ZOOM = 31;
    private static final double MAX_LONGITUDE = 180.0;

    /* (2*atan(exp(M_PI))*180.0/M_PI - 90.0) */
    private static final double MAX_LATITUDE = 85.05112877980659;
    private static final double MIN_LONGITUDE = -MAX_LONGITUDE;
    private static final double MIN_LATITUDE = -MAX_LATITUDE;

    private int zoom;
    private long quadInt;
    private String quadKey;

    public QuadKey(Coordinate coordinate, int zoom) {
        this.zoom = zoom;
        this.quadInt = coordinate2QuadInt(coordinate, zoom);
        this.quadKey = coordinate2QuadKey(coordinate, zoom);
    }

    public QuadKey(long quadInt) {
        this.quadInt = quadInt;
        this.quadKey = quadInt2String(quadInt, zoom);
    }

    public QuadKey(String quadKey) {
        this.quadInt = quadKey2QuadInt(quadKey);
        this.quadKey = quadKey;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        if (zoom <= 0) throw new IllegalArgumentException("Zoom level invalid.");
        if (zoom > getZoom()) throw new IllegalArgumentException("Can not zoom in further.");
        this.zoom = zoom;
        this.quadKey = quadKey.substring(0, zoom);
        this.quadInt = quadKey2QuadInt(quadKey);
    }

    public long getAsLong() {
        return quadInt;
    }

    public String getAsString() {
        return this.toString();
    }

    public Coordinate getAsCoordinate() {
        return quadInt2Coordinate(quadInt, zoom);
    }

    public BoundingRectangle getAsBoundingBox() {
        return tile2Bbox(quadInt, getZoom());
    }

    public BoundingRectangle getAsBoundingBox(int zoom) {
        if (zoom > getZoom()) throw new IllegalArgumentException("Can not zoom in further.");
        return tile2Bbox(quadInt, zoom);
    }

    public boolean contains(Coordinate coordinate) {
        return getAsBoundingBox().contains(coordinate);
    }

    public boolean isParentOf(QuadKey other) {
        return other.getAsString().startsWith(quadKey);
    }

    @Override
    public String toString() {
        return quadKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuadKey quadKey = (QuadKey) o;
        return quadInt == quadKey.quadInt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quadInt);
    }

    /**
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
    private static Coordinate quadInt2Coordinate(long quadInt, int zoom) {
        final Point p = quadInt2Point(quadInt);
        long x = p.getX();
        long y = p.getY();
        double xMin = -0.5 + (x * 1.0 / (1L << zoom));
        double xMax = -0.5 + ((x + 1) * 1.0 / (1L << zoom));
        double yMin = -0.5 + ((y + 1) * 1.0 / (1L << zoom));
        double yMax = -0.5 + (y * 1.0 / (1L << zoom));

        double lonMin = 360.0 * xMin;
        double lonMax = 360.0 * xMax;

        double latMin = 90.0 - 360.0 * Math.atan(Math.exp(-2 * Math.PI * (-yMin))) / Math.PI;
        double latMax = 90.0 - 360.0 * Math.atan(Math.exp(-2 * Math.PI * (-yMax))) / Math.PI;

        return new Coordinate((latMin + latMax) / 2D, (lonMin + lonMax) / 2D);
    }

    private static String coordinate2QuadKey(Coordinate coordinate, int zoom) {
        return quadInt2String(pointToQuadInt(coordinate2Point(coordinate, zoom)), zoom);
    }

    private static long coordinate2QuadInt(Coordinate coordinate, int zoom) {
        final Point point = coordinate2Point(coordinate, zoom);
        return pointToQuadInt(point);
    }

    private static BoundingRectangle tile2Bbox(long quadInt, int zoom) {
        final Range<Coordinate> r = tile2BboxScaled(1.0, 1.0, -0.5, -0.5, quadInt, zoom);

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


    private static String quadInt2String(long quadInt, int zoom) {
        StringBuilder str = new StringBuilder();
        int n = zoom * 2;
        for (int i = 2; i < n + 2; i += 2) {
            long charCode = (quadInt >> (n-i)) & 0b11;
            str.append(charCode);
        }
        return str.toString();
    }

    private static long quadKey2QuadInt(String quadKey) {
        long quadInt = 0;
        byte[] chars = quadKey.getBytes();
        for (int i = 0; i < chars.length; i++) {
            int digit = chars[i] - 48;
            quadInt = (quadInt << 2) | digit;
        }
        return quadInt;
    }

    private static long pointToQuadInt(Point point) {
        final long[] b =
            {0x5555555555555555L, 0x3333333333333333L, 0x0F0F0F0F0F0F0F0FL, 0x00FF00FF00FF00FFL, 0x0000FFFF0000FFFFL};
        final long[] s =
            {1, 2, 4, 8, 16};

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

    private static Point quadInt2Point(long quadInt) {
        final long[] b =
            {0x5555555555555555L, 0x3333333333333333L, 0x0F0F0F0F0F0F0F0FL, 0x00FF00FF00FF00FFL, 0x0000FFFF0000FFFFL, 0x00000000FFFFFFFFL};
        final int[] s =
            {0, 1, 2, 4, 8, 16};

        long x = quadInt;
        long y = quadInt >> 1;

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

    private static Point coordinate2Point(Coordinate coordinate, int zoom) {
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

    private static Range<Coordinate> tile2BboxScaled(double scaleX, double scaleY, double offsetX, double offsetY, long quadInt, int zoom) {
        final Point p = quadInt2Point(quadInt);
        long x = p.getX();
        long y = p.getY();
        double xMin = offsetX + (x * 1.0 / (1L << zoom)) * scaleX;
        double xMax = offsetX + ((x + 1) * 1.0 / (1L << zoom)) * scaleX;
        double yMin = offsetY + ((y + 1) * 1.0 / (1L << zoom)) * scaleY;
        double yMax = offsetY + (y * 1.0 / (1L << zoom)) * scaleY;
        return new Range<>(new Coordinate(xMin, yMin), new Coordinate(xMax, yMax));
    }
}
