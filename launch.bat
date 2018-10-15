@echo off
set /A round = 1

:startLoop
FOR /F "tokens=1,2" %%i in (snake_config.txt) DO IF %%i == duration set /A seconds = %%j + 10

:startGame
echo Round %round% will last %seconds% seconds
start "Round %round%" java -jar lib/Snake2018-v0.jar -java out/artifacts/DaisyAgent/myAgent.jar
ping 127.0.0.1 -n %seconds% -w 1000 > nul
taskkill /F /FI "IMAGENAME eq Java.exe" /T
set /A round += 1
GOTO startLoop