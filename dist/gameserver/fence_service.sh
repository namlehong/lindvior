#!/bin/bash
# myapp daemon
# chkconfig: 345 20 80
# description: myapp daemon
# processname: myapp

SCRIPT_DIR=$(readlink -f $(dirname ${BASH_SOURCE[0]}))
DAEMON_PATH="/opt/fence_new"

DATA_PATH="/opt/kain_dp"

DAEMON=./GameServer_loop.sh
DAEMONOPTS="-my opts"

NAME=fence
DESC="My l2gs daemon manage"
PIDFILE=$DAEMON_PATH/l2gs.pid
SCRIPTNAME=/etc/init.d/$NAME

case "$1" in
start)
	printf "%-50s" "Starting $NAME..."
	cd $DAEMON_PATH
	PID=`$DAEMON $DAEMONOPTS > /dev/null 2>&1`
;;
status)
        printf "%-50s" "Checking $NAME..."
        if [ -f $PIDFILE ]; then
            PID=`cat $PIDFILE`
            if [ -z "`ps axf | grep ${PID} | grep -v grep`" ]; then
                printf "%s\n" "Process dead but pidfile exists"
            else
                echo "Running"
            fi
        else
            printf "%s\n" "Service not running"
        fi
;;
stop)
        printf "%-50s" "Stopping $NAME"
            PID=`cat $PIDFILE`
            cd $DAEMON_PATH
        if [ -f $PIDFILE ]; then
            kill -HUP $PID
            printf "%s\n" "Ok"
            rm -f $PIDFILE
        else
            printf "%s\n" "pidfile not found"
        fi
;;

restart)
  	$0 stop
  	$0 start
;;

update)
	echo "update main folder"
	svn up $DAEMON_PATH
	echo "update datapack"
	svn up $DATA_PATH
#	echo "svn up $DAEMON_PATH"
;;

watch)
	tail -f $DAEMON_PATH/log/stdout.log
;;


*)
        echo "Usage: $0 {status|start|stop|restart|update|watch}"
        exit 1
esac