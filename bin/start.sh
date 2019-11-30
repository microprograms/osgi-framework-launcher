#!/bin/bash

rm -rf felix-cache
nohup java -cp "conf:conf/*:lib:lib/*" com.github.microprograms.osgi_framework_launcher.OsgiFrameworkLauncher > server.nohup.out 2>&1 &
echo $! > bin/server.pid
