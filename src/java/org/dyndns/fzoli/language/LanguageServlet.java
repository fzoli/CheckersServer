package org.dyndns.fzoli.language;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author zoli
 */
public class LanguageServlet extends HttpServlet {
    
    public static final String KEY_LANG = "lang";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }
    
    private void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String l = req.getParameter(KEY_LANG);
        if (l != null) {
            req.getSession(true).setAttribute(KEY_LANG, l);
        }
        redirect(req, resp);
    }
    
    private void redirect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String referer = req.getHeader("Referer");
        resp.sendRedirect(referer == null ? (req.getServletContext().getContextPath()) : referer);
    }
    
    @Override
    public String getServletInfo() {
        return "Language resource setter.";
    }
    
    public static String createLanguage(ServletRequest request) {
        String language = request.getLocale().getLanguage();
        String l = request.getParameter(KEY_LANG);
        if (l == null) {
            if (request instanceof HttpServletRequest) {
                HttpSession session = ((HttpServletRequest)request).getSession(false);
                if (session != null) {
                    l = (String) session.getAttribute(KEY_LANG);
                    if (l != null) {
                        language = l;
                    }
                }
            }
        }
        else {
            language = l;
        }
        return language;
    }
    
}