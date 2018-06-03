<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title><g:message code="index.title"/></title>
	</head>
	<body>
    <h1><g:message code="index.title"/></h1>
    <div clas="row blurb">
        <div class="col-lg-12 col-md-12 col-sm-12">
            <g:message code="index.description"/>
        </div>
    </div>
    <g:form controller="normalisation">
    <div class="row">
        <div class="col-lg-4 col-md-4 col-sm-6">
            <g:actionSubmit value="${message(code: 'normalisation.distribution.label')}" class="btn btn-default" action="normaliseDistributions"/>
        </div>
        <div class="col-lg-8 col-md-8 col-sm-6">
            <g:message code="normalisation.distribution.description"/>
        </div>
    </div>
    </g:form>
	</body>
</html>
