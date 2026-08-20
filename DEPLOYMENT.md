# QuickAnswer Backend — Deployment Guide

> **Stack:** Spring Boot 3.5.5 · Java 21 · Docker (multi-stage build)  
> **Exposes:** `POST /api/answer` · `GET /server` (health check)

---

## Table of Contents

1. [Environment Variables](#1-environment-variables)
2. [Build the Docker Image](#2-build-the-docker-image)
3. [Run Locally with Docker Compose](#3-run-locally-with-docker-compose)
4. [Push to Docker Hub / GHCR](#4-push-to-docker-hub--ghcr)
5. [Deploy on Railway](#5-deploy-on-railway)
6. [Deploy on Render](#6-deploy-on-render)
7. [Deploy on Fly.io](#7-deploy-on-flyio)
8. [Deploy on a VPS (Ubuntu)](#8-deploy-on-a-vps-ubuntu)
9. [Maintenance & Updates](#9-maintenance--updates)

---

## 1. Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `CODECRAFT_API_KEY` | Yes | — | Your CodeCraft API key |
| `CODECRAFT_BASE_URL` | No | `https://codecraftapi.com/v1` | AI provider base URL |
| `CODECRAFT_MODEL` | No | `claude-opus-5` | Model name to use |
| `CORS_ALLOWED_ORIGIN` | No | `chrome-extension://*` | Allowed CORS origin(s), comma-separated |
| `PORT` | No | `8080` | HTTP port (auto-set by Railway/Render) |

> **Never commit `.env` to git.** Use `.env.example` as the template.

---

## 2. Build the Docker Image

```bash
# Clone the repo
git clone <your-repo-url>
cd QuickAnswerExtension

# Build with version tag
docker build \
  -t quick-answer-backend:1.0.0 \
  -t quick-answer-backend:latest \
  ./spring-backend

# Verify image was created
docker images quick-answer-backend
```

### Image details
- **Base:** `eclipse-temurin:21-jre` (slim JRE, not full JDK)
- **Runs as:** non-root user `appuser` (security hardened)
- **JVM flags:** container-aware (`UseContainerSupport`, `MaxRAMPercentage=75%`)
- **Build stages:** deps cache -> build -> runtime (fast incremental rebuilds)

---

## 3. Run Locally with Docker Compose

```bash
# 1. Copy and fill in your env file
cp .env.example .env
# Edit .env — set CODECRAFT_API_KEY=cc_your_real_key

# 2. Start backend only
docker compose up -d

# 3. Verify health
curl http://localhost:8080/server
# Response: "Server is live"

# 4. View logs
docker compose logs -f quick-answer-backend

# 5. Stop
docker compose down
```

---

## 4. Push to Docker Hub / GHCR

### Docker Hub

```bash
# Login
docker login

# Tag with your username
docker tag quick-answer-backend:1.0.0 YOUR_DOCKERHUB_USERNAME/quick-answer-backend:1.0.0
docker tag quick-answer-backend:latest YOUR_DOCKERHUB_USERNAME/quick-answer-backend:latest

# Push
docker push YOUR_DOCKERHUB_USERNAME/quick-answer-backend:1.0.0
docker push YOUR_DOCKERHUB_USERNAME/quick-answer-backend:latest
```

### GitHub Container Registry (GHCR)

```bash
# Login with your GitHub token (needs write:packages scope)
echo $GITHUB_TOKEN | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin

# Tag
docker tag quick-answer-backend:1.0.0 ghcr.io/YOUR_GITHUB_USERNAME/quick-answer-backend:1.0.0

# Push
docker push ghcr.io/YOUR_GITHUB_USERNAME/quick-answer-backend:1.0.0
```

---

## 5. Deploy on Railway

Railway auto-detects the Dockerfile — no config file needed.

1. Go to [railway.app](https://railway.app) -> **New Project** -> **Deploy from GitHub repo**
2. Select your repository
3. Railway will detect `spring-backend/Dockerfile` automatically
4. Go to **Variables** tab and add:
   ```
   CODECRAFT_API_KEY=cc_your_real_key
   ```
5. Railway injects `PORT` automatically — the app reads `${PORT:8080}`
6. Click **Deploy** -> your URL: `https://your-app.up.railway.app`

**Health check path:** `/server`

---

## 6. Deploy on Render

1. Go to [render.com](https://render.com) -> **New** -> **Web Service**
2. Connect your GitHub repo
3. Set:
   - **Runtime:** Docker
   - **Dockerfile Path:** `spring-backend/Dockerfile`
   - **Port:** `8080`
4. Under **Environment** add:
   ```
   CODECRAFT_API_KEY=cc_your_real_key
   ```
5. **Health Check Path:** `/server`
6. Click **Create Web Service**

> Free tier spins down after inactivity. Use Starter ($7/mo) for always-on.

---

## 7. Deploy on Fly.io

```bash
# Install flyctl: https://fly.io/docs/hands-on/install-flyctl/

# Login
fly auth login

# Launch from the spring-backend directory
cd spring-backend
fly launch --dockerfile Dockerfile --name quick-answer-backend --no-deploy

# Set secret env var
fly secrets set CODECRAFT_API_KEY=cc_your_real_key

# Deploy
fly deploy

# Check status
fly status
fly logs
```

App available at: `https://quick-answer-backend.fly.dev`

---

## 8. Deploy on a VPS (Ubuntu)

```bash
# 1. Install Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER && newgrp docker

# 2. Clone the repo
git clone <your-repo-url>
cd QuickAnswerExtension

# 3. Create .env
cp .env.example .env
nano .env   # set CODECRAFT_API_KEY=cc_your_real_key

# 4. Start with Docker Compose
docker compose up -d

# 5. Verify
curl http://localhost:8080/server
```

### Optional: Nginx reverse proxy + SSL

```bash
sudo apt install nginx -y
sudo nano /etc/nginx/sites-available/quickanswer
```

Nginx config:
```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_read_timeout 60s;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/quickanswer /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx

# Free SSL with Let's Encrypt
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d your-domain.com
```

---

## 9. Maintenance & Updates

### Rebuild after code changes

```bash
git pull
docker compose up --build -d
```

### Useful commands

```bash
# View running containers
docker compose ps

# Live logs
docker compose logs -f quick-answer-backend

# Check env vars inside container
docker exec quick-answer-backend env | grep CODECRAFT

# Restart without rebuild
docker compose restart quick-answer-backend

# Stop everything
docker compose down

# Rebuild from scratch
docker compose down --rmi local
docker compose up --build -d
```

### Rotate the API key (zero downtime)

```bash
# 1. Edit .env
#    Update CODECRAFT_API_KEY=cc_new_key

# 2. Recreate container (< 1s downtime)
docker compose up -d

# 3. Verify new key is live
docker exec quick-answer-backend env | grep CODECRAFT_API_KEY
```

---

## CORS Configuration for Production

Lock down CORS to your specific extension ID:

```env
# .env
CORS_ALLOWED_ORIGIN=chrome-extension://abcdefghijklmnopabcdefghijklmnop
```

Multiple origins (comma-separated):
```env
CORS_ALLOWED_ORIGIN=chrome-extension://abcdefghijklmnopabcdefghijklmnop,https://your-frontend.com
```

---

*QuickAnswer v1.0.0 | Generated 2026-10-17*
