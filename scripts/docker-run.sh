#!/bin/bash

if docker-compose up -d; then
    docker-compose ps
fi

