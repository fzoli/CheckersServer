package org.dyndns.fzoli.mill.server.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dyndns.fzoli.language.LanguageResource;
import org.dyndns.fzoli.mill.common.key.MillServletURL;
import org.dyndns.fzoli.mill.common.key.ModelKeys;
import org.dyndns.fzoli.mill.common.model.pojo.PlayerEvent;
import org.dyndns.fzoli.mill.server.Resource;
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
    public static final String KEY_LANG = "lang";
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
        if (action.equals(ACTION_VALIDATE)) return validate(key, true);
        else return validate(key, false);
    }
    
    public static ValidatorInfo validate(String key, final boolean add) {
        if (key == null) return new ValidatorInfo(ValidatorInfo.Return.NOT_OK, key);
        final Validator v = ValidatorDAO.getValidator(key);
        if (v == null) return new ValidatorInfo(ValidatorInfo.Return.NOT_OK, key);
        final Player p = v.getPlayer();
        if (p == null) return new ValidatorInfo(ValidatorInfo.Return.USED, key);
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (add) p.setValidated(true);
                else p.setEmail("");
                PlayerDAO.save(p);
                v.setPlayer(null);
                ValidatorDAO.save(v);
                List<ModelBean> l = ModelBeanRegister.getModelBeans();
                synchronized(l) {
                    for (ModelBean bean : l) {
                        PlayerModel m = (PlayerModel) bean.getModel(ModelKeys.PLAYER);
                        if (m == null || m.getPlayer() == null) continue;
                        if (m.getPlayer().getPlayerName().equals(p.getPlayerName())) {
                            m.addEvent(new PlayerEvent(ConvertUtil.createPlayer(m, p), add ? PlayerEvent.PlayerEventType.VALIDATE : PlayerEvent.PlayerEventType.INVALIDATE));
                            break;
                        }
                    }
                }
            }
            
        }).start();
        return new ValidatorInfo(add ? ValidatorInfo.Return.VALIDATED : ValidatorInfo.Return.REMOVED, key, p.getName());
    }
    
    public static String getEmailValidationSubject(HttpServletRequest hsr) {
        return new Resource(hsr).getEmailValidation();
    }
    
    public static String createValidationEmail(HttpServletRequest hsr, String key, Player player) throws IOException {
        String host = MillControllerServlet.getHost(hsr);
        LanguageResource res = new Resource(hsr);
        String url = host + MillServletURL.VALIDATOR + "?" + ValidatorServlet.KEY_LANG + "=" + res.getLanguage() + "&" + ValidatorServlet.KEY_KEY + "=" + key + "&" + ValidatorServlet.KEY_ACTION + "=";
        String validationUrl = url + ValidatorServlet.ACTION_VALIDATE;
        String invalidationUrl = url + ValidatorServlet.ACTION_INVALIDATE;
        String out = readFileAsString(res.getResourceFile("validator-email.xhtml"))
        .replace("${css}", readFileAsString(res.getResourceFile("validator-email.css")))
        .replace("${user}", player.getName())
        .replace("${host}", host)
        .replace("${validation-url}", validationUrl)
        .replace("${invalidation-url}", invalidationUrl);
        return out;
    }
    
    private static String readFileAsString(File file) throws java.io.IOException {
        StringBuilder fileData = new StringBuilder(1000);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        char[] buf = new char[1024];
        int numRead;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
    
    protected void forward(HttpServletRequest req, HttpServletResponse resp, String jsp) throws ServletException, IOException {
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(jsp);
        dispatcher.forward(req, resp);
    }
    
}