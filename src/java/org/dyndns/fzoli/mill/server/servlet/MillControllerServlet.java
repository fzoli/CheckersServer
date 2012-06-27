package org.dyndns.fzoli.mill.server.servlet;

import javax.servlet.annotation.WebServlet;
import org.dyndns.fzoli.mill.common.key.MillServletURL;
import org.dyndns.fzoli.mvc.server.servlet.controller.JSONControllerServlet;

/**
 *
 * @author zoli
 */
@WebServlet(urlPatterns={MillServletURL.CONTROLLER})
public final class MillControllerServlet extends JSONControllerServlet {

}