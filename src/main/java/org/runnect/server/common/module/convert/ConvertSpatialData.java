package org.runnect.server.common.module.convert;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class ConvertSpatialData {


    private LineString getLineString(List<CoordinateDto> coordinateDtos) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Coordinate[] coordinates = new Coordinate[coordinateDtos.size()];
        for (int i = 0; i < coordinateDtos.size(); i++) {
            coordinates[i] = new Coordinate(coordinateDtos.get(i).getLatitude(), coordinateDtos.get(i).getLongitude());
        }
        LineString lineString = geometryFactory.createLineString(coordinates);
        return lineString;
    }


    @Getter
    @AllArgsConstructor
    static class CoordinateDto {
        private double latitude;
        private double longitude;
    }

    private Point getPoint(double latitude, double longitude) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point point = geometryFactory.createPoint(new Coordinate(latitude, longitude));
        return point;
    }
}
