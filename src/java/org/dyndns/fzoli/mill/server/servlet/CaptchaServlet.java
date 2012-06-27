package org.dyndns.fzoli.mill.server.servlet;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.captcha.Captcha;
import nl.captcha.backgrounds.BackgroundProducer;
import nl.captcha.backgrounds.FlatColorBackgroundProducer;
import nl.captcha.gimpy.GimpyRenderer;
import nl.captcha.gimpy.RippleGimpyRenderer;
import nl.captcha.noise.CurvedLineNoiseProducer;
import nl.captcha.noise.NoiseProducer;
import nl.captcha.servlet.CaptchaServletUtil;
import nl.captcha.text.producer.DefaultTextProducer;
import nl.captcha.text.producer.TextProducer;
import nl.captcha.text.renderer.ColoredEdgesWordRenderer;
import nl.captcha.text.renderer.WordRenderer;

/**
 *
 * @author zoli
 */
@WebServlet(
        urlPatterns={"/Captcha"}
)
public class CaptchaServlet extends HttpServlet {
    
    private final static int W = 200, H = 50;
    private final static GimpyRenderer GR = new RippleGimpyRenderer();
    private final static BackgroundProducer BP = new FlatColorBackgroundProducer(Color.WHITE);
    private final static NoiseProducer NP = new CurvedLineNoiseProducer(Color.WHITE, 2);
    private final static TextProducer TP = new DefaultTextProducer(6, new char[]{'q', 'w', 'e', 'r', 't', 'z', 'u', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'y', 'x', 'c', 'v', 'b', 'n', 'm'});
    private final static WordRenderer WR = new ColoredEdgesWordRenderer(new ArrayList<Color>() {{add(Color.BLACK);}}, new ArrayList<Font>() {{add(new Font("Arial", Font.BOLD, 40));}}, 2);
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Captcha captcha = new Captcha.Builder(W, H).addText(TP, WR).addBackground(BP).gimp(GR).addBorder().addNoise(NP).build();
        CaptchaServletUtil.writeImage(resp, captcha.getImage());
        System.out.println("Answer: " + captcha.getAnswer());
    }
    
}