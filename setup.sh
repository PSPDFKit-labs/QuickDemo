#!/usr/bin/env bash

if [[ $1 ]] || [[ ${ANDROID_HOME} ]];
then
    cd ~
    git clone git@github.com:PSPDFKit-labs/QuickDemo.git
    cd QuickDemo/
    if [[ $1 ]]; then echo "sdk.dir="$1 >>local.properties
    fi
    ./gradlew installDebug
    ./gradlew setupDemoMode
    cd ~
    yes | rm -r QuickDemo
else
    echo "Missing ANDROID_HOME"
    echo "Usage: ./quick_demo.sh ANDROID_HOME"
    echo "or set an ANROID_HOME environment variable"
fi
