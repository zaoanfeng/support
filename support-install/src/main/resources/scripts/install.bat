cd /d %~dp0

"vcredist_x64.exe" /q /norestart
IF "%ERRORLEVEL%" == "0" (
  echo Microsoft Visual C++ 2013 Redistributable installed!
)

start ../jdk1.8.0_191\jre\bin\java -jar support-install-1.0.0.jar install
pause