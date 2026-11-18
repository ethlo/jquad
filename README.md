# jquad
Java [QuadKey](https://docs.microsoft.com/en-us/bingmaps/articles/bing-maps-tile-system) Utility

## Usage Examples

The examples below are the tests in [`src/test/java/samples`](src/test/java/samples), copied into this file by the
build.

#### fromCoordinate <sup>[source](src/test/java/samples/QuadKeySamples.java#L32-L44)</sup>

Create a quadkey from a coordinate and a zoom level. Zoom level 18 is about 0.6 m per pixel. A quadkey has a
64-bit integer form and a string form with one digit per zoom level.

```java
final Coordinate c = new Coordinate(40.757678, -73.969558);
final QuadKey qk = new QuadKey(c, 18);

assertThat(qk.getAsLong()).isEqualTo(15104861027L);
assertThat(qk.getAsString()).isEqualTo("032010110132031203");
```

#### containment <sup>[source](src/test/java/samples/QuadKeySamples.java#L46-L57)</sup>

Check whether a coordinate lies within the tile of a quadkey.

```java
final Coordinate c1 = new Coordinate(40.757678, -73.969558);
final Coordinate c2 = new Coordinate(40.757608, -73.969436);
final QuadKey qk = new QuadKey(c1, 16);

assertThat(qk.contains(c2)).isTrue();
```

#### parentTile <sup>[source](src/test/java/samples/QuadKeySamples.java#L59-L72)</sup>

A tile at a lower zoom level contains the tiles at higher zoom levels whose quadkeys start with its own.

```java
final Coordinate c = new Coordinate(40.757678, -73.969558);
final QuadKey parent = new QuadKey(c, 12);
final QuadKey child = new QuadKey(c, 18);

assertThat(parent.getAsString()).isEqualTo("032010110132");
assertThat(parent.isParentOf(child)).isTrue();
assertThat(child.isParentOf(parent)).isFalse();
```


See the other tests for further examples.
