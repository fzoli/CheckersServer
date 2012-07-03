package org.dyndns.fzoli.mill.server.servlet;

import java.util.Locale;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import org.dyndns.fzoli.language.LanguageResource;
import org.dyndns.fzoli.language.LanguageServlet;
import org.dyndns.fzoli.mill.common.key.MillServletURL;

/**
 *
 * @author zoli
 */
@WebServlet(urlPatterns={MillServletURL.LANGUAGE})
public class MillLanguageServlet extends LanguageServlet {
    
    public static String createLanguageSetterUrls(HttpServletRequest hsr) {
        return LanguageResource.createLanguageUrls(hsr, hsr.getServletContext().getContextPath() + MillServletURL.LANGUAGE, Locale.ENGLISH, false);
    }
    
}