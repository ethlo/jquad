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

import java.util.List;
import java.util.Objects;

/**
 * A tile in the <a href="https://learn.microsoft.com/en-us/bingmaps/articles/bing-maps-tile-system">Bing Maps tile
 * system</a>: a square of the Web Mercator map at a zoom level from 1 to {@link #MAX_ZOOM}.
 *
 * <p>A quadkey has a string form, one digit from 0 to 3 per zoom level ({@code "0320101101"}), and a 64-bit integer
 * form ({@link #toLong()}). The integer form does not record the zoom level, so it is always read back together with
 * one ({@link #fromLong(long, int)}). The keys of all tiles inside a tile form one contiguous integer range at any
 * deeper zoom level ({@link #firstDescendant(int)}, {@link #lastDescendant(int)}), which makes it a good database key
 * for area lookups.</p>
 *
 * <p>Instances are immutable. Two quadkeys are equal when they have the same zoom level and the same tile.</p>
 */
public final class QuadKey
{
    /**
     * The deepest zoom level: 31 levels of two bits each fit in a {@code long}
     */
    public static final int MAX_ZOOM = 31;

    /**
     * The northernmost latitude on the Web Mercator map. Positions further north or south are placed in the tiles
     * at the edge of the map.
     */
    public static final double MAX_LATITUDE = 85.05112877980659;

    private final long bits;
    private final int zoom;

    private QuadKey(long bits, int zoom)
    {
        this.bits = bits;
        this.zoom = zoom;
    }

    /**
     * @param coordinate the position
     * @param zoom       the zoom level, from 1 to {@link #MAX_ZOOM}
     * @return the tile that contains the position at the given zoom level
     */
    public static QuadKey of(Coordinate coordinate, int zoom)
    {
        Objects.requireNonNull(coordinate, "coordinate cannot be null");
        validateZoom(zoom);
        final double lat = Math.max(-MAX_LATITUDE, Math.min(MAX_LATITUDE, coordinate.lat()));
        final double sinLat = Math.sin(Math.toRadians(lat));
        final double fx = (coordinate.lon() + 180) / 360;
        final double fy = 0.5 - Math.log((1 + sinLat) / (1 - sinLat)) / (4 * Math.PI);
        final long size = 1L << zoom;
        final long x = Math.min(size - 1, Math.max(0, (long) Math.floor(fx * size)));
        final long y = Math.min(size - 1, Math.max(0, (long) Math.floor(fy * size)));
        return new QuadKey(interleave(x, y), zoom);
    }

    /**
     * @param lat  latitude, from -90 to 90
     * @param lon  longitude, from -180 to 180
     * @param zoom the zoom level, from 1 to {@link #MAX_ZOOM}
     * @return the tile that contains the position at the given zoom level
     */
    public static QuadKey of(double lat, double lon, int zoom)
    {
        return of(new Coordinate(lat, lon), zoom);
    }

    /**
     * @param x    the tile column, from 0 (west) to 2<sup>zoom</sup> - 1
     * @param y    the tile row, from 0 (north) to 2<sup>zoom</sup> - 1
     * @param zoom the zoom level, from 1 to {@link #MAX_ZOOM}
     * @return the tile
     */
    public static QuadKey ofTile(long x, long y, int zoom)
    {
        validateZoom(zoom);
        final long size = 1L << zoom;
        if (x < 0 || x >= size || y < 0 || y >= size)
        {
            throw new IllegalArgumentException("Tile (" + x + ", " + y + ") is outside zoom level " + zoom + ", which has " + size + " tiles in each direction");
        }
        return new QuadKey(interleave(x, y), zoom);
    }

    /**
     * @param quadKey the string form, one digit from 0 to 3 per zoom level
     * @return the tile
     */
    public static QuadKey parse(String quadKey)
    {
        Objects.requireNonNull(quadKey, "quadKey cannot be null");
        validateZoom(quadKey.length());
        long bits = 0;
        for (int i = 0; i < quadKey.length(); i++)
        {
            final char c = quadKey.charAt(i);
            if (c < '0' || c > '3')
            {
                throw new IllegalArgumentException("Invalid character '" + c + "' at position " + i + " in quadkey '" + quadKey + "'");
            }
            bits = (bits << 2) | (c - '0');
        }
        return new QuadKey(bits, quadKey.length());
    }

    /**
     * @param bits the integer form, as returned by {@link #toLong()}
     * @param zoom the zoom level of the integer form, from 1 to {@link #MAX_ZOOM}
     * @return the tile
     */
    public static QuadKey fromLong(long bits, int zoom)
    {
        validateZoom(zoom);
        if (bits < 0 || (bits >>> (2 * zoom)) != 0)
        {
            throw new IllegalArgumentException("Value " + bits + " is not a quadkey at zoom level " + zoom);
        }
        return new QuadKey(bits, zoom);
    }

    /**
     * @return the zoom level, from 1 to {@link #MAX_ZOOM}
     */
    public int zoom()
    {
        return zoom;
    }

    /**
     * @return the integer form: two bits per zoom level, with the first level in the highest bits used
     */
    public long toLong()
    {
        return bits;
    }

    /**
     * @return the tile column, from 0 (west) to 2<sup>zoom</sup> - 1
     */
    public long tileX()
    {
        return deinterleave(bits);
    }

    /**
     * @return the tile row, from 0 (north) to 2<sup>zoom</sup> - 1
     */
    public long tileY()
    {
        return deinterleave(bits >>> 1);
    }

    /**
     * @return the tile one zoom level up, which contains this one
     * @throws IllegalStateException at zoom level 1, which has no parent
     */
    public QuadKey parent()
    {
        if (zoom == 1)
        {
            throw new IllegalStateException("Zoom level 1 has no parent");
        }
        return atZoom(zoom - 1);
    }

    /**
     * @param zoom a zoom level from 1 up to this quadkey's own
     * @return the tile at that zoom level which contains this one
     */
    public QuadKey atZoom(int zoom)
    {
        validateZoom(zoom);
        if (zoom > this.zoom)
        {
            throw new IllegalArgumentException("Zoom level " + zoom + " is deeper than this quadkey's " + this.zoom + "; use firstDescendant or children");
        }
        return new QuadKey(bits >>> (2 * (this.zoom - zoom)), zoom);
    }

    /**
     * @return the four tiles one zoom level down, in key order: north-west, north-east, south-west, south-east
     * @throws IllegalStateException at {@link #MAX_ZOOM}
     */
    public List<QuadKey> children()
    {
        if (zoom == MAX_ZOOM)
        {
            throw new IllegalStateException("Zoom level " + MAX_ZOOM + " has no children");
        }
        final long first = bits << 2;
        return List.of(new QuadKey(first, zoom + 1), new QuadKey(first | 1, zoom + 1), new QuadKey(first | 2, zoom + 1), new QuadKey(first | 3, zoom + 1));
    }

    /**
     * @param zoom a zoom level from this quadkey's own down to {@link #MAX_ZOOM}
     * @return the tile inside this one with the lowest key at that zoom level
     */
    public QuadKey firstDescendant(int zoom)
    {
        return new QuadKey(bits << (2 * levelsDown(zoom)), zoom);
    }

    /**
     * @param zoom a zoom level from this quadkey's own down to {@link #MAX_ZOOM}
     * @return the tile inside this one with the highest key at that zoom level. Every key from
     * {@link #firstDescendant(int)} to this one, inclusive, is inside this tile.
     */
    public QuadKey lastDescendant(int zoom)
    {
        final int shift = 2 * levelsDown(zoom);
        return new QuadKey(((bits + 1) << shift) - 1, zoom);
    }

