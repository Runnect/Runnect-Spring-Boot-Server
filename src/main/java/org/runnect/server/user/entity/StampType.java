package org.runnect.server.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StampType {
    // 유저 스탬프 목록
    CSPR0("default", 0, -1),

    C1("course", 1, 1),
    C2("course", 2, 10),
    C3("course", 3, 30),

    S1("scrap", 1, 1),
    S2("scrap", 2, 20),
    S3("scrap", 3, 40),

    U1("upload", 1, 1),
    U2("upload", 2, 10),
    U3("upload", 3, 30),

    R1("record", 1, 1),
    R2("record", 2, 15),
    R3("record", 3, 30),

    // 스탬프 타입
    C("course", 0, -1),
    S("scrap", 0, -1),
    U("upload", 0, -1),
    R("record", 0, -1);

    private final String type;
    private final int stampLevel;
    private final int levelUpCriteria;

    public static int getLevelUpCriteria(StampType type, int stampLevel) {
        for (StampType stampType : StampType.values()) {
            if (stampType.getType().equals(type.getType())
                && stampType.getStampLevel() == stampLevel) {
                return stampType.getLevelUpCriteria();
            }
        }
        return -1;
    }

    public static StampType getStampType(StampType type, int level) {
        for (StampType stampType : StampType.values()) {
            if (stampType.getType().equals(type.getType()) && stampType.getStampLevel() == level) {
                return stampType;
            }
        }
        return null;
    }
}
