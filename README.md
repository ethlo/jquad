# jquad

[QuadKeys](https://learn.microsoft.com/en-us/bingmaps/articles/bing-maps-tile-system) for Java: the tiles of the Web
Mercator map, as used by Bing Maps, addressed by a string of digits or a 64-bit number.

A quadkey names a square of the map at a zoom level. Each extra digit picks one of the four smaller squares inside, so
the key of a tile is a prefix of the keys of everything inside it. That makes quadkeys useful for:

* **Indexing positions in a database:** store each position as a 64-bit quadkey at the deepest zoom level, and every
  position inside a tile is one range of numbers, so one index range scan.
* **Grouping and aggregating** positions by area, at any level of detail.
* **Map tiles:** converting between quadkeys and tile column and row.

No dependencies. Java 17 or later.

## Usage

The examples below are the tests in [`src/test/java/samples`](src/test/java/samples), copied into this file by the
build.

#### fromCoordinate <sup>[source](src/test/java/samples/QuadKeySamples.java#L33-L45)</sup>

The tile that contains a position at a zoom level, from 1 to 31. Zoom level 18 is about 0.6 m per pixel. A
quadkey has a string form with one digit per zoom level, and a 64-bit integer form.

```java
final QuadKey key = QuadKey.of(40.757678, -73.969558, 18);

assertThat(key.toString()).isEqualTo("032010110132031203");
assertThat(key.toLong()).isEqualTo(15104861027L);
assertThat(key.zoom()).isEqualTo(18);
```

#### parse <sup>[source](src/test/java/samples/QuadKeySamples.java#L47-L60)</sup>

Read either form back. The integer form does not say which zoom level it is at, so it needs one.

```java
assertThat(QuadKey.parse("032010110132031203")).isEqualTo(QuadKey.fromLong(15104861027L, 18));

// Map tile coordinates: column from the west, row from the north
final QuadKey tile = QuadKey.ofTile(3, 5, 3);
assertThat(tile.toString()).isEqualTo("213");
assertThat(tile.tileX()).isEqualTo(3);
assertThat(tile.tileY()).isEqualTo(5);
```

#### containment <sup>[source](src/test/java/samples/QuadKeySamples.java#L62-L73)</sup>

A tile contains the positions inside it. A position on the edge between two tiles is in exactly one of them,
the same one that `QuadKey.of` returns.

```java
final QuadKey key = QuadKey.of(40.757678, -73.969558, 16);

assertThat(key.contains(new Coordinate(40.757608, -73.969436))).isTrue();
assertThat(key.contains(new Coordinate(40.759740, -73.965454))).isFalse();
```

#### hierarchy <sup>[source](src/test/java/samples/QuadKeySamples.java#L75-L87)</sup>

Move between zoom levels. A tile contains itself and every tile whose key starts with its own.

```java
final QuadKey key = QuadKey.parse("032010110132");

assertThat(key.parent().toString()).isEqualTo("03201011013");
assertThat(key.atZoom(4).toString()).isEqualTo("0320");
assertThat(key.children()).hasSize(4).allSatisfy(child -> assertThat(key.contains(child)).isTrue());
assertThat(key.contains(QuadKey.parse("032010110132031203"))).isTrue();
```

#### databaseRange <sup>[source](src/test/java/samples/QuadKeySamples.java#L89-L103)</sup>

Store positions as quadkeys at the deepest zoom level. All positions inside a tile are then one contiguous
range of numbers, so finding them is a single range query on an index, for example
`WHERE quadkey BETWEEN ? AND ?`.

```java
final QuadKey area = QuadKey.parse("032010110132");
final long from = area.firstDescendant(QuadKey.MAX_ZOOM).toLong();
final long to = area.lastDescendant(QuadKey.MAX_ZOOM).toLong();

final long stored = QuadKey.of(40.757678, -73.969558, QuadKey.MAX_ZOOM).toLong();
assertThat(stored).isBetween(from, to);
```

#### areas <sup>[source](src/test/java/samples/QuadKeySamples.java#L105-L119)</sup>

The area of a tile, and a search area around a position. A search area near longitude 180 crosses it, and one
near a pole covers every longitude.

```java
final BoundingBox tile = QuadKey.parse("032010110132").boundingBox();
assertThat(tile.contains(new Coordinate(40.757678, -73.969558))).isTrue();

final BoundingBox nearby = BoundingBox.around(new Coordinate(59.9139, 10.7522), 500);
assertThat(nearby.contains(new Coordinate(59.9127, 10.7461))).isTrue();

assertThat(BoundingBox.around(new Coordinate(-17.7134, 179.999), 1000).crossesAntimeridian()).isTrue();
```


## API

| Type | What it is |
|---|---|
| `QuadKey` | An immutable tile: `of`, `ofTile`, `parse`, `fromLong`; `zoom`, `toLong`, `toString`, `tileX`, `tileY`; `parent`, `atZoom`, `children`, `firstDescendant`, `lastDescendant`; `contains`, `boundingBox`, `center` |
| `Coordinate` | A latitude and longitude in degrees |
| `BoundingBox` | An area between two latitudes and two longitudes, which may cross longitude 180. `BoundingBox.around(center, meters)` gives a search area |

Positions north or south of the Web Mercator limit (`QuadKey.MAX_LATITUDE`, about 85.05°) are placed in the tiles at
the top and bottom of the map.
