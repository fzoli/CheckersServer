<%-- 
    Document   : index
    Created on : 2012.05.02., 18:55:29
    Author     : zoli
--%>

<%@page import="org.dyndns.fzoli.mill.server.Resource"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1><%= new Resource(request).getString("hello_world") %></h1>
        <h2><a href="Language?lang=en">English</a>&nbsp;<a href="Language?lang=hu">Magyar</a></h2>
    </body>
</html>
