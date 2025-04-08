# Proyecto Base Implementando Clean Architecture

## Antes de Iniciar

Empezaremos por explicar los diferentes componentes del proyectos y partiremos de los componentes externos, continuando
con los componentes core de negocio (dominio) y por �ltimo el inicio y configuraci�n de la aplicaci�n.

Lee el
art�culo [Clean Architecture � Aislando los detalles](https://medium.com/bancolombia-tech/clean-architecture-aislando-los-detalles-4f9530f35d7a)

# Arquitectura

![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)

## Domain

Es el m�dulo m�s interno de la arquitectura, pertenece a la capa del dominio y encapsula la l�gica y reglas del negocio
mediante modelos y entidades del dominio.

## Usecases

Este m�dulo gradle perteneciente a la capa del dominio, implementa los casos de uso del sistema, define l�gica de
aplicaci�n y reacciona a las invocaciones desde el m�dulo de entry points, orquestando los flujos hacia el m�dulo de
entities.

## Infrastructure

### Helpers

En el apartado de helpers tendremos utilidades generales para los Driven Adapters y Entry Points.

Estas utilidades no est�n arraigadas a objetos concretos, se realiza el uso de generics para modelar comportamientos
gen�ricos de los diferentes objetos de persistencia que puedan existir, este tipo de implementaciones se realizan
basadas en el patr�n de
dise�o [Unit of Work y Repository](https://medium.com/@krzychukosobudzki/repository-design-pattern-bc490b256006)

Estas clases no puede existir solas y debe heredarse su compartimiento en los **Driven Adapters**

### Driven Adapters

Los driven adapter representan implementaciones externas a nuestro sistema, como lo son conexiones a servicios rest,
soap, bases de datos, lectura de archivos planos, y en concreto cualquier origen y fuente de datos con la que debamos
interactuar.

### Entry Points

Los entry points representan los puntos de entrada de la aplicaci�n o el inicio de los flujos de negocio.

## Application

Este m�dulo es el m�s externo de la arquitectura, es el encargado de ensamblar los distintos m�dulos, resolver las
dependencias y crear los beans de los casos de use (UseCases) de forma autom�tica, inyectando en �stos instancias
concretas de las dependencias declaradas. Adem�s inicia la aplicaci�n (es el �nico m�dulo del proyecto donde
encontraremos la funci�n �public static void main(String[] args)�.

**Los beans de los casos de uso se disponibilizan automaticamente gracias a un '@ComponentScan' ubicado en esta capa.**

Perfecto, te dejo los pasos detallados para que puedas copiar y pegar en tu README. Está enfocado para entorno local
usando **Podman** y **PowerShell** en Windows, con una aplicación Spring Boot que expone métricas en
`/actuator/prometheus`.

---

## 🧩 Observabilidad con Prometheus y Grafana usando Podman en Windows

### 1. Habilita Prometheus en Spring Boot

Agrega estas dependencias en `build.gradle`:

```groovy
implementation 'io.micrometer:micrometer-registry-prometheus'
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

En `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

Tu app expone métricas en:  
👉 `http://localhost:8080/actuator/prometheus`

---

### 2. Crea el archivo de configuración de Prometheus

Crea un archivo llamado `prometheus.yml` en la raíz del proyecto con este contenido:

```yaml
global:
  scrape_interval: 5s

scrape_configs:
- job_name: 'spring-boot-app'
  metrics_path: '/actuator/prometheus'
  static_configs:
  - targets: [ 'host.containers.internal:8080' ]
```

> ⚠️ `host.containers.internal` permite que los contenedores accedan a tu máquina host desde Podman.

---

### 3. Levanta Prometheus con Podman

Abre PowerShell en la raíz del proyecto y ejecuta:

```powershell
podman run -d --name prometheus `
  -p 9090:9090 `
  -v "${PWD}\prometheus.yml:/etc/prometheus/prometheus.yml:ro" `
  prom/prometheus
```

---

### 4. Levanta Grafana con Podman

```powershell
podman run -d --name grafana `
  -p 3000:3000 `
  grafana/grafana
```

---

### 5. Configura Grafana

1. Accede a [http://localhost:3000](http://localhost:3000)
2. Usuario: `admin`, Contraseña: `admin` (te pedirá cambiarla)
3. Ve a **Settings > Data Sources > Add data source**
4. Elige **Prometheus**
5. En la URL escribe: `http://host.containers.internal:9090`
6. Click en **Save & Test**

---

### 6. Visualiza métricas

Puedes crear un dashboard personalizado. Algunas métricas útiles:

- Mensajes procesados:
  ```promql
  rate(spring_rabbitmq_listener_seconds_count[1m])
  ```
- Errores:
  ```promql
  rate(spring_rabbitmq_listener_seconds_count{result="failure"}[1m])
  ```
- Tiempo de procesamiento:
  ```promql
  rate(spring_rabbitmq_listener_seconds_sum[1m])
  ```

---
