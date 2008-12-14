@echo off
setlocal
set JRE_KEY=HKLM\SOFTWARE\JavaSoft\Java Runtime Environment
if not exist "%JAVA_HOME%\bin\java.exe" (
  for /f "tokens=2* delims=	 " %%u in ('reg query "%JRE_KEY%" /v CurrentVersion') do for /f "tokens=2* delims=	 " %%i in ('reg query "%JRE_KEY%\%%v" /v JavaHome') do set JAVA_HOME=%%j
) 2>nul
if exist "%JAVA_HOME%\bin\java.exe" goto found_java
echo Could not find a Java Runtime Environment. Download one from <http://java.sun.com/javase/downloads/>.
exit /b 1
:found_java
set JAR_DIR=%~dp0
if exist "%JAR_DIR%jing.jar" goto found_jing
echo Could not find jing.jar. Must be in the same directory as jing.bat (%JAR_DIR%).
exit /b 1
:found_jing
"%JAVA_HOME%\bin\java.exe" -classpath "%JAR_DIR%jing.jar" com.thaiopensource.relaxng.util.Driver %*
