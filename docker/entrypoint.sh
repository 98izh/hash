#!/bin/bash
java --version
cd /home/proxidize/app/
echo "Starting app hash"
FNAME=$(ls -1 /home/proxidize/app/hash.jar)

java $JAVAOPS \
     -javaagent:/home/proxidize/jmx_prometheus_javaagent.jar=9090:/home/proxidize/config.yaml \
     -jar $FNAME