<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
<meta http-equiv="cache-control" content="no-cache" />
<meta http-equiv="pragma" content="no-cache" />
<meta name="description" content="">
<meta name="author" content="">
<link rel="icon" href="/images/headers/medipi_logo.png">
<!--css media queries for ie8 / ie 7-->
<!--[if lt IE 9]>
		<script src="plugins/respond.min.js"></script>
		<![endif]-->

<title>MediPi Clinician</title>
<!-- <link rel="stylesheet" type="text/css" href="/css/main.css"> -->
<!-- Bootstrap core CSS -->
<link rel="stylesheet" type="text/css" href="/plugins/bootstrap-3.3.5/dist/css/bootstrap.min.css">
<!-- Bootstrap theme -->
<link rel="stylesheet" type="text/css" href="/plugins/bootstrap-3.3.5/dist/css/bootstrap-theme.min.css">
<!-- Custom styles for this template -->
<link rel="stylesheet" type="text/css" href="/plugins/bootstrap-3.3.5/docs/examples/theme/theme.css">
<link rel="stylesheet" type="text/css" href="/css/main.css">

<script type="text/javascript" charset="utf8" src="/plugins/dataTables-1.10.9/media/js/jquery.js"></script>
<script type="text/javascript" charset="utf8" src="/plugins/bootstrap-3.3.5/dist/js/bootstrap.min.js"></script>
<script type="text/javascript" charset="utf8" src="/plugins/bootstrap-3.3.5/docs/assets/js/ie-emulation-modes-warning.js"></script>
<script type="text/javascript" charset="utf8" src="/js/misc/header.js"></script>
<script type="text/javascript" charset="utf8" src="/js/common/common.js"></script>

<!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
		  <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
		  <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
		<![endif]-->
</head>
<body role="document">
	<!-- Fixed navbar -->
	<nav class="navbar navbar-inverse navbar-fixed-top">
		<div class="container">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
					<span class="sr-only">Toggle navigation</span> <span class="icon-bar"></span> <span class="icon-bar"></span> <span class="icon-bar"></span>
				</button>
				<a class="navbar-brand" style="padding-top: 0px;" href="/clinician/"> <img src="/images/headers/medipi_logo.png" height="70"></a>

			</div>
			<div id="navbar" class="navbar-collapse collapse">
				<ul class="nav navbar-nav">
					<li id="menu_home"><a href="/clinician/">Home</a></li>
					<!-- <li><a href="blog.html">Blog</a></li> -->
					<li id="menu_manage" class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Manage <span class="caret"></span></a>
						<ul class="dropdown-menu">
							<li><a href="/clinician/patient/patients" id="menu_patients">Patients</a></li>
						</ul>
					</li>
					<li id="menu_about"><a href="/clinician/about">About</a></li>
				</ul>
			</div>
			<!--/.nav-collapse -->
		</div>
	</nav>
	<div class="container" role="main">
		<div class="alert alert-danger hidden" role="alert" id="errorMessageDiv">
			<strong>Error!&nbsp;</strong><span id="errorMessage"></span>
		</div>
		<div class="alert alert-success hidden" role="alert" id="successMessageDiv">
			<strong>Success!&nbsp;</strong><span id="successMessage">Your employee has been saved with id: and number: .</span>
		</div>