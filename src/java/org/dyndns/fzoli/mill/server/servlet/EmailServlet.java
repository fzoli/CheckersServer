package org.dyndns.fzoli.mill.server.servlet;

/**
 *
 * @author zoli
 */
public interface EmailServlet {
    
    String getCtxInitParameter(String string);
    
    void sendEmail(String address, String subject, String msg);
    
}