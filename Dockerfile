FROM openjdk:20-slim

# Install dependencies
RUN apt-get update && apt-get install -y \
    wget libx11-6 libxtst6 libxrender1 libxt6 \
    x11vnc xvfb fluxbox python3-pip git \
    python3-numpy procps make \
    --no-install-recommends && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Install Scala and Scala Swing
RUN wget https://downloads.lightbend.com/scala/2.13.12/scala-2.13.12.tgz && \
    tar xzf scala-2.13.12.tgz && \
    mv scala-2.13.12 /usr/share/scala && \
    ln -s /usr/share/scala/bin/* /usr/bin/ && \
    rm scala-2.13.12.tgz && \
    wget -qO /usr/share/scala-swing.jar https://repo1.maven.org/maven2/org/scala-lang/modules/scala-swing_2.13/3.0.0/scala-swing_2.13-3.0.0.jar

# Install noVNC
RUN git clone https://github.com/novnc/noVNC.git /opt/noVNC && \
    cd /opt/noVNC && \
    git checkout v1.4.0 && \
    ln -s vnc.html index.html && \
    cd utils && \
    git clone https://github.com/novnc/websockify websockify

# Create a script to start the VNC server
RUN echo '#!/bin/bash' > /start-vnc.sh && \
    echo 'export DISPLAY=:0' >> /start-vnc.sh && \
    echo 'Xvfb :0 -screen 0 1920x720x16 &' >> /start-vnc.sh && \
    echo 'sleep 1' >> /start-vnc.sh && \
    echo 'fluxbox &' >> /start-vnc.sh && \
    echo 'x11vnc -display :0 -forever -nopw -xdamage &' >> /start-vnc.sh && \
    echo 'cd /opt/noVNC' >> /start-vnc.sh && \
    echo './utils/novnc_proxy --vnc localhost:5900 --listen 8080 &' >> /start-vnc.sh && \
    echo 'echo "GUI enabled. Access http://localhost:8080/vnc.html"' >> /start-vnc.sh && \
    chmod +x /start-vnc.sh

WORKDIR /app

# Stablish the CLASSPATH
ENV CLASSPATH=/app:/usr/share/scala-swing.jar