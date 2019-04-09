<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Image Voting System</title>

    <!-- Bootstrap -->
    <link href="css/bootstrap.min.css" rel="stylesheet">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
      <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
  </head>
  <body>
    <?php
      ini_set('display_errors', 'On');
      include 'include/functions.php';
    ?>
    <div class="jumbotron">
        <h1>Image Voting System</h1>
        <?php
          if (check_signed_in())
          { print('<a href="logout.php" class="btn btn-inverse btn-large">Log out</a>'); }
        ?>
    </div>

    <div class="container">
      <?php
	if (check_signed_in())
        {
            if (isset($_POST['post']))
            {
             	$whose = $_COOKIE['username'];  // get username from cookie
                $link = $_POST['link'];

                submit_image_link($whose, $link);
            }
            get_image_list($_COOKIE['username']);
        }
	elseif (isset($_POST['username'], $_POST['password']))
        {
          $username = $_POST['username'];
          $password = $_POST['password'];

	  if (isset($_POST['signup']))
	  {
	    signup($username, $password);
          }
          elseif (isset($_POST['login']))
          {
            logout();
            $success = login($username, $password);
            if ($success)
            {
              reload_page();
            }
            else
            {
              print("<p>Password didn't match</p>");
            }
          }
	}
	else
	{
    // login page
          print '<form name="login" class="form-horizontal" action="index.php" method="post">';
          print '  <fieldset>';
          print '    <legend>Login or sign up</legend>';
          print '    <div class="control-group">';
          print '      <label class="control-label" for="username">User name</label>';
          print '      <div class="controls">';
          print '        <input id="username" name="username" type="text" placeholder="username" class="input-medium" required="">';
          print '      </div>';
          print '    </div>';
          print '    <div class="control-group">';
          print '      <label class="control-label" for="password">Password</label>';
          print '      <div class="controls">';
          print '        <input id="password" name="password" type="password" placeholder="password" class="input-medium" required="">';
          print '      </div>';
          print '    </div>';
          print '    <div class="control-group">';
          print '      <label class="control-label" for="createaccount"></label>';
          print '      <div class="controls">';
	  print '        <button id="signup" name="signup" type="submit" value="signup" class="btn btn-default">Sign up</button>';
          print '        <button id="login" name="login" type="submit" value="login" class="btn btn-default">Log in</button>';
          print '      </div>';
          print '    </div>';
          print '  </fieldset>';
          print '</form>';
        }
      ?>
    </div>

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="js/bootstrap.min.js"></script>
  </body>
</html>
