package org.dyndns.fzoli.mill.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dyndns.fzoli.mill.common.key.MillServletURL;
import org.dyndns.fzoli.mill.common.key.ModelKeys;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerEvent;
import org.dyndns.fzoli.mill.server.model.PlayerModel;
import org.dyndns.fzoli.mill.server.model.dao.PlayerDAO;
import org.dyndns.fzoli.mill.server.model.dao.ValidatorDAO;
import org.dyndns.fzoli.mill.server.model.entity.ConvertUtil;
import org.dyndns.fzoli.mill.server.model.entity.Player;
import org.dyndns.fzoli.mill.server.model.entity.Validator;
import org.dyndns.fzoli.mvc.server.model.bean.ModelBean;
import org.dyndns.fzoli.mvc.server.model.bean.ModelBeanRegister;

/**
 *
 * @author zoli
 */
@WebServlet(urlPatterns={MillServletURL.VALIDATOR})
public class ValidatorServlet extends HttpServlet {

    public static final String KEY_KEY = "key";
    public static final String KEY_ACTION = "action";
    
    public static final String ACTION_VALIDATE = "validation";
    public static final String ACTION_INVALIDATE = "invalidation";
    
    public static enum ValidateReturn {
        OK,
        USED,
        NOT_OK,
        REMOVED
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException { //TODO
        resp.setContentType("text/html;charset=utf-8");
        PrintWriter out = resp.getWriter();
        out.print("<html><head><title>E-mail cím érvényesítés</title></head><body>");
        String key = req.getParameter(KEY_KEY);
        String action = req.getParameter(KEY_ACTION);
        if (action == null) action = ACTION_VALIDATE;
        Player player = ValidatorDAO.getPlayer(key);
        ValidateReturn ret;
        if (action.equals(ACTION_VALIDATE)) ret = validate(key);
        else ret = invalidate(key);
        switch (ret) {
            case OK:
                out.print("Kedves " + player.getName() + "!<br />");
                out.print("Sikeresen érvényesítette e-mail címét.");
                break;
            case USED:
                out.print("A megadott kód már fel lett használva!");
                break;
            case REMOVED:
                out.print("E-mail címét eltávolítottuk adatbázisunkból!");
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
    
    public static ValidateReturn invalidate(String key) { //TODO
        return ValidateReturn.REMOVED;
    }
    
    public static ValidateReturn validate(String key) {
        if (key == null) return ValidateReturn.NOT_OK;
        final Validator v = ValidatorDAO.getValidator(key);
        if (v == null) return ValidateReturn.NOT_OK;
        final Player p = v.getPlayer();
        if (p == null) return ValidateReturn.USED;
        new Thread(new Runnable() {

            @Override
            public void run() {
                p.setValidated(true);
                PlayerDAO.save(p);
                v.setPlayer(null);
                ValidatorDAO.save(v);
                List<ModelBean> l = ModelBeanRegister.getModelBeans();
                synchronized(l) {
                    for (ModelBean bean : l) {
                        PlayerModel m = (PlayerModel) bean.getModel(ModelKeys.PLAYER);
                        if (m == null || m.getPlayer() == null) continue;
                        if (m.getPlayer().getPlayerName().equals(p.getPlayerName())) {
                            m.addEvent(new PlayerEvent(ConvertUtil.createPlayer(m, p), PlayerEvent.PlayerEventType.VALIDATE));
                            break;
                        }
                    }
                }
            }
            
        }).start();
        return ValidateReturn.OK;
    }
    
}