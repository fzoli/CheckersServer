package org.dyndns.fzoli.mill.server.servlet;

import javax.servlet.annotation.WebServlet;
import org.dyndns.fzoli.mill.common.key.MillServletURL;
import org.dyndns.fzoli.mvc.server.servlet.controller.JSONControllerServlet;

/**
 *
 * @author zoli
 */
@WebServlet(urlPatterns={MillServletURL.CONTROLLER})
public final class MillControllerServlet extends JSONControllerServlet implements EmailServlet {

    @Override
    public void sendEmail(String address, String subject, String msg) {
        EmailServletUtil.sendEmail(this, address, subject, msg);
    }

    @Override
    public String getCtxInitParameter(String name) {
        return super.getCtxInitParameter(name);
    }
    
}