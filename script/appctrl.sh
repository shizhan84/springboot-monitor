#!/bin/sh

FINDNAME=$0
echo $FINDNAME
while [ -h $FINDNAME ] ; do FINDNAME=`ls -ld $FINDNAME | awk '{print $NF}'` ; done
SERVER_HOME=`echo $FINDNAME | sed -e 's@/[^/]*$@@'`
unset FINDNAME

if [ "$SERVER_HOME" = '.' ]; then
   SERVER_HOME=$(echo `pwd` | sed 's/\/bin//')
else
   SERVER_HOME=$(echo $SERVER_HOME | sed 's/\/bin//')
fi

if [ ! -d $SERVER_HOME/pids ]; then
    mkdir $SERVER_HOME/pids
fi

HEAP_MEMORY=256m
PERM_MEMORY=128m
SERVER_NAME=springboot-monitor
JMX_PORT=0
LOCAL_IP=`ifconfig eth0 | grep "inet addr" | awk '{print $2}' | awk -F: '{print $2}'`

PIDFILE=$SERVER_HOME/pids/$SERVER_NAME.pid

case $1 in
start)
    echo  "Starting $SERVER_NAME ... "

    JAVA_OPTS="-server -XX:+HeapDumpOnOutOfMemoryError"
    JAVA_OPTS="${JAVA_OPTS} -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
    JAVA_OPTS="${JAVA_OPTS} -Djava.rmi.server.hostname=${LOCAL_IP} -Dcom.sun.management.jmxremote.port=${JMX_PORT} -Dcom.sun.management.jmxremote.rmi.port=${JMX_PORT}"

    shift
    ARGS=($*)
    for ((i=0; i<${#ARGS[@]}; i++)); do
        case "${ARGS[$i]}" in
        -D*)    JAVA_OPTS="${JAVA_OPTS} ${ARGS[$i]}" ;;
        -Heap*) HEAP_MEMORY="${ARGS[$i+1]}" ;;
        -Perm*) PERM_MEMORY="${ARGS[$i+1]}" ;;
        esac
    done

    JAVA_OPTS="${JAVA_OPTS} -Xms${HEAP_MEMORY} -Xmx${HEAP_MEMORY} "
    JAVA_OPTS="${JAVA_OPTS} -XX:+AlwaysPreTouch"
    JAVA_OPTS="${JAVA_OPTS} -Duser.dir=${SERVER_HOME} -Dapp.name=${SERVER_NAME} -Dlogging.config=${SERVER_HOME}/bin/logback-spring.xml "
    echo "start jvm args ${JAVA_OPTS}"
    JAR_NAME=${ARGS[$#-1]}
    nohup java $JAVA_OPTS -jar ${SERVER_HOME}/bin/${JAR_NAME} > nohup &
    echo $! > $PIDFILE
    echo STARTED
    ;;

stop)
    echo "Stopping $SERVER_NAME ... "
    if [ ! -f $PIDFILE ]
    then
        echo "error: could not find file $PIDFILE"
        exit 1
    else
        kill -9 $(cat $PIDFILE)
        rm $PIDFILE
        echo STOPPED
    fi
    ;;

restart)
    ./appctrl.sh stop
    sleep 1
    ./appctrl.sh start $0
    ;;

esac

exit 0
