package org.dyndns.fzoli.mill.server.servlet;

import javax.servlet.annotation.WebServlet;
import org.dyndns.fzoli.language.LanguageServlet;
import org.dyndns.fzoli.mill.common.key.MillServletURL;

/**
 *
 * @author zoli
 */
@WebServlet(urlPatterns={MillServletURL.LANGUAGE})
public class MillLanguageServlet extends LanguageServlet {
    
}