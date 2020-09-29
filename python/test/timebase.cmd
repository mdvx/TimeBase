set DELTIX_HOME=..\..
setlocal
set START="%DELTIX_HOME%\jre\bin\java"
call "%DELTIX_HOME%\bin\clntenv.cfg.cmd"
rmdir %DELTIX_HOME%\temp\python /S /Q
mkdir %DELTIX_HOME%\temp\python
%START% %JAVA_OPTS% -jar "%DELTIX_HOME%\bin\runjava.jar" deltix.qsrv.comm.cat.TomcatCmd -tb -home "%DELTIX_HOME%\temp\python" %*
endlocal