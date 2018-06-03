<%--
  Created by IntelliJ IDEA.
  User: pal155
  Date: 27/4/18
  Time: 1:58 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="normalisation.distribution.report.title"/></title>
</head>
<body>
<h1><g:message code="normalisation.distribution.report.title"/></h1>
<div class="row">
    <div class="col-lg-12 col-md-12 col-sm-12">
        <table class="table table-striped">
            <thead>
            <tr>
                <th><g:message code="normalisation.distribution.report.element.label"/></th>
                <th><g:message code="normalisation.distribution.report.description.label"/></th>
                <th><g:message code="normalisation.distribution.report.errors.label"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each var="msg" in="${report}">
                <tr>
                    <td><span mapper-link="${msg.link}">${raw(msg.element)}</span></td>
                    <td><g:message code="${msg.code}" args="${msg.args}"/></td>
                    <td>${msg.errors}</td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</div>
<script type="application/javascript">
    $(function() {
        mapperLinks();
    })
</script>
</body>
</html>