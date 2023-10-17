package org.runnect.server.common.module.convert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.runnect.server.common.exception.BadRequestException;
import org.runnect.server.common.exception.BasicException;
import org.runnect.server.common.exception.ErrorStatus;

public class CoordinatePathConverter {

    public static LineString coorConvertPath(String path) {

        List<CoordinateDto2> coordinateDtos = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(path);

            List<String> formattedCoordinates = new ArrayList<>();

            for (JsonNode node : jsonNode) {
                Double lat = node.get("lat").asDouble();
                Double lon = node.get("long").asDouble();
                coordinateDtos.add(new CoordinateDto2(lat, lon));
//                formattedCoordinates.add("[" + lat + ", " + lon + "]");
            }

//            return "[" + String.join(", ", formattedCoordinates) + "]";

            return getLineString(coordinateDtos);
        } catch (Exception e) {
            throw new BadRequestException(ErrorStatus.VALIDATION_COURSE_PATH_EXCEPTION, ErrorStatus.VALIDATION_COURSE_PATH_EXCEPTION.getMessage());
        }
    }

    public static List<List<Double>> pathConvertCoor(LineString path) {
        try {
            System.out.println(path);
//            WKTWriter wktWriter = new WKTWriter();
//            String wktString = wktWriter.write(path);
//            System.out.println(wktString);
//            ObjectMapper objectMapper = new ObjectMapper();
//            List<List<Double>> coordinates = objectMapper.readValue(wktString, List.class);

            List<List<Double>> coordinates = new ArrayList<>();

            for (int i = 0; i < path.getNumPoints(); i++) {
//                Point pointN = path.getPointN(i).getX();
                List<Double> temp = new ArrayList<>();
                temp.add(path.getPointN(i).getX());
                temp.add(path.getPointN(i).getY());
                coordinates.add(temp);
            }

            return coordinates;
        } catch (Exception e) {
            throw new BasicException(ErrorStatus.PATH_CONVERT_FAIL, ErrorStatus.PATH_CONVERT_FAIL.getMessage());
        }
    }


    private static LineString getLineString(List<CoordinateDto2> coordinateDtos) {
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
    static class CoordinateDto2 {
        private double latitude;
        private double longitude;
    }

}

