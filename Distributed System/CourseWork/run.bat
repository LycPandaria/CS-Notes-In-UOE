@echo off
cd ./TopWikiV1/
set /p line=
if "%line%"== "compile" (
	call mvn compile
) ELSE (
	call mvn exec:java -Dexec.mainClass="utils.NodeSetup" -Dexec.args="%line%"
)

pause