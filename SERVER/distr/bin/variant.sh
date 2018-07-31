#!/bin/bash

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
   ps -ef | grep java | grep 'variant.variant-0.9.2' | awk '{print $2}' | xargs kill -2

   ;;
status)
   if [ -f RUNNING_PID ] ; then 
      echo "Running PID $(cat RUNNING_PID)" 
   else
      echo "Not running"
   fi  
   ;;
*)
        echo "Usage: variant.sh {start|stop|status}"
        exit 1
esac
