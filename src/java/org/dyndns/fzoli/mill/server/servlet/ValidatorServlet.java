package org.dyndns.fzoli.mill.server.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import org.dyndns.fzoli.mill.common.key.MillServletURL;

/**
 *
 * @author zoli
 */
@WebServlet(urlPatterns={MillServletURL.VALIDATOR})
public class ValidatorServlet extends HttpServlet {
    
    @Override
    public String getServletInfo() {
        return "E-mail address validator servlet.";
    }
    
}