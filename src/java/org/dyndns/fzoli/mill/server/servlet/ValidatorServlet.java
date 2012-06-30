package org.dyndns.fzoli.mill.server.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dyndns.fzoli.mill.common.key.MillServletURL;

/**
 *
 * @author zoli
 */
@WebServlet(urlPatterns={MillServletURL.VALIDATOR})
public class ValidatorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String key = req.getParameter("key");
        resp.getWriter().print(key == null ? "-" : key);
        resp.getWriter().close();
    }
    
    @Override
    public String getServletInfo() {
        return "E-mail address validator servlet.";
    }
    
}