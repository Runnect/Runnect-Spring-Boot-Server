package org.runnect.server.common.module.convert;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CoordinatePathConverter {
    public static String coorConvertPath(List<Coordinate> coor) {
        List<Coordinate> pathArray = new ArrayList<>();

        for (Coordinate element : coor) {
            double lat = Double.parseDouble(String.valueOf(element.getLat()));
            double lon = Double.parseDouble(String.valueOf(element.getLong()));
            pathArray.add(new Coordinate(lat, lon));
        }

        StringBuilder pathBuilder = new StringBuilder("[");
        for (int i = 0; i < pathArray.size(); i++) {
            Coordinate element = pathArray.get(i);
            pathBuilder.append("(").append(element.getLat()).append(",").append(element.getLong()).append(")");
            if (i < pathArray.size() - 1) {
                pathBuilder.append(",");
            }
        }
        pathBuilder.append("]");

        return pathBuilder.toString();
    }

    public static List<List<Double>> pathConvertCoor(String path) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<List<Double>> coordinates = objectMapper.readValue(path, List.class);
        return coordinates;
    }

}

class Coordinate {
    private double lat;
    private double lon;

    public Coordinate(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLong() {
        return lon;
    }
}

