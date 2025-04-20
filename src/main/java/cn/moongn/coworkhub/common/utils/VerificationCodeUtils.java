package cn.moongn.coworkhub.common.utils;

import java.util.Random;

public class VerificationCodeUtils {

    private static final String NUMBERS = "0123456789";

    /**
     * 生成指定长度的随机数字验证码
     */
    public static String generateNumericCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        return sb.toString();
    }
}