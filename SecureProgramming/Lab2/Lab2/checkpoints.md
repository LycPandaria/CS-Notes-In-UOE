# Question 1: An Authentication Program

**Checkpoint 0.** 

insert into db_users values ('hi','c22b5f9178342609428d6f51b2c5af4c0bde6a42');
./Login hi hi

./Login "alice\x27 OR 1 --" "hi"

**Checkpoint 1.** 

H S SQL: Login.doPrivilegedAction(String, String) passes a nonconstant String to an execute method on an SQL statement  At Login.java:[line 69]
M B ODR: Login.doPrivilegedAction(String, String) may fail to close Statement  At Login.java:[line 67]
M X OBL: Login.doPrivilegedAction(String, String) may fail to clean up java.sql.Statement  Obligation to clean up resource created at Login.java:[line 67] is not discharged
M X OBL: Login.doPrivilegedAction(String, String) may fail to clean up java.sql.ResultSet  Obligation to clean up resource created at Login.java:[line 69] is not discharged


**Checkpoint 2.**

The query string is vulnerable to SQL injection. Attack can use crafted string to execute SQL command to login in or change data in database.

**Checkpoint 3.** 

We can use unicode to avoid the sed, for example, we can use \x27 to indicate ' and execute SQL injection.

**Checkpoint 4.** Give your patch to repair the `Login` and `Login.java` programs and explain how it works.

59,66c59,64
< 	    String sqlString =
<                 "SELECT *"                        +
<                 "  FROM db_users"                 +
<                 " WHERE username='"+username+"'"  +
<                 "   AND password='"+hPassword+"'" ;
<             Statement statement = connection.createStatement();
<             statement.setQueryTimeout(5);
<             ResultSet rs = statement.executeQuery(sqlString);
---
> 	    
>             
> 	    PreparedStatement stmt = connection.prepareStatement("SELECT * FROM db_users WHERE username=? AND password=?");
> 	    stmt.setString(1, username);
> 	    stmt.setString(2, hPassword);
>             ResultSet rs = stmt.executeQuery();
95d92
< 

When we use PreparedStatement to query the database, database will compile the prepared query statement first, then execute the query with parameters. So, the content of parameters will not be regarded as a part of SQL instruction.


**Checkpoint 5.** 

The exploit1.sh uses \x27 to replace ' and \x3b to replace ';'. So the program executes "select * from db_users where username='david'", and results to true.

The exploit2.sh make the 'sed' function disabled so that we can input ' and ';' directly.
 

**Checkpoint 6.** 
Yes!

# Question 2: Another SQL injection

**Checkpoint 0.** 
admin, user

I guess attack maybe able to login in as a user and change the password of admin.

**Checkpoint 1.** When you run the `findbugs` program on the class file
  it shows there is an SQL injection problem despite the use of
  a prepared statement.  Why?

because the programe put a variable "this.username" into the prepared statement. This may give a chance to attack.

**Checkpoint 2.** Fix the code and provide a patch for `Login.java`. 
In the changePassword() function: using constant string to build prepared statement.

 PreparedStatement statement =
                    connection.prepareStatement(
                            "UPDATE db_users" +
                                    "   SET password=?"+
                                    " WHERE username=?");
            statement.setString(1, hPassword);
            statement.setString(2, this.username);

**Checkpoint 3.** Describe how the exploit works.
Attack add a user named admin';-- with a password.
So that attacker then login in with this account.
But when attcker change the password, due to the fact that program uses a nonconstant string to build prepared statement, the query will become: update db_users set password='wharever' where username='admin';--;. The content of username becomes a part of the query, so the query will update admin`s password, not the password of "admin';--" 

**Checkpoint 4.** Verify your patch stops the exploit.  If not, make
a new patch that does.




