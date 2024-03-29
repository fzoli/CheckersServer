package org.dyndns.fzoli.mill.server.servlet;

import java.io.File;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import org.dyndns.fzoli.mill.common.key.MillServletURL;
import org.dyndns.fzoli.mvc.server.servlet.controller.JSONControllerServlet;

/**
 *
 * @author zoli
 */
@WebServlet(urlPatterns={MillServletURL.CONTROLLER})
public final class MillControllerServlet extends JSONControllerServlet {

    public static String getHost(HttpServletRequest hsr) {
        String host = hsr.getServletContext().getInitParameter("public-host");
        return host == null ? (hsr.isSecure() ? "https" : "http") + "://" + hsr.getRemoteAddr() + ":" + hsr.getLocalPort()  + hsr.getContextPath(): host;
    }
    
    public static File getEmailConfig(HttpServletRequest hsr) {
        try {
            return new File(hsr.getServletContext().getInitParameter("gmail-authenticator"));
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
}