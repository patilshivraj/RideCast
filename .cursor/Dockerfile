FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        git \
        unzip \
        wget \
        openjdk-17-jdk \
        libc6-dev-i386 \
        lib32z1 \
        libpulse0 \
        libnss3 \
        libxcomposite1 \
        libxcursor1 \
        libxi6 \
        libxtst6 \
        libasound2t64 \
        libx11-xcb1 \
        libxdamage1 \
        libxrandr2 \
        libgbm1 \
        libtcmalloc-minimal4t64 \
    && rm -rf /var/lib/apt/lists/*

# Emulator needs read access to KVM in Cloud Agent VMs.
RUN chmod 666 /dev/kvm 2>/dev/null || true

RUN useradd -m -s /bin/bash -u 1000 ubuntu 2>/dev/null || true

ENV ANDROID_HOME=/opt/android-sdk
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/emulator:${PATH}"

RUN mkdir -p "${ANDROID_HOME}" && chown -R ubuntu:ubuntu "${ANDROID_HOME}"

USER ubuntu
