package org.dyndns.fzoli.mill.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dyndns.fzoli.mill.common.key.MillServletURL;
import org.dyndns.fzoli.mill.server.model.dao.PlayerDAO;
import org.dyndns.fzoli.mill.server.model.dao.ValidatorDAO;
import org.dyndns.fzoli.mill.server.model.entity.Player;
import org.dyndns.fzoli.mill.server.model.entity.Validator;

/**
 *
 * @author zoli
 */
@WebServlet(urlPatterns={MillServletURL.VALIDATOR})
public class ValidatorServlet extends HttpServlet {

    public static enum ValidateReturn {
        OK,
        USED,
        NOT_OK
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter out = resp.getWriter();
        out.print("<html><head><title>E-mail cím érvényesítés</title></head><body>");
        String key = req.getParameter("key");
        Player player = ValidatorDAO.getPlayer(key);
        ValidateReturn ret = validate(key);
        switch (ret) {
            case OK:
                out.print("Kedves " + player.getName() + "!<br />");
                out.print("Sikeresen érvényesítette e-mail címét.");
                break;
            case USED:
                out.print("A megadott kód már fel lett használva!");
                break;
            default:
                out.print("Érvénytelen kód!");
        }
        out.print("</body></html>");
        out.close();
    }
    
    @Override
    public String getServletInfo() {
        return "E-mail address validator servlet.";
    }
    
    public static ValidateReturn validate(String key) {
        if (key == null) return ValidateReturn.NOT_OK;
        Validator v = ValidatorDAO.getValidator(key);
        if (v == null) return ValidateReturn.NOT_OK;
        Player p = v.getPlayer();
        if (p == null) return ValidateReturn.USED;
        p.setValidated(true);
        PlayerDAO.save(p);
        v.setPlayer(null);
        ValidatorDAO.save(v);
        return ValidateReturn.OK;
    }
    
}