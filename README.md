# 🧪 PoC Arquitectura Orientada a Eventos + Observabilidad + DevSecOps

Esta prueba de concepto implementa una arquitectura orientada a eventos usando RabbitMQ y promueve buenas prácticas de
observabilidad y seguridad optimizando imágenes Docker (con Podman y Gradle).

---

## 🚀 Requisitos

- [Podman](https://podman.io/)
- [PowerShell](https://learn.microsoft.com/en-us/powershell/)
- [Java 17](https://adoptium.net/)
- [Gradle](https://gradle.org/)

---

## 🐰 RabbitMQ

### 1. Levantar RabbitMQ (solo la primera vez)

```powershell
podman run -d --restart always --name rabbitmq `
  -p 5672:5672 -p 15672:15672 `
  rabbitmq:3.9-management
```

### 2. Iniciar RabbitMQ (después)

```powershell
podman start rabbitmq
```

Accede al panel: [http://localhost:15672](http://localhost:15672)  
Usuario: `guest` – Contraseña: `guest`

---

## 📈 Prometheus + Grafana

### 3. Levanta Prometheus con Podman

```powershell
podman run -d --name prometheus `
  -p 9090:9090 `
  -v "${PWD}\prometheus.yml:/etc/prometheus/prometheus.yml:ro" `
  prom/prometheus
```

### 4. Levanta Grafana con Podman

```powershell
podman run -d --name grafana `
  -p 3000:3000 `
  grafana/grafana
```

### 5. Configura Grafana

1. Abre [http://localhost:3000](http://localhost:3000)
2. Usuario: `admin` | Contraseña: `admin` (te pedirá cambiarla)
3. Ve a **Settings > Data Sources > Add data source**
4. Selecciona **Prometheus**
5. Usa esta URL: `http://host.containers.internal:9090`
6. Clic en **Save & Test**

---

## 📦 Construcción y ejecución de la app

La imagen está optimizada y basada en una imagen mínima de Java, compilada con Gradle.

### 6. Build y run de la aplicación

```powershell
./gradlew clean build
Move-Item -Path "applications/app-service/build/libs/poc-rabbitmq.jar" -Destination "deployment/" -Force
cd deployment
podman build -t poc-rabbitmq-img .
podman run --name poc-rabbitmq-cnt -p 8080:8080 poc-rabbitmq-img

podman rm poc-rabbitmq-cnt
podman rmi poc-rabbitmq-img

.\trivy.exe image localhost/poc-rabbitmq-img
```

La app estará disponible en: [http://localhost:8080](http://localhost:8080)

---

## 🛡️ Seguridad y optimización

- Imagen de base mínima (`eclipse-temurin:17-jdk-alpine`)
- Separación en multi-stage build
- Usuario no root
- `.dockerignore` configurado

---
