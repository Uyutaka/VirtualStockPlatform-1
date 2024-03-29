<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>

<html>
<head>
<title>User Profile</title>
<!-- reference our style sheet -->
<!-- Bootstrap CSS -->
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css" integrity="sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm" crossorigin="anonymous">
<link type="text/css" rel="stylesheet"
	href="${pageContext.request.contextPath}/resources/css/user-profile.css"/>
</head>

<body>
	<div id="wrapper">
		<div id="header">
			<h2>User profile platform</h2>
		</div>

		<div id="container">
			<div id="content">

				<table class=table>
				 	<thead class="thead-dark">
						<tr>
							<th>First Name</th>
							<th>Last Name</th>
							<th>Email</th>
							<th>Balance</th>
						</tr>
					</thead>
					<c:url var="updateLink" value="/user/showFormForUpdate">
							<c:param name="userId" value="${user.id}" />
						</c:url>
						<thead class="thead-light">
						<tr>
							<td>${user.firstName}</td>
							<td>${user.lastName}</td>
							<td>${user.email}</td>
							<td>${user.balance}</td>
						</tr>
						</thead>
				</table>	
						<!-- display the update link -->
						<input type="button" value="Edit"
					onclick="window.location.href='${updateLink}'"
					class="btn btn-dark" />		
					<c:url var="checkLink" value="/user/symbolCheck">
							<c:param name="userId" value="${user.id}" />
					</c:url>
						<input type="button" value="Check to buy/sell stocks"
					onclick="window.location.href='${checkLink}'"
					class="btn btn-dark" />	
							
			</div>
		</div>
	</div>
</body>
</html>