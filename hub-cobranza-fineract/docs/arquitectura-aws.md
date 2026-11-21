Arquitectura objetivo en AWS para hub-cobranza-fineract

Para llevar la solución desde el entorno local con Docker Compose a un despliegue en AWS, se propone una arquitectura segmentada por red, con cómputo en contenedores administrados, base de datos relacional gestionada, administración centralizada de secretos y observabilidad unificada.


## Componentes y decisiones de diseño en AWS

### Red y segmentación (`Amazon VPC`)

La solución se aísla dentro de una `Amazon VPC` dividida en capas:

* **Subredes públicas:** alojan el `Application Load Balancer` (`ALB`) como punto de entrada HTTP/HTTPS.
* **Subredes privadas:** alojan los contenedores de `hub-cobranza-fineract`, la base de datos relacional y la integración con el core Fineract, evitando exposición directa a internet.
* **`Security Groups`:** el `ALB` permite tráfico entrante controlado en `443` (`HTTPS`) hacia el puerto `8080` del servicio; el `Security Group` de la base de datos permite `3306` únicamente desde el `Security Group` de `hub-cobranza-fineract`.

### Cómputo (`Amazon ECS` con `AWS Fargate` y `Amazon ECR`)

La imagen Docker de `hub-cobranza-fineract` (`Java 21`, Spring Boot) se publica en `Amazon ECR` y se ejecuta en `AWS Fargate` sin administrar servidores base, con `healthcheck` HTTP y escalado horizontal según demanda.

### Base de datos (`Amazon RDS` para `MariaDB`)

Sustituye al contenedor local de MariaDB en producción, con respaldos automáticos, cifrado en reposo mediante `AWS KMS` y ejecución automática de migraciones con **Flyway** al iniciar el servicio.

### Secretos y configuración (`AWS Secrets Manager` / `SSM Parameter Store`)

Las credenciales de base de datos, credenciales/tenant de Fineract y parámetros de entorno se inyectan de forma segura en tiempo de ejecución, evitando contraseñas hardcodeadas o archivos `.env` dentro de la imagen.

### Observabilidad y monitoreo (`Amazon CloudWatch`)

Recolecta logs de aplicación (`hub-cobranza-fineract`), métricas de CPU/memoria/latencia y alarmas ante fallos de sincronización (`FALLIDO`) o errores HTTP del core (`401`, `5xx`).