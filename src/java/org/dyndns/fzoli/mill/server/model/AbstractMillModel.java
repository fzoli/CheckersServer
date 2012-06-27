package org.dyndns.fzoli.mill.server.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import nl.captcha.Captcha;
import nl.captcha.backgrounds.BackgroundProducer;
import nl.captcha.backgrounds.FlatColorBackgroundProducer;
import nl.captcha.gimpy.GimpyRenderer;
import nl.captcha.gimpy.RippleGimpyRenderer;
import nl.captcha.noise.CurvedLineNoiseProducer;
import nl.captcha.noise.NoiseProducer;
import nl.captcha.text.producer.DefaultTextProducer;
import nl.captcha.text.producer.TextProducer;
import nl.captcha.text.renderer.ColoredEdgesWordRenderer;
import nl.captcha.text.renderer.WordRenderer;
import org.dyndns.fzoli.mill.common.key.BaseKeys;
import org.dyndns.fzoli.mill.common.key.ModelKeys;
import org.dyndns.fzoli.mill.server.model.entity.Player;
import org.dyndns.fzoli.mvc.common.request.map.RequestMap;
import org.dyndns.fzoli.mvc.server.model.JSONModel;

/**
 *
 * @author zoli
 */
abstract class AbstractMillModel<EventObj, PropsObj> extends JSONModel<EventObj, PropsObj> implements BaseKeys {
    
    private final static GimpyRenderer GR = new RippleGimpyRenderer();
    private final static TextProducer TP = new DefaultTextProducer(6, new char[]{'q', 'w', 'e', 'r', 't', 'z', 'u', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'y', 'x', 'c', 'v', 'b', 'n', 'm'});
    
    private int w = 200, h = 50;
    private Color fgc = Color.WHITE, bgc = Color.BLACK;
    private BackgroundProducer bp = new FlatColorBackgroundProducer(bgc);
    private NoiseProducer np = createProducer();
    private WordRenderer wr = createRenderer();
    
    private boolean captchaValidated = false;
    private Captcha captcha;

    @Override
    protected RenderedImage getImage(RequestMap rm) {
        String action = rm.getFirst(KEY_REQUEST);
        if (action != null) {
            if (action.equals(REQ_GET_CAPTCHA)) return getCaptcha();
            if (action.equals(REQ_CREATE_CAPTCHA)) return createCaptcha();
        }
        return super.getImage(rm);
    }

    @Override
    protected int askModel(RequestMap rm) {
        String action = rm.getFirst(KEY_REQUEST);
        if (action != null) {
            String val = rm.getFirst(KEY_VALUE);
            if (val != null) {
                if (action.equals(REQ_VALIDATE_CAPTCHA)) {
                    return validate(val) ? 0 : 1;
                }
                try {
                    if (action.equals(REQ_SET_CAPTCHA_COLOR)) {
                        setCaptchaBgColor(Boolean.parseBoolean(val));
                        return 0;
                    }
                    if (action.equals(REQ_SET_CAPTCHA_SIZE)) {
                        setCaptchaSize(Integer.parseInt(val));
                        return 0;
                    }
                }
                catch (Exception ex) {}
            }
        }
        return -1;
    }
    
    public PlayerModel getPlayerModel() {
        return getPlayerModel(false);
    }
    
    public PlayerModel getPlayerModel(boolean init) {
        return findModel(ModelKeys.PLAYER, init, PlayerModel.class);
    }

    public int getCaptchaWidth() {
        return w;
    }
    
    public Player getPlayer() {
        PlayerModel model = getPlayerModel();
        if (model == null) return null;
        return model.getPlayer();
    }
    
    public org.dyndns.fzoli.mill.common.model.entity.Player getCommonPlayer() {
        PlayerModel model = getPlayerModel();
        if (model == null) return null;
        return model.getCommonPlayer();
    }
    
    public String getPlayerName() {
        Player player = getPlayer();
        if (player == null) return null;
        return player.getPlayerName();
    }

    protected boolean isCaptchaValidated() {
        return captchaValidated;
    }
    
    protected boolean validate(String answer) {
        boolean ok = captcha == null ? false : captcha.isCorrect(answer.toLowerCase());
        if (!ok) createCaptcha();
        captchaValidated = ok;
        return ok;
    }
    
    protected void removeCaptcha() {
        captcha = null;
        captchaValidated = false;
    }

    protected RenderedImage getCaptcha() {
        if (captcha == null) return createCaptcha();
        BufferedImage img = captcha.getImage();
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(fgc);
        g.drawRect(0, 0, img.getWidth() - 1, img.getHeight() - 1);
        return img;
    }
    
    protected void setCaptchaBgColor(boolean white) {
        bgc = white ? Color.WHITE : Color.BLACK;
        fgc = white ? Color.BLACK : Color.WHITE;
        bp = new FlatColorBackgroundProducer(bgc);
        np = createProducer();
        wr = createRenderer();
    }
    
    protected void setCaptchaSize(int w) {
        if (w < 200) w = 200;
        if (w > 1000) w = 1000;
        this.w = w;
        this.h = w / 4;
        wr = createRenderer();
        np = createProducer();
    }
    
    protected NoiseProducer createProducer() {
        return new CurvedLineNoiseProducer(bgc, w / 100);
    }
    
    protected WordRenderer createRenderer() {
        return new ColoredEdgesWordRenderer(new ArrayList<Color>() {{add(fgc);}}, new ArrayList<Font>() {{add(new Font("Arial", Font.BOLD, h - (w / 20)));}}, w / 100);
    }
    
    protected RenderedImage createCaptcha() {
        removeCaptcha();
        captcha = new Captcha.Builder(w, h).addText(TP, wr).addBackground(bp).gimp(GR).addBorder().addNoise(np).build();        
        return getCaptcha();
    }

}