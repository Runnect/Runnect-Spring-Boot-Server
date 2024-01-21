package org.runnect.server.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StampType {
    DELETED_USER("알 수 없음",-1,-1),
    // 유저 스탬프 목록
    CSPR0("default", 0, -1),

    c1("course", 1, 1),
    c2("course", 2, 10),
    c3("course", 3, 30),

    s1("scrap", 1, 1),
    s2("scrap", 2, 20),
    s3("scrap", 3, 40),

    u1("upload", 1, 1),
    u2("upload", 2, 10),
    u3("upload", 3, 30),

    r1("record", 1, 1),
    r2("record", 2, 15),
    r3("record", 3, 30),

    // 스탬프 타입
    c("course", 0, -1),
    s("scrap", 0, -1),
    u("upload", 0, -1),
    r("record", 0, -1);

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
