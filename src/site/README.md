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

${samples}
## API

| Type | What it is |
|---|---|
| `QuadKey` | An immutable tile: `of`, `ofTile`, `parse`, `fromLong`; `zoom`, `toLong`, `toString`, `tileX`, `tileY`; `parent`, `atZoom`, `children`, `firstDescendant`, `lastDescendant`; `contains`, `boundingBox`, `center` |
| `Coordinate` | A latitude and longitude in degrees |
| `BoundingBox` | An area between two latitudes and two longitudes, which may cross longitude 180. `BoundingBox.around(center, meters)` gives a search area |

Positions north or south of the Web Mercator limit (`QuadKey.MAX_LATITUDE`, about 85.05°) are placed in the tiles at
the top and bottom of the map.
