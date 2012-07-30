package org.dyndns.fzoli.language;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author zoli
 */
public class LanguageResource {
    
    protected File xmlFile;
    protected String language;
    protected ServletContext context;
    protected HttpServletRequest request;

    private static final String WEB_INF = "/WEB-INF/";
    private static final String STRINGS_XML = "strings.xml";
    
    public LanguageResource() {
    }

    public LanguageResource(PageContext context) {
        this(context.getRequest());
    }
    
    public LanguageResource(ServletRequest request) {
        init(request);
    }

    private void init(ServletRequest request) {
        this.context = request.getServletContext();
        this.language = LanguageServlet.createLanguage(request);
        if (request instanceof HttpServletRequest) {
            this.request = (HttpServletRequest) request;
        }
    }
    
    public String getLanguage() {
        return language;
    }
    
    public String getString(String key) {
        if (xmlFile == null) xmlFile = getResourceFile(context, language, STRINGS_XML);
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            NodeList strings = doc.getDocumentElement().getElementsByTagName("string");
            for (int i = 0; i < strings.getLength(); i++) {
                Node node = strings.item(i);
                String k = ((Element)node).getAttribute("name");
                if (k.equals(key)) return node.getTextContent();
            }
            return null;
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setLanguage(String language) {
        this.xmlFile = null;
        this.language = language;
    }
    
    public void setPageContext(PageContext context) {
        init(context.getRequest());
    }
    
    public File getResourceFile(String filename) {
        return getResourceFile(context, language, filename);
    }
    
    public static File getResourceFile(ServletContext context, String lang, String filename) {
        if (context == null) throw new NullPointerException("ServletContext is null");
        File xmlFile = new File(context.getRealPath(WEB_INF + lang + "-" + filename));
        if (!xmlFile.isFile()) xmlFile = new File(context.getRealPath(WEB_INF + filename));
        if (!xmlFile.isFile()) throw new NullPointerException(filename + " not exists");
        return xmlFile;
    }
    
    private static boolean contains(List<Locale> locales, Locale locale) {
        for (Locale l : locales) {
            if (l.getLanguage().equals(locale.getLanguage())) return true;
        }
        return false;
    }
    
    public static List<Locale> getLocales(ServletContext context) {
        List<Locale> l = new ArrayList<Locale>();
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            if(!contains(l, locale) && new File(context.getRealPath(WEB_INF + locale.getLanguage() + "-" + STRINGS_XML)).isFile()) l.add(locale);
        }
        return l;
    }
    
    public static String createLanguageUrls(ServletRequest hsr, String url, Locale defLocale, boolean startAmp) {
        return createLanguageUrls(hsr, url, defLocale, startAmp, null);
    }
    
    public static String createLanguageUrls(ServletRequest hsr, String url, Locale defLocale, boolean startAmp, String clazz) {
        if (clazz == null) clazz = "lang_url";
        StringBuilder langs = new StringBuilder();
        List<Locale> locales = getLocales(hsr.getServletContext());
        appendLangUrl(langs, url, defLocale, startAmp, clazz);
        for (Locale locale : locales) {
            appendLangUrl(langs, url, locale, startAmp, clazz);
        }
        return langs.substring(0, langs.length() - 7);
    }
    
    private static void appendLangUrl(StringBuilder langs, String langUrl, Locale locale, boolean startAmp, String clazz) {
        String start = startAmp ? "&amp;" : "?";
        String dl = locale.getDisplayLanguage();
        dl = String.valueOf(dl.charAt(0)).toUpperCase() + dl.substring(1);
        langs.append("<a class=\"")
        .append(clazz)
        .append("\" href=\"")
        .append(langUrl)
        .append(start)
        .append(LanguageServlet.KEY_LANG)
        .append('=')
        .append(locale.getLanguage())
        .append("\">")
        .append(dl)
        .append("</a>")
        .append(",&nbsp;");
    }
    
}