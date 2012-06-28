package org.dyndns.fzoli.mill.common;

import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author zoli
 */
public class InputValidator {
    
    public final static int MIN_USER_LENGTH = 3;
    public final static int MAX_USER_LENGTH = 10;
    public final static int MIN_PASSWORD_LENGTH = 6;
    public final static int MAX_PASSWORD_LENGTH = 15;
    public final static int MD5_LENGTH = 32;
    
    private final static Pattern PATTERN_EMAIL = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", Pattern.CASE_INSENSITIVE);
    private final static Pattern PATTERN_CAPTCHA = Pattern.compile("^[qwertzuopasdfghjkyxcvbnm]{6,6}$", Pattern.CASE_INSENSITIVE);
    
    public static boolean isCaptchaValid(String value) {
        if (value == null) return false;
        return PATTERN_CAPTCHA.matcher(value).matches();
    }
    
    public static boolean isUserIdValid(String value) {
        if (value == null) return false;
        return value.matches(createRule(MIN_USER_LENGTH, MAX_USER_LENGTH));
    }
        
    public static boolean isPasswordValid(String value) {
        if (value == null) return false;
        return value.matches(createRule(MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH));
    }
    
    public static boolean isPasswordHashValid(String value) {
        if (value == null) return false;
        return value.matches(createRule(MD5_LENGTH, MD5_LENGTH));
    }
    
    public static boolean isPasswordValid(String value, boolean hash) {
        return hash ? isPasswordHashValid(value) : isPasswordValid(value);
    }
    
    public static boolean isEmailValid(String value) {
        if (value == null) return false;
        if (value.isEmpty()) return true;
        return PATTERN_EMAIL.matcher(value).matches();
    }
    
    public static String md5Hex(String s) {
        return new String(Hex.encodeHex(DigestUtils.md5(s)));
    }
    
    private static String createRule(int min, int max) {
        return "^[a-zA-Z0-9]{" + min + "," + max + "}$";
    }
    
}