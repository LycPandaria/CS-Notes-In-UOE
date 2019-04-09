% Secure Programming Lab 1: Data Corruption
% School of Informatics, University of Edinburgh
% 3pm-6pm, 7th February 2017



Exercise 1
==========

## Part 1: classic stack overflow

**Checkpoint 1.** 
-rwsr-sr-x 1 root root, because of the setuid bit, we can execute it as root, since the owner of noticeborad is root.

**Checkpoint 2.** 
results by checksec.sh shows the technologies that this program use, but this program does not ues RELRO(Relocation Read Only), no stack canary, no PIE so on.
results bt RATS shows the problem of code, fixed size local buffer and the call of strcpy may be expoilted by a buffer overflow attack.
-fno-stack-protector means disable stack canary.
-z execstack means an executable stack

**Checkpoint 3.** 
My shellcode will spwan a shell. It use execve() function and take "/bin/sh" as an augrment.
I found the shellcode from internet.

**Checkpoint 4.** 
My input string consists from 3 part, first part is 111 "NOP", which is used to help me easier to redirect. 
Second part is shellcode. Last part is a address point to one of the "NOP".
So basicly, we overwrite the address to return address(usually at $ebp+4), then when funtion return, program will redirect to one of the NOP operation.
Then NOP go to next NOP, utill it reaches the shellcode and execute, finally spawn a shell.

**Checkpoint 5.** 
Do the boundary check

int len = strlen(arg[1]);
if(len > 0 && len < 140)
	strcpy(buffer, arg[1]);
else
{
	printf("invalid input.");
	return 0;
}

## Part 2: another vulnerability

**Checkpoint 1.** 
Through the Makefile, I guess this time program enable stack protector and also non-executable stack.
The problem is the varible "NOTICEABOARD", attacker can overwrite buffer and change the contant of NOTICEBOARD. And input some malicious code to 
import file such as /home/user/.profile.


**Checkpoint 2 (optional).** 
Sorry, I look the answer and try to understand it.
In my understanding, the first part "echo 'hello'" is the malicious code, we can change it to "rm /f" some other more dangerous
bash command, and we found the address of 'NOTICEABOARD' and overwrites it with "/home/user/.profile".
So, next time user login in, system will run this file whenever user logins in. So the "echo 'helllo'" is executed.


**Checkpoint 3.** 
Use: const char[] NOTICEABOARD = "/tmp/noticeboard.txt";
 



Exercise 2
==========

**Checkpoint 1.** 
message are conbined by two parts, first line is the length of string, rest is the message itself



**Checkpoint 2.** 
java Server 1234 &
sleep2
nc localhost 1234 <<< $'-1\nhello'


**Checkpoint 3.** 
check the length at the Server side too.
Use: if(len > 0 && len < this.buffersize)




Exercise 3 (Advanced)
=====================

**Checkpoint 1.** 
We can use 'find &system,+9999999,"/bin/sh"' in gdb to found it, if it does not exist, we can add to the environment variable 


**Checkpoint 2.** 
In Standard C library ? not sure

system calls are loaded in the kernel.


**Checkpoint 3.** 

Overwrite 'system'`s address to EBP

  
**Checkpoint 4.** 

We can overwrite the address of "exit()" to EIP, so that once the shell exits
the process will jump to EIP, which, to save having a log entry should call
exit() and cleanly terminate. 


Exercise 4 (Optional)
=====================

**Checkpoint 1.** Identify the security flaw in the code, and provide
  the relevant CVE number.

**Checkpoint 2.** Briefly summarise the problem and explain and why
  it is a security flaw.

**Checkpoint 3.** Give a recommendation for a way to repair the problem.

**Checkpoint 4 (very optional).** Build a *proof-of-concept* to
  demonstrate the security flaw and explain how it might be exploited;
  check that your repair (or the current released version) prevents
  your attack.

