# Question 1: An Authentication Program

scp -P 2222 LoginV1.java user@localhost:/home/user/SQLi/


**Checkpoint 0.** Give the login command needed to login as one of the
users in the database.

insert into db_users values ('hi','c22b5f9178342609428d6f51b2c5af4c0bde6a42');
./Login hi hi

./Login "alice\x27 OR 1 --" "hi"

**Checkpoint 1.** What does the output of `findbugs` tell you?

H S SQL: Login.doPrivilegedAction(String, String) passes a nonconstant String to an execute method on an SQL statement  At Login.java:[line 69]
M B ODR: Login.doPrivilegedAction(String, String) may fail to close Statement  At Login.java:[line 67]
M X OBL: Login.doPrivilegedAction(String, String) may fail to clean up java.sql.Statement  Obligation to clean up resource created at Login.java:[line 67] is not discharged
M X OBL: Login.doPrivilegedAction(String, String) may fail to clean up java.sql.ResultSet  Obligation to clean up resource created at Login.java:[line 69] is not discharged


**Checkpoint 2.** Which part of the authentication program is
vulnerable to SQL injection and how can an attacker exploit it?

The query string is vulnerable to SQL injection. Attack can use crafted string to execute SQL command to login in or change data in database.

**Checkpoint 3.** Why isn't the removal of quotes and semicolons
  through `sed` adequate to protect against SQL injection?

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

As we use PreparedStatement to perform the query, database will compile the query first, then insert parameter to execute the query.
So, the database will not regard the content of parameter as a part of SQL instruction.

**Checkpoint 5.** Examine the exploit and explain how they work. What design flaws or vulnerabilities are abused by the exploit?

**Checkpoint 6.** Did your patch stop the exploits?  If not explain why and  provide an updated patch that does.

# Question 2: Another SQL injection

**Checkpoint 0.** What are the users already in the database and what
  attacks against them can you imagine?

**Checkpoint 1.** When you run the `findbugs` program on the class file
  it shows there is an SQL injection problem despite the use of
  a prepared statement.  Why?

**Checkpoint 2.** Fix the code and provide a patch for `Login.java`. 

**Checkpoint 3.** Describe how the exploit works.

**Checkpoint 4.** Verify your patch stops the exploit.  If not, make
a new patch that does.

# Question 3: Linkers

**Checkpoint 1.** What program is used to interpret the ELF file `Vulnerable`?

**Checkpoint 2.** What is an untrusted search path? How it related to dynamically linking of library?

**Checkpoint 3.** How could the `Vulnerable` program be fixed to avoid the search path exploit?

**Checkpoint 4.** Describe what each of the provided exploits does,
  how likely you consider it to be an achievable exploit and under
  what circumstances.

**Checkpoint 5.** How could you prevent each of the attacks?

**Checkpoint 6. (optional)** Modify `exploit-ld.sh` so it attacks the `SHA1` call.
