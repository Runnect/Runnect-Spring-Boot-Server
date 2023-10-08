package org.runnect.server.common.module.convert;

import java.util.Random;

public class NicknameGenerator {
    public static String randomInitialNickname() {
        Random random = new Random();
        String alphanumeric = Long.toString(Math.abs(random.nextLong()), 36);
        return "임시" + alphanumeric.substring(0, 9);
    }
}
