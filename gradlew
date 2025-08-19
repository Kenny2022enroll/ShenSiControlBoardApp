#!/usr/bin/env sh

##############################################################################
##
##  Gradle wrapper script for UNIX
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done

APP_HOME=`dirname "$PRG"`

# Add default JVM options here. You may also use JAVA_OPTS and GRADLE_OPTS.
# The first element of the array is the executable
if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD=java
fi

if [ -x "$JAVACMD" ]; then
  exec "$JAVACMD" ${GRADLE_OPTS} -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
else
  echo "JAVA_HOME is not defined correctly." >&2
  echo "We cannot execute \$JAVACMD" >&2
  exit 1
fi

# OS specific support (must be 'true' or 'false')
cygwin=false
darwin=false
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything else
if $cygwin ; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# Set the default locale. This allows the application to
# be portable on different locales.
LANG=en_US.UTF-8
export LANG

# For Darwin, ensure that the umask is set to 022
if $darwin ; then
  umask 022
fi

# Set GRADLE_OPTS to the default value if it's not set.
if [ -z "$GRADLE_OPTS" ]; then
  GRADLE_OPTS="-Xmx128m -Dfile.encoding=UTF-8"
fi

# Execute Gradle