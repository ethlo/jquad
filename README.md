# jquad
Java [QuadKey](https://docs.microsoft.com/en-us/bingmaps/articles/bing-maps-tile-system) Utility

## Usage Examples
**Creating from coordinates**
```java
final Coordinate c = new Coordinate(40.757678, -73.969558);
QuadKey qk = new QuadKey(c, 18); // Zoom level 18 (~ 0.5972 m / pixel)
System.out.println(qk.getAsLong()); // Print 64-bit integer representation: 15104861027
System.out.println(qk.getAsString()); // Print string representation: "032010110132031203"
```

**Checking containment**
```java
final Coordinate c1 = new Coordinate(40.757678, -73.969558);
final Coordinate c2 = new Coordinate(40.757608, -73.969436);
QuadKey qk = new QuadKey(c1, 16);
System.out.println(qk.contains(c2)); // true
```

See tests for further examples.