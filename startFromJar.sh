#!/bin/bash
java -jar -Dspring.profiles.active=local,postgres build/libs/camera-backend-0.0.1-SNAPSHOT.jar
