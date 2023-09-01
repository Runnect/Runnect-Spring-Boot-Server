package org.runnect.server.common.module.convert;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.runnect.server.common.exception.BadRequestException;
import org.runnect.server.common.exception.BasicException;
import org.runnect.server.common.exception.ErrorStatus;
import org.springframework.jdbc.BadSqlGrammarException;

public class CoordinatePathConverter {

    public static String coorConvertPath(String path) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(path);

            List<String> formattedCoordinates = new ArrayList<>();

            for (JsonNode node : jsonNode) {
                String lat = node.get("lat").asText();
                String lon = node.get("long").asText();
                formattedCoordinates.add("[" + lat + ", " + lon + "]");
            }

            return "[" + String.join(", ", formattedCoordinates) + "]";

        } catch (Exception e) {
            throw new BadRequestException(ErrorStatus.VALIDATION_COURSE_PATH_EXCEPTION, ErrorStatus.VALIDATION_COURSE_PATH_EXCEPTION.getMessage());
        }
    }

    public static List<List<Double>> pathConvertCoor(String path) {
        try {
            System.out.println(path);
            ObjectMapper objectMapper = new ObjectMapper();
            List<List<Double>> coordinates = objectMapper.readValue(path, List.class);
            return coordinates;
        } catch (Exception e) {
            throw new BasicException(ErrorStatus.PATH_CONVERT_FAIL, ErrorStatus.PATH_CONVERT_FAIL.getMessage());
        }
    }

}

