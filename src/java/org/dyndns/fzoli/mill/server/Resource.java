package org.dyndns.fzoli.mill.server;

import javax.servlet.ServletRequest;
import org.dyndns.fzoli.language.LanguageResource;
import org.dyndns.fzoli.mill.server.servlet.MillLanguageServlet;

/**
 *
 * @author zoli
 */
public class Resource extends LanguageResource {

    public Resource() {
    }

    public Resource(ServletRequest request) {
        super(request);
    }
    
    public String getLanguageSetterUrls() {
        if (request == null) return null;
        return MillLanguageServlet.createLanguageSetterUrls(request);
    }
    
    public String getEmailValidation() {
        return getString("email_validation");
    }
    
    public String getDear() {
        return getString("dear");
    }
    
    public String getUser() {
        return getString("user");
    }
    
    public String getDearClose() {
        return getString("dear_close");
    }
    
    public String getSuccessfulEmailValidation() {
        return getString("successful_email_validation");
    }
    
    public String getSuccessfulEmailDelete() {
        return getString("successful_email_delete");
    }
    
    public String getKeyUsed() {
        return getString("key_used");
    }
    
    public String getKeyInvalid() {
        return getString("key_invalid");
    }
    
    public String getInvalidRequest() {
        return getString("invalid_request");
    }
    
}