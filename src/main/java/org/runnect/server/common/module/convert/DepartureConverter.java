package org.runnect.server.common.module.convert;

import org.runnect.server.common.dto.DepartureResponse;

import java.util.Arrays;

public class DepartureConverter {
    public static DepartureResponse requestConvertDeparture(String departureAddress, String departureName) {
        String[] departures = departureAddress.split(" ");
        if (departures.length < 3) {
            return null;
        } else if (departures.length == 3) {
            return new DepartureResponse(
                    departures[0],
                    departures[1],
                    departures[2],
                    departureName);
        } else if (departures.length == 4) {
            return new DepartureResponse(
                    departures[0],
                    departures[1],
                    departures[2],
                    departures[3],
                    departureName);
        } else {
            String detail = String.join(" ", Arrays.copyOfRange(departures, 3, departures.length));
            return new DepartureResponse(
                    departures[0],
                    departures[1],
                    departures[2],
                    detail,
                    departureName);
        }
    }
}

