package org.dyndns.fzoli.mill.common.model.entity;

/**
 *
 * @author zoli
 */
public enum MessageType {
    CHAT,
    SYSTEM,
    SUPPORT;
    
    public enum SystemMessage {
        SIGN_IN,
        SIGN_OUT,
        CHAT_OPEN,
        CHAT_CLOSE,
        PLAY_REQUEST
    }
    
}