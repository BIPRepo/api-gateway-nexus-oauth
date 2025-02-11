#!/bin/env bash

echo "clean target"
sudo rm -rf ./target
echo "building..."
docker run -it --rm --name mvn-build-image -v "$(pwd)":/usr/src/dgsia -w /usr/src/dgsia maven:3.9.5 mvn clean install -DskipTests