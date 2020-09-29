
<%-- allerts dialog --%>
<c:if test="${not empty alert_type}">
    <div class="alert alert-${alert_type} alert-dismissible" role="alert">
        <button type="button" class="close" data-dismiss="alert" aria-label="Close">
            <span aria-hidden="true">x</span>
        </button>
        <strong>${alert_msg}</strong>
    </div>
</c:if>

<%-- images --%>
<c:url var="editImgUrl" value="/resources/img/edit.png" />
<c:url var="returnImgUrl" value="/resources/img/return.png" />
<c:url var="deleteImgUrl" value="/resources/img/delete.png" />
<c:url var="addImgUrl" value="/resources/img/add.png" />
