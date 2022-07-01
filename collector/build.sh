#!/bin/bash
container_name="collector"
image_name="collector-java"


# Check if container exists & is running
if docker ps | grep -q $container_name; then
  docker stop $container_name && docker rm $container_name && docker rmi $image_name && mvn package && docker build -t $image_name . && docker run --name $container_name --network=host $image_name

# Check if container exists and is not running
elif docker ps -a | grep -q $container_name; then
  docker rm $container_name && docker rmi $image_name && mvn package && docker build -t $image_name . && docker run --name $container_name --network=host $image_name

else
  docker build -t $image_name . && docker run --name $container_name --network=host $image_name
fi