    private int levelsDown(int zoom)
    {
        validateZoom(zoom);
        if (zoom < this.zoom)
        {
            throw new IllegalArgumentException("Zoom level " + zoom + " is above this quadkey's " + this.zoom + "; use atZoom");
        }
        return zoom - this.zoom;
    }

    /**
     * @param other a quadkey
     * @return whether the other tile is this one or inside it
     */
    public boolean contains(QuadKey other)
    {
        return other.zoom >= zoom && (other.bits >>> (2 * (other.zoom - zoom))) == bits;
    }

    /**
     * Uses the same rule as {@link #of(Coordinate, int)}, so a position on the edge between two tiles is in exactly
     * one of them: the one to the east, or to the south.
     *
     * @param coordinate a position
     * @return whether the position is in this tile
     */
    public boolean contains(Coordinate coordinate)
    {
        return of(coordinate, zoom).bits == bits;
    }

    /**
     * @return the area of the tile
     */
    public BoundingBox boundingBox()
    {
        final double size = 1L << zoom;
        final long x = tileX();
        final long y = tileY();
        return new BoundingBox(
                new Coordinate(latitude((y + 1) / size), longitude(x / size)),
                new Coordinate(latitude(y / size), longitude((x + 1) / size)));
    }

    /**
     * @return the center of the tile on the map. Because of the Mercator projection it is slightly north of the
     * middle latitude in the northern hemisphere, and south of it in the southern.
     */
    public Coordinate center()
    {
        final double size = 1L << zoom;
        return new Coordinate(latitude((tileY() + 0.5) / size), longitude((tileX() + 0.5) / size));
    }

    /**
     * @return the string form, one digit from 0 to 3 per zoom level
     */
    @Override
    public String toString()
    {
        final char[] digits = new char[zoom];
        for (int i = 0; i < zoom; i++)
        {
            digits[i] = (char) ('0' + ((bits >>> (2 * (zoom - 1 - i))) & 3));
        }
        return new String(digits);
    }

    @Override
    public boolean equals(Object o)
    {
        return o instanceof QuadKey other && other.bits == bits && other.zoom == zoom;
    }

    @Override
    public int hashCode()
    {
        return 31 * Long.hashCode(bits) + zoom;
    }

    private static void validateZoom(int zoom)
    {
        if (zoom < 1 || zoom > MAX_ZOOM)
        {
            throw new IllegalArgumentException("Zoom level must be from 1 to " + MAX_ZOOM + ", was " + zoom);
        }
    }

    private static double longitude(double fx)
    {
        return fx * 360 - 180;
    }

    private static double latitude(double fy)
    {
        return Math.toDegrees(Math.atan(Math.sinh(Math.PI * (1 - 2 * fy))));
    }

    /**
     * Spreads the bits of x to the even positions and those of y to the odd ones
     */
    private static long interleave(long x, long y)
    {
        return spread(x) | (spread(y) << 1);
    }

    private static long spread(long v)
    {
        v &= 0xFFFFFFFFL;
        v = (v | (v << 16)) & 0x0000FFFF0000FFFFL;
        v = (v | (v << 8)) & 0x00FF00FF00FF00FFL;
        v = (v | (v << 4)) & 0x0F0F0F0F0F0F0F0FL;
        v = (v | (v << 2)) & 0x3333333333333333L;
        v = (v | (v << 1)) & 0x5555555555555555L;
        return v;
    }

    /**
     * Collects the even bits into the low 32 bits
     */
    private static long deinterleave(long v)
    {
        v &= 0x5555555555555555L;
        v = (v | (v >>> 1)) & 0x3333333333333333L;
        v = (v | (v >>> 2)) & 0x0F0F0F0F0F0F0F0FL;
        v = (v | (v >>> 4)) & 0x00FF00FF00FF00FFL;
        v = (v | (v >>> 8)) & 0x0000FFFF0000FFFFL;
        v = (v | (v >>> 16)) & 0x00000000FFFFFFFFL;
        return v;
    }
}
