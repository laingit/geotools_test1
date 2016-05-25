package org.geotools;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;
import com.vividsolutions.jts.io.ParseException;
import org.geotools.geometry.jts.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alai on 25/05/2016.

 http://docs.geoserver.org/2.8.x/en/user/styling/sld-extensions/pointsymbols.html

 Custom WKT Shapes

 wkt://

 "MULTISURFACE (
 ((0 0, 1 0, 1 4, 0 0)),
 CURVEPOLYGON (COMPOUNDCURVE (CIRCULARSTRING (0 2, 7 5, 2 10), (2 10, 0 2)),
 COMPOUNDCURVE (CIRCULARSTRING (3 9, 6 5, 3 2), (3 2, 3 9))))"

 test in POSTGIS:
        SELECT ST_Area
	        (ST_WKTToSQL ('MULTISURFACE (
			        ((0 0, 1 0, 1 4, 0 0)),
		                CURVEPOLYGON (COMPOUNDCURVE (CIRCULARSTRING (0 2, 7 5, 2 10), (2 10, 0 2)),
		                COMPOUNDCURVE (CIRCULARSTRING (3 9, 6 5, 3 2), (3 2, 3 9))))'));

		postgisArea = 31.1602165357206

 Exception in thread "main" com.vividsolutions.jts.io.ParseException: Expected EMPTY or ( but found '0'
 at org.geotools.geometry.jts.WKTReader2.parseError(WKTReader2.java:352)
 at org.geotools.geometry.jts.WKTReader2.getNextEmptyOrOpener(WKTReader2.java:265)
 at org.geotools.geometry.jts.WKTReader2.getCoordinates(WKTReader2.java:168)
 at org.geotools.geometry.jts.WKTReader2.readLinearRingText(WKTReader2.java:567)
 at org.geotools.geometry.jts.WKTReader2.readPolygonText(WKTReader2.java:663)
 at org.geotools.geometry.jts.WKTReader2.readMultiSurfaceText(WKTReader2.java:778)
 at org.geotools.geometry.jts.WKTReader2.readGeometryTaggedText(WKTReader2.java:420)
 at org.geotools.geometry.jts.WKTReader2.read(WKTReader2.java:151)
 at org.geotools.geometry.jts.WKTReader2.read(WKTReader2.java:120)
 at org.geotools.TestMultiSurface.main(TestMultiSurface.java:120)
 */

public class TestMultiSurface {
    public static void main(String[] args) throws ParseException, IOException {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        CurvedGeometryFactory curvedFactory = new CurvedGeometryFactory(geometryFactory, Double.MAX_VALUE);

        // B: - arc+line = exterior CounterClockwise
        PackedCoordinateSequence coords_arc_B = new PackedCoordinateSequence.Double(
                new double[]{0, 2, 7, 5, 2, 10}, 2);

        Coordinate[] coords_line_B = new Coordinate[]{new Coordinate(2, 10), new Coordinate(0, 2)};

        CircularString arc_B = (CircularString) curvedFactory.createCurvedGeometry(coords_arc_B);
        LineString line_B = geometryFactory.createLineString(coords_line_B);

        List<LineString> list_B = new ArrayList<>();
        list_B.add(arc_B);
        list_B.add(line_B);

        CompoundRing ring_B = (CompoundRing) curvedFactory.createCurvedGeometry(list_B);

        // C: - arc+line = holes Clockwise
        PackedCoordinateSequence coords_arc_C = new PackedCoordinateSequence.Double(
                new double[]{3, 9, 6, 5, 3, 2}, 2);

        Coordinate[] coords_line_C = new Coordinate[]
                {new Coordinate(3, 2), new Coordinate(3, 9)};

        CircularString arc_C = (CircularString) curvedFactory.createCurvedGeometry(coords_arc_C);
        LineString line_C = geometryFactory.createLineString(coords_line_C);

        List<LineString> list_C = new ArrayList<>();
        list_C.add(arc_C);
        list_C.add(line_C);

        CompoundRing ring_C = (CompoundRing) curvedFactory.createCurvedGeometry(list_C);

        // curve polygon BC
        Polygon poly_BC = curvedFactory.createCurvePolygon(ring_B, new LinearRing[]{ring_C});

        // A: polygon
        Coordinate[] coords_poly_A = new Coordinate[]
                {new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(1, 4), new Coordinate(0, 0)};

        Polygon poly_A = geometryFactory.createPolygon(coords_poly_A);

        // MultiSurface_A_BC
        List<Polygon> polygons = new ArrayList<>();
        polygons.add(poly_A);
        polygons.add(poly_BC);

        MultiSurface multiSurface_A_BC = (MultiSurface) curvedFactory.createMultiSurface(polygons);

        // write
        WKTWriter2 wktWriter2 = new WKTWriter2();
        String WKT2 = wktWriter2.write(multiSurface_A_BC);

        // reader
        WKTReader2 wktReader2 = new WKTReader2(curvedFactory);

        // CREATE EXCEPTION -> NOT WORKING
        MultiSurface multisurface_from_wkt2 = (MultiSurface) wktReader2.read(WKT2);// ERROR

        System.out.println("multiSurface_A_BC: valid? = " + multiSurface_A_BC.isValid());
        System.out.println("Area = " + multiSurface_A_BC.getArea()); // Area = 31.110026267242873
        System.out.println("\n" + wktWriter2.writeFormatted(multiSurface_A_BC));
    }
}
