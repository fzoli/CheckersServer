package org.dyndns.fzoli.language;

import java.io.File;
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
    
    private String language;
    private ServletContext context;

    public LanguageResource() {
    }

    public LanguageResource(PageContext context) {
        this(context.getRequest());
    }
    
    public LanguageResource(ServletRequest request) {
        language = request.getLocale().getLanguage();
        context = request.getServletContext();
    }

    public String getLanguage() {
        return language;
    }
    
    public String getString(String key) {
        File xmlFile = getResourceFile(context, language, "strings.xml");
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
        this.language = language;
    }
    
    public void setPageContext(PageContext context) {
        this.language = context.getRequest().getLocale().getLanguage();
        this.context = context.getServletContext();
    }
    
    public static File getResourceFile(ServletContext context, String lang, String filename) {
        if (context == null) throw new NullPointerException("ServletContext is null");
        File xmlFile = new File(context.getRealPath("/WEB-INF/" + lang + "-" + filename));
        if (!xmlFile.isFile()) xmlFile = new File(context.getRealPath("/WEB-INF/" + filename));
        if (!xmlFile.isFile()) throw new NullPointerException(filename + " not exists");
        return xmlFile;
    }
    
}