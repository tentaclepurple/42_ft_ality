# noVNC Services Recovery Guide for Docker Container

## Prerequisites
Make sure these packages are installed inside the container:
```bash
apt-get update
apt-get install net-tools psmisc
```

## 1. Container Access

From the host (VPS), access the container:
```bash
docker exec -it scala bash
```

## 2. Initial Verification

Check the current status of services:
```bash
# View active processes
ps aux | grep -E "Xvfb|x11vnc|novnc|fluxbox"

# View ports in use
netstat -tulpn | grep -E '5900|8080'
```

## 3. Process Cleanup

If necessary, stop all services:
```bash
# Kill existing processes
pkill Xvfb
pkill x11vnc
pkill fluxbox
pkill -f novnc_proxy

# Verify no processes remain
ps aux | grep -E "Xvfb|x11vnc|novnc|fluxbox"
```

## 4. Starting Services

Start services in the correct order:

### 4.1 Virtual X Server (Xvfb)
```bash
Xvfb :0 -screen 0 1920x720x16 &
```

### 4.2 Window Manager (Fluxbox)
```bash
sleep 1
fluxbox &
```

### 4.3 VNC Server (x11vnc)
```bash
x11vnc -display :0 -listen localhost -rfbport 5900 -forever -shared -nopw &
```

### 4.4 noVNC Proxy
```bash
cd /opt/noVNC
./utils/novnc_proxy --vnc localhost:5900 --listen 8080 &
```

## 5. Final Verification

### 5.1 Verify Processes
```bash
ps aux | grep -E "Xvfb|x11vnc|novnc|fluxbox"
```
You should see all 4 services running.

### 5.2 Verify Ports
```bash
netstat -tulpn | grep -E '5900|8080'
```
You should see:
- Port 5900 for VNC
- Port 8080 for noVNC

## 6. Common Issues and Solutions

### Port 8080 in use
If you get the "Port 8080 in use" error:
```bash
# Identify the process
netstat -tulpn | grep 8080

# Kill the process
kill <PID>

# Restart noVNC proxy
cd /opt/noVNC
./utils/novnc_proxy --vnc localhost:5900 --listen 8080 &
```

### x11vnc not running
If you can't connect to VNC:
```bash
# Kill existing x11vnc
pkill x11vnc

# Restart with different parameters
x11vnc -display :0 -listen 0.0.0.0 -rfbport 5900 -shared -forever -nopw -xdamage &
```

## 7. Access Verification

After all services are running, access your noVNC interface at:
```
https://your-ip-address
```

The connection might show a security warning due to the self-signed certificate. This is normal and can be safely bypassed for your own server.
