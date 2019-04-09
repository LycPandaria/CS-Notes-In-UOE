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
            if (!isset($_POST['vote']) || check_uniqueness(get_db(),$_POST['vote']))
            {
             	print ("<p>Invalid vote!!</p>");
            }
            elseif ($_COOKIE['username'] == $_POST['vote'])
            {
                print ("<p>You cannot vote for yourself!!</p>");
            }
            else
            {
                if(vote($_COOKIE['username'],$_POST['vote']))
                {
                    header("Location:/");
                }
                else
                {
                    print ("<p>Invalid vote!!</p>");
                }
            }
        }
	else
	{
            header("Location:/");
        }
      ?>
    </div>

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="js/bootstrap.min.js"></script>
  </body>
</html>
