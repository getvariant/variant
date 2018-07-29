#!/bin/bash

# Class loader expects variant root to be the current dir.
cd $(dirname $0)/..

case "$1" in
start)
   bin/playapp -Dhttp.port=5377
   ;;

stop)
   if [ -f RUNNING_PID ] ; then
     kill $(cat RUNNING_PID) > /dev/null 2>&1
     rm -f RUNNING_PID
     echo "Stopped"
   else
     $0 status
   fi
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
