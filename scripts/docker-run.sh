#!/bin/bash

if docker-compose up -d --build; then
    docker-compose ps
fi

