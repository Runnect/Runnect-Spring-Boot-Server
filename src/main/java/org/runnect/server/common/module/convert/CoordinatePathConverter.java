package org.runnect.server.common.module.convert;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.runnect.server.common.constant.ErrorStatus;
import org.runnect.server.common.exception.BadRequestException;
import org.runnect.server.common.exception.BasicException;

@Slf4j
public class CoordinatePathConverter {

    public static LineString coorConvertPath(List<CoordinateDto> path) {
        try {
            return getLineString(path);
        } catch (Exception e) {
            log.warn("course 요청 데이터 값 (path) -> " + path);
            log.warn("course 요청 데이터 값의 타입 (path) -> " + path.getClass().getName());
            throw new BadRequestException(ErrorStatus.VALIDATION_COURSE_PATH_EXCEPTION, ErrorStatus.VALIDATION_COURSE_PATH_EXCEPTION.getMessage());
        }
    }

    public static List<List<Double>> pathConvertCoor(LineString path) {
        try {

            List<List<Double>> coordinates = new ArrayList<>();

            for (int i = 0; i < path.getNumPoints(); i++) {
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

    private static LineString getLineString(List<CoordinateDto> coordinateDtos) {
        StringBuilder sb = new StringBuilder();
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Coordinate[] coordinates = new Coordinate[coordinateDtos.size()];
        for (int i = 0; i < coordinateDtos.size(); i++) {
            coordinates[i] = new Coordinate(coordinateDtos.get(i).getLatitude(), coordinateDtos.get(i).getLongitude());
            sb.append("(" + coordinateDtos.get(i).getLatitude() + ", " + coordinateDtos.get(i).getLongitude() + ")");
        }
        LineString lineString = geometryFactory.createLineString(coordinates);
        log.info("create course!");
        log.info(sb.toString());
        return lineString;
    }
}
