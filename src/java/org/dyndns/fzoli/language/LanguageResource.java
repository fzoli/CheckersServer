package org.dyndns.fzoli.language;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
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
    
    private File xmlFile;
    private String language;
    private ServletContext context;

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
        context = request.getServletContext();
        language = LanguageServlet.createLanguage(request);
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
                String k = ((Element)node).getAttribute("key");
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
    
}