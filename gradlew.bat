@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Gradle wrapper script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
@if "%OS%" == "Windows_NT" @setlocal

@rem Add default JVM options here. You may also use JAVA_OPTS and GRADLE_OPTS.
set DEFAULT_JVM_OPTS="-Xmx1024m -Dfile.encoding=UTF-8"

set DIR=%~dp0
if "%DIR%" == "" (
    set DIR=.\
) else (
    set DIR=%DIR:"=%
)

set APP_HOME=%DIR%

@rem Find java.exe
if defined JAVA_HOME (
    set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
) else (
    set JAVA_EXE=java.exe
)

if not exist %JAVA_EXE% (
    echo Error: JAVA_HOME is not defined correctly.
    echo  We cannot execute %JAVA_EXE%
    goto fail
)

@rem Execute Gradle.
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %* 

:fail
@if "%OS%" == "Windows_NT" @endlocal


