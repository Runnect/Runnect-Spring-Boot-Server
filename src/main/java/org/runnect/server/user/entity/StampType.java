package org.runnect.server.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StampType {
    CSPR0("default", 0),

    C1("course", 1),
    C2("course", 2),
    C3("course", 3),

    S1("scrap", 1),
    S2("scrap", 2),
    S3("scrap", 3),

    U1("upload", 1),
    U2("upload", 2),
    U3("upload", 3),

    R1("record", 1),
    R2("record", 2),
    R3("record", 3);

    private final String type;
    private final int stampLevel;
}
