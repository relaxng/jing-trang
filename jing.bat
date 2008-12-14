@echo off
setlocal
set MAIN_CLASS=com.thaiopensource.relaxng.util.Driver
set JAR_FILE=jing.jar
set JRE_KEY=HKLM\SOFTWARE\JavaSoft\Java Runtime Environment
if not exist "%JAVA_HOME%\bin\java.exe" (
  for /f "tokens=2*" %%u in ('reg query "%JRE_KEY%" /v CurrentVersion') do for /f "tokens=2*" %%i in ('reg query "%JRE_KEY%\%%v" /v JavaHome') do set JAVA_HOME=%%j
) 2>nul
if exist "%JAVA_HOME%\bin\java.exe" goto found_java
echo Could not find a Java Runtime Environment. Download one from <http://java.sun.com/javase/downloads/>.
exit /b 1
:found_java
set JAR_DIR=%~dp0
if exist "%JAR_DIR%%JAR_FILE%" goto found_jar
echo Could not find %JAR_FILE%. Must be in the same directory as %~nx0 (%JAR_DIR%).
exit /b 1
:found_jar
"%JAVA_HOME%\bin\java.exe" -classpath "%JAR_DIR%%JAR_FILE%" %MAIN_CLASS% %*
