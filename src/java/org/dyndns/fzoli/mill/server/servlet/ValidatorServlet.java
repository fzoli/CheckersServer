package org.dyndns.fzoli.mill.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dyndns.fzoli.mill.common.key.MillServletURL;
import org.dyndns.fzoli.mill.server.model.dao.ValidatorDAO;
import org.dyndns.fzoli.mill.server.model.entity.Player;

/**
 *
 * @author zoli
 */
@WebServlet(urlPatterns={MillServletURL.VALIDATOR})
public class ValidatorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String key = req.getParameter("key");
        Player player = ValidatorDAO.getPlayer(key);
        PrintWriter out = resp.getWriter();
        out.print("<html>Key: ");
        out.print(key == null ? "-" : key);
        out.print("<br>User: ");
        out.print(player == null ? "-" : player.getPlayerName());
        out.print("</html>");
        out.close();
    }
    
    @Override
    public String getServletInfo() {
        return "E-mail address validator servlet.";
    }
    
}