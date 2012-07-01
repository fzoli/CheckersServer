package org.dyndns.fzoli.mill.server.servlet;

/**
 *
 * @author zoli
 */
public class ValidatorInfo {
    
    public enum Return {
        VALIDATED,
        USED,
        NOT_OK,
        REMOVED
    }
    
    private Return ret;
    private String name, key;
    private boolean processed;

    public ValidatorInfo() {
        this.processed = false;
    }

    public ValidatorInfo(Return ret, String key) {
        this(ret, key, null);
    }
    
    public ValidatorInfo(Return ret, String key, String name) {
        this.ret = ret;
        this.key = key;
        this.name = name;
        this.processed = true;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public Return getReturn() {
        return ret;
    }

    public boolean isProcessed() {
        return processed;
    }
    
    public boolean isKeySpecified() {
        return getKey() != null;
    }
    
    public boolean isReturnValidated() {
        return isReturn(Return.VALIDATED.name());
    }
    
    public boolean isReturnNotOk() {
        return isReturn(Return.NOT_OK.name());
    }
    
    public boolean isReturnUsed() {
        return isReturn(Return.USED.name());
    }
    
    public boolean isReturnRemoved() {
        return isReturn(Return.REMOVED.name());
    }
    
    public boolean isReturn(String value) {
        try {
            return ret.equals(Return.valueOf(value));
        }
        catch (Exception ex) {
            return false;
        }
    }
    
}