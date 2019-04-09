<?php
// Reload the page
function reload_page()
{
    print("<script>location.reload()</script>");
}

// Get the databaase
function get_db()
{
    $db = new PDO('sqlite:db/imagevoting.db');
    $db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    return $db;
}

// Check if user already submit image link
function check_uniqueness($db, $username)
{
    $check = $db->prepare("SELECT * FROM image WHERE user=:name");
    $check->bindParam(':name', $username);
    $result = $check->execute();

    if ($result)
    {
     	while ($row = $check->fetch())
        {
            return False;
        }
	return True;
    }

    return False;
}

// Add a user to the database
function add_user($db, $username, $password)
{
    $insert = $db->prepare("INSERT INTO users VALUES(:name, :pass)");
    $insert->bindParam(':name', $username);
    $insert->bindParam(':pass', $password);
    $insert->execute();

    $insert = $db->prepare("INSERT INTO vote VALUES(0, :name)");
    $insert->bindParam(':name', $username);
    $insert->execute();

    $insert = $db->prepare("INSERT INTO votereceived VALUES(0, :name)");
    $insert->bindParam(':name', $username);
    $insert->execute();

    print("<p>Created login for '{$username}'.</p>");
}

// Try and sign a use up to the voting system
function signup($username, $password)
{
    try
    {
     	$db = get_db();
        if (check_uniqueness($db, $username))
        { add_user($db, $username, $password); }
        else
	{ print("<p>Username '{$username}' is already registered.</p>"); }
    }
    catch(PDOException $e)
    {
     	print($e->getMessage());
    }
}

// Create a session token
function create_token($username)
{
    setcookie("username", $username);
    setcookie("session", md5($username));
}

// Check a session token
function check_token($username, $session)
{
    return (md5($username) == $session);
}

// Destroy the session token
function logout()
{
    setcookie("username", "", time()-3600);
    setcookie("session", "", time()-3600);
}

function login($username, $password)
{
    logout();

    try
    {
     	$db = get_db();

        $check = $db->prepare("SELECT * FROM users WHERE username='".$username."' AND password='".$password."'");
        $result = $check->execute();

        while ($row = $check->fetch())
        {
            create_token($row['username']);
            return True;
        }

	return False;
    }
    catch(PDOException $e)
    {
     	print($e->getMessage());
    }
}

// Check if there is a user signed in
function check_signed_in()
{
    if (isset($_COOKIE['username'], $_COOKIE['session']))
    {
        if (check_token($_COOKIE['username'], $_COOKIE['session']))
     	{   return True; }
        else
        {
            logout();
        }
    }
    return False;
}



// Retrieve Image List
function get_image_list($username)
{
    try
    {
        $db = get_db();

        image_submit_form($username);

        $messages = $db->prepare("SELECT * FROM image inner join votereceived on image.user=votereceived.user");
        $result = $messages->execute();

       	while ($row = $messages->fetch())
        {
            print "<blockquote>";
            print "<p>";
     	    print "<img src='".$row['link']."' />";
            print "</p>";
            print "<footer>";
            print "<cite>";
            print $row['user'];
            print "</cite>";
            print "<br/>";
            print "<cite>";
            print "<b>Vote count: ".$row['vote']."</b>";
            print "</cite>";
            print "</footer>";
	    print "<form action='vote.php' method='GET'>";
            print "<input type='hidden' name='vote' value='".$row['user']."' />";
            print "<input type='submit' value='Vote for me' />";
            print "</form>";
            print "</blockquote>";
            print "</blockquote>";
        }
    }
    catch(PDOException $e)
    {
       	print($e->getMessage());
    }
}

// Print the image submit form
function image_submit_form($username)
{
    print '<h6> You have logged in with username: '.$username.'</h6>';
    print '<form action="index.php" method="post">';
    print '<fieldset>';
    print '<div class="col-lg-6">';
    print '  <div class="input-group">';
    print '    <input id="message" class="form-control" name="link" placeholder="Input your image link here..."/>';
    print '    <span class="input-group-btn">';
    print '	 <button id="post" name="post" type="submit" class="btn btn-primary">Submit / Update</button>';
    print '    </span>';
    print '  </div>';
    print '</div>';
    print '</fieldset>';
    print '</form>';

}

// Submit and image link
function submit_image_link($user, $link)
{
    try
    {
     	$db = get_db();

        if (check_uniqueness($db, $user))
        {
            $post = $db->prepare("INSERT INTO image VALUES(:link, :user)");
        }
        else
        {
            $post = $db->prepare("UPDATE image SET link=:link WHERE user=:user");
        }

        $post->bindParam(':link', $link);
        $post->bindParam(':user', $user);

        $post->execute();
    }
    catch(PDOException $e)
    {
     	print($e->getMessage());
    }
}

//Save vote and voter
function vote($from,$to)
{
    try
    {
        $db = get_db();

	//Save vote
        $check = $db->prepare("SELECT * FROM votereceived WHERE user=:name");
        $check->bindParam(':name', $to);
        $result = $check->execute();

        if ($result)
        {
            if ($row = $check->fetch())
            {
                $vote = $row['vote'] + 1;
                $action = $db->prepare("UPDATE votereceived SET vote=:vote WHERE user=:name");
                $action->bindParam(':vote',$vote);
                $action->bindParam(':name',$to);
                $action->execute();
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }

        //Save voter
        $check = $db->prepare("SELECT * FROM vote WHERE user=:name");
        $check->bindParam(':name', $from);
        $result = $check->execute();

        if ($result)
        {
            if ($row = $check->fetch())
            {
                $vote = $row['vote'] + 1;
                $action = $db->prepare("UPDATE vote SET vote=:vote WHERE user=:name");
                $action->bindParam(':vote',$vote);
                $action->bindParam(':name',$from);
                $action->execute();
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
        return true;
    }
    catch(PDOException $e)
    {
        print($e->getMessage());
        return false;
    }
}

?>
