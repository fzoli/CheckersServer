package org.dyndns.fzoli.mill.common;

import java.util.Date;
import java.util.regex.Matcher;
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
    public final static int MIN_AGE = 1, MAX_AGE = 150;
    public final static int MD5_LENGTH = 32;
    
    private final static Pattern PATTERN_NAME = Pattern.compile("^[\\p{L}]{1,}[.]{0,1}[ ]{0,1}[\\p{L}-]{1,18}[\\p{L}]{1,1}$");
    private final static Pattern PATTERN_EMAIL = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$", Pattern.CASE_INSENSITIVE);
    private final static Pattern PATTERN_CAPTCHA = Pattern.compile("^[qwertzuopasdfghjkyxcvbnm]{6,6}$", Pattern.CASE_INSENSITIVE);
    private final static Pattern PATTERN_AGES = Pattern.compile("^\\s*([1-9]{1,1}\\d{0,2}){1,1}(\\s*-\\s*([1-9]{1,1}\\d{0,2}))?\\s*$");
    
    public static class AgeInterval {
        
        private final Integer from, to;
        
        public AgeInterval() {
            this(null);
        }
        
        public AgeInterval(Integer number) {
            this(number, null);
        }
        
        public AgeInterval(Integer from, Integer to) {
            this.from = from;
            this.to = to;
        }
        
        public Integer getFrom() {
            return from;
        }
        
        public Integer getTo() {
            return to == null ? from : to;
        }
        
        public boolean isEmpty() {
            return from == null;
        }
        
        @Override
        public String toString() {
            return "[" + (from == null ? "empty" : (from + (to == null ? "" : "," + to))) + "]";
        }
        
    }
    
    public static boolean isAgesValid(String value) {
        return !getAges(value).isEmpty();
    }
    
    public static AgeInterval getAges(String value) {
        if (value == null) return new AgeInterval();
        Matcher m = PATTERN_AGES.matcher(value);
        if (m.matches()) {
            Integer from = parseInt(m.group(1));
            Integer to = parseInt(m.group(3));
            if ((from != null && from >= MIN_AGE && from <= MAX_AGE) && (to == null || to >= MIN_AGE && to <= MAX_AGE)) {
                return new AgeInterval(from, to);
            }
        }
        return new AgeInterval();
    }
    
    public static boolean isCaptchaValid(String value) {
        return isValid(PATTERN_CAPTCHA, value);
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
        return isValid(PATTERN_EMAIL, value);
    }
    
    public static boolean isNameValid(String value) {
        if(value != null && !value.isEmpty()) {
            if(!value.contains(".") && !Character.isUpperCase(value.charAt(0))) return false;
            if (value.contains(".") && !value.contains(" ")) return false;
        }
        return isValid(PATTERN_NAME, value);
    }
    
    public static boolean isBirthDateValid(Date date) {
        if (date == null) return false;
        Date now = new Date();
        long l = Math.abs(date.getTime() - now.getTime());
        return !(date.after(now) || l > 150 * 365.24 * 24 * 60 * 60 * 1000 || l < 2 * 365.24 * 24 * 60 * 60 * 1000);
    }
    
    public static String md5Hex(String s) {
        return new String(Hex.encodeHex(DigestUtils.md5(s)));
    }
    
    private static boolean isValid(Pattern pattern, String value) {
        if (value == null) return false;
        return pattern.matcher(value).matches();
    }
    
    private static String createRule(int min, int max) {
        return "^[a-zA-Z0-9]{" + min + "," + max + "}$";
    }
    
    private static Integer parseInt(String s) {
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException ex) {
            return null;
        }
    }
    
}