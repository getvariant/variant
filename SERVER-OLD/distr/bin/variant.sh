#!/bin/bash

running_pid () {
  ps -ef | grep java | grep 'variant.variant-<version>' | awk '{print $2}'
}

# Class loader expects variant root to be the current dir.
cd $(dirname $0)/..

case "$1" in
start)
   shift
   # If caller passed http.port, it will override the default.
   bin/playapp -Dhttp.port=5377 $@
   ;;

stop)
   # Sending the process the interrupt signal appears to be the prescribed way to shutdown the server.
   pid=$(running_pid)
   if [ -n "$pid" ]; then
      kill -2 $pid
   fi

   ;;
status)
   pid=$(running_pid)
   if [ -n "$pid" ] ; then 
      echo "Running (PID $pid)" 
   else
      echo "Not running"
   fi  
   ;;
*)
        echo "Usage: variant.sh {start|stop|status}"
        exit 1
esac
