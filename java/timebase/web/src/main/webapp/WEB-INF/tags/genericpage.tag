<%@tag description="Overall Page template" pageEncoding="UTF-8"%>

<%@attribute name="header" fragment="true" %>
<%@attribute name="menu" fragment="true" %>
<%@attribute name="footer" fragment="true" %>

<html>
    <jsp:invoke fragment="header"/>
    <body>
        <jsp:invoke fragment="menu"/>

        <div class="bodybox">
            <div class="container">
                <jsp:doBody/>
            </div>
        </div>

        <div class="footer navbar-fixed-bottom" id="footer">
            <jsp:invoke fragment="footer"/>
        </div>
    </body>
</html>