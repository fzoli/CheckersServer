package org.dyndns.fzoli.mill.server.servlet;

import java.io.IOException;
import java.util.List;
import javax.servlet.RequestDispatcher;
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
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("validator_info", process(req));
        forward(req, resp, "/validator.jspx");
    }
    
    @Override
    public String getServletInfo() {
        return "E-mail address validator servlet.";
    }
    
    private ValidatorInfo process(HttpServletRequest req) {
        String key = req.getParameter(KEY_KEY);
        String action = req.getParameter(KEY_ACTION);
        if (action == null) action = ACTION_INVALIDATE;
        if (action.equals(ACTION_VALIDATE)) return validate(key);
        else return invalidate(key);
    }
    
    public static ValidatorInfo invalidate(String key) { //TODO
        return new ValidatorInfo(ValidatorInfo.Return.REMOVED, key);
    }
    
    public static ValidatorInfo validate(String key) {
        if (key == null) return new ValidatorInfo(ValidatorInfo.Return.NOT_OK, key);
        final Validator v = ValidatorDAO.getValidator(key);
        if (v == null) return new ValidatorInfo(ValidatorInfo.Return.NOT_OK, key);
        final Player p = v.getPlayer();
        if (p == null) return new ValidatorInfo(ValidatorInfo.Return.USED, key);
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
        return new ValidatorInfo(ValidatorInfo.Return.OK, key, p.getName());
    }
    
    protected void forward(HttpServletRequest req, HttpServletResponse resp, String jsp) throws ServletException, IOException {
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(jsp);
        dispatcher.forward(req, resp);
    }
    
}