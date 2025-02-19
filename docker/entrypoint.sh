#!/bin/bash
java --version
cd /home/proxidize/app/
echo "Starting app hash"
FNAME=$(ls -1 /home/proxidize/app/hash.jar)
ENVIRONMENT=assignment


java $JAVAOPS \
     -javaagent:/home/proxidize/jmx_prometheus_javaagent.jar=9090:/home/proxidize/config.yaml \
     -Dspring.profiles.active=$ENVIRONMENT \
     -jar $FNAME