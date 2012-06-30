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
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter out = resp.getWriter();
        out.print("<html><head><title>E-mail cím érvényesítés</title></head><body>");
        String key = req.getParameter("key");
        Player player = ValidatorDAO.getPlayer(key);
        if (validate(key)) {
            out.print("Kedves " + player.getName() + "!<br />");
            out.print("Sikeresen érvényesítette e-mail címét.");
        }
        else {
            out.print("Érvénytelen kérés!");
        }
        out.print("</body></html>");
        out.close();
    }
    
    @Override
    public String getServletInfo() {
        return "E-mail address validator servlet.";
    }
    
    public static boolean validate(String key) {
        if (key == null) return false;
        Player player = ValidatorDAO.getPlayer(key);
        if (player == null) return false;
        ValidatorDAO.removeKey(player);
        player.setValidated(true);
        return true;
    }
    
}