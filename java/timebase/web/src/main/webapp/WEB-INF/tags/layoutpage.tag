<%@tag description="Page template with header and footer" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<%@attribute name="headerFile" required="true"%>
<%@attribute name="menuFile" required="true"%>
<%@attribute name="footerFile" required="true"%>

<t:genericpage>
    <jsp:attribute name="header">
        <jsp:include page="${headerFile}"/>
    </jsp:attribute>
    <jsp:attribute name="menu">
        <jsp:include page="${menuFile}"/>
    </jsp:attribute>
    <jsp:attribute name="footer">
        <jsp:include page="${footerFile}"/>
    </jsp:attribute>

    <jsp:body>
        <jsp:doBody/>
    </jsp:body>
</t:genericpage>