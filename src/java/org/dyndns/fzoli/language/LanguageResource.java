package org.dyndns.fzoli.language;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

/**
 *
 * @author zoli
 */
public class LanguageResource {
    
    private static final String EN = "en";
    
    private String language;

    public LanguageResource() {
    }

    public LanguageResource(PageContext context) {
        this(context.getRequest());
    }
    
    public LanguageResource(ServletRequest request) {
        language = request.getLocale().getLanguage();
    }

    public String getLanguage() {
        if (language == null) return EN;
        return language;
    }

    public String getText(String key) {
        return getText(getLanguage(), key);
    }
    
    public static String getText(String lang, String key) {
        if (key == null) return "";
        if (lang == null) lang = EN;
        //TODO
        return "";
    }
    
    public void setPageContext(PageContext context) {
        language = context.getRequest().getLocale().getLanguage();
    }
    
}