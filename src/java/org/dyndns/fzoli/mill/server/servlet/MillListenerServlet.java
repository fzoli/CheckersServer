package org.dyndns.fzoli.mill.server.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.dyndns.fzoli.mill.common.key.MillServletURL;
import org.dyndns.fzoli.mill.common.key.ModelKeys;
import org.dyndns.fzoli.mill.server.model.PlayerModel;
import org.dyndns.fzoli.mvc.server.model.bean.ModelBean;
import org.dyndns.fzoli.mvc.server.model.bean.ModelBeanRegister;
import org.dyndns.fzoli.mvc.server.servlet.listener.JSONListenerServlet;

/**
 *
 * @author zoli
 */
@WebServlet(
        urlPatterns={MillServletURL.LISTENER}, 
        initParams ={
            @WebInitParam(name=MillListenerServlet.PARAM_EVENT_DELAY, value="50"),
            @WebInitParam(name=MillListenerServlet.PARAM_EVENT_TIMEOUT, value="20000"),
            @WebInitParam(name=MillListenerServlet.PARAM_GC_DELAY, value="60000")
        }
)
public final class MillListenerServlet extends JSONListenerServlet implements EmailServlet {

    private static Date lastTime;
    
    @Override
    protected void printResponse(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final Date now = new Date();
        if (lastTime == null) lastTime = now;
        if (new Date().getTime() - lastTime.getTime() >= 60000) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    List<ModelBean> beans = ModelBeanRegister.getModelBeans();
                    for (ModelBean bean : beans) {
                        HttpSession s = bean.getSession();
                        if (s == null) continue;
                        try {
                            if (now.getTime() - s.getLastAccessedTime() >= 30000) {
                                PlayerModel m = (PlayerModel) bean.getModel(ModelKeys.PLAYER);
                                if (m == null) continue;
                                m.onDisconnect();
                            }
                        }
                        catch (Exception ex) {
                            continue;
                        }
                    }
                }
                
            }).start();
            lastTime = now;
        }
        super.printResponse(request, response);
    }

    @Override
    public void sendEmail(String address, String subject, String msg) {
        EmailServletUtil.sendEmail(this, address, subject, msg);
    }

    @Override
    public String getCtxInitParameter(String name) {
        return super.getCtxInitParameter(name);
    }
    
}