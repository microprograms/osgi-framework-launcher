#!/bin/bash

mvn clean package
rm -rf lib/
mvn dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=lib
cp target/*.jar lib/
