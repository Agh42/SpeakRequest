#!/bin/bash

docker run -d --name speakrequest --restart unless-stopped -p 8080:8080 agh42/speakrequest:latest
