#!/bin/bash
java -jar -Dspring.profiles.active=local,h2 build/libs/Intelligent-CCTV_backend-0.1.1.jar
