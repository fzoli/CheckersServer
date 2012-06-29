package org.dyndns.fzoli.email;

import java.io.File;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GMailSender {

    private static class AuthenticatorData {
        
        String user;
        Authenticator authenticator;

        AuthenticatorData(String user, Authenticator authenticator) {
            this.user = user;
            this.authenticator = authenticator;
        }
        
    }
    
    private static Properties createGmailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        return props;
    }
    
    private static AuthenticatorData createAuthenticator(File fXmlFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            Element e = doc.getDocumentElement();
            final String user = e.getAttribute("user");
            final String password = e.getAttribute("password");
            if (user == null || password == null) throw new Exception("Wrong GMail XML file");
            Authenticator authenticator = new Authenticator() {

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }
                    
            };
            return new AuthenticatorData(user, authenticator);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static void sendEmail(File xml, String address, String subject, String msg) {
        AuthenticatorData data = createAuthenticator(xml);
        Session session = Session.getDefaultInstance(createGmailProperties(), data.authenticator);
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(data.user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(address));
            message.setSubject(subject);
            message.setText(msg);
            Transport.send(message);
        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) {
        sendEmail(new File("/home/zoli/gmail-authenticator.xml"), "f.zoli@mailbox.hu", "Testing Subject", "Dear Mail Crawler,\n\n No spam to my email, please!");
    }
    
}