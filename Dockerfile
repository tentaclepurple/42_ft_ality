# Usar OpenJDK como imagen base
FROM openjdk:20-slim

# Instalar dependencias necesarias, incluyendo git y procps
RUN apt-get update && apt-get install -y \
    wget libx11-6 libxtst6 libxrender1 libxt6 \
    x11vnc xvfb fluxbox python3-pip git \
    python3-numpy procps \
    --no-install-recommends && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Descargar Scala 2.13 desde el sitio oficial
RUN wget https://downloads.lightbend.com/scala/2.13.12/scala-2.13.12.tgz && \
    tar xzf scala-2.13.12.tgz && \
    mv scala-2.13.12 /usr/share/scala && \
    ln -s /usr/share/scala/bin/* /usr/bin/ && \
    rm scala-2.13.12.tgz

# Descargar Scala Swing compatible con Scala 2.13.x
RUN wget -qO /usr/share/scala-swing.jar https://repo1.maven.org/maven2/org/scala-lang/modules/scala-swing_2.13/3.0.0/scala-swing_2.13-3.0.0.jar

# Descargar e instalar noVNC
RUN git clone https://github.com/novnc/noVNC.git /opt/noVNC && \
    cd /opt/noVNC && \
    git checkout v1.4.0 && \
    ln -s vnc.html index.html && \
    cd utils && \
    git clone https://github.com/novnc/websockify websockify

# Configurar fluxbox
RUN mkdir -p /root/.fluxbox && \
    echo 'session.screen0.toolbar.visible:        false' >> /root/.fluxbox/init && \
    echo 'session.screen0.toolbar.autoHide:       true' >> /root/.fluxbox/init && \
    echo 'session.screen0.toolbar.placement:      TopCenter' >> /root/.fluxbox/init && \
    echo 'session.screen0.toolbar.layer:          Desktop' >> /root/.fluxbox/init

# Crear directorio de trabajo y establecer el CLASSPATH
WORKDIR /app
ENV CLASSPATH=/app:/usr/share/scala-swing.jar

# Copiar el archivo de código fuente Scala
COPY Main.scala /app/

# Compilar el programa Scala con la librería Swing
RUN scalac Main.scala

# Configurar entorno gráfico virtual
ENV DISPLAY=:0

# Crear script de inicio
RUN echo '#!/bin/bash' > /start.sh && \
    echo 'export DISPLAY=:0' >> /start.sh && \
    echo 'export CLASSPATH=/app:/usr/share/scala-swing.jar' >> /start.sh && \
    echo 'Xvfb :0 -screen 0 1024x768x16 &' >> /start.sh && \
    echo 'sleep 1' >> /start.sh && \
    echo 'fluxbox 2>/dev/null &' >> /start.sh && \
    echo 'x11vnc -display :0 -forever -nopw -xdamage &' >> /start.sh && \
    echo 'cd /opt/noVNC' >> /start.sh && \
    echo './utils/novnc_proxy --vnc localhost:5900 --listen 8080 &' >> /start.sh && \
    echo 'cd /app' >> /start.sh && \
    echo 'scala Main' >> /start.sh && \
    chmod +x /start.sh

# Usar ENTRYPOINT con el script de inicio
ENTRYPOINT ["/start.sh"]