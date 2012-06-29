package org.dyndns.fzoli.mill.server.servlet;

import java.io.File;
import org.dyndns.fzoli.email.GMailSender;

/**
 *
 * @author zoli
 */
class EmailServletUtil {

    private EmailServletUtil() {
    }
    
    public static void sendEmail(EmailServlet servlet, String address, String subject, String msg) {
        String path = servlet.getCtxInitParameter("gmail-authenticator");
        if (path == null) throw new NullPointerException("gmail-authenticator not configured in web.xml");
        GMailSender.sendEmail(new File(path), address, subject, msg);
    }
    
}