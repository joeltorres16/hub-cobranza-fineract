# Arquitectura objetivo en AWS para `hub-cobranza-fineract`

Esta es una **propuesta de despliegue objetivo**, no la arquitectura de la demostración local. La demostración funcional se ejecuta con Docker Compose; esta propuesta lleva a AWS tanto Apache Fineract 1.12.1 como el Hub y sus bases de datos. El diagrama se entrega en [PNG](diagramas/02-arquitectura-aws.png) y en su fuente editable [Draw.io](diagramas/02-arquitectura-aws.drawio).

## Componentes y decisiones de diseño

### Red y segmentación

La solución se aísla en una `Amazon VPC` distribuida en varias zonas de disponibilidad:

* **Subredes públicas:** alojan el `Application Load Balancer` (`ALB`), el único punto de entrada público de la API del Hub. El ALB acepta HTTPS por el puerto `443` y termina TLS.
* **Subredes privadas de aplicación:** alojan servicios ECS sobre AWS Fargate para el Hub y Fineract. No tienen direcciones públicas ni reciben conexiones entrantes directas desde Internet.
* **Subredes privadas aisladas para datos:** alojan dos despliegues RDS independientes: uno para `hub_cobranza` y otro para las bases de Fineract, como `fineract_tenants` y `fineract_default`. Los grupos de subredes de RDS deben abarcar varias zonas de disponibilidad.
* **Grupos de seguridad:** el ALB acepta `443` desde los orígenes permitidos; el servicio Hub acepta `8080` únicamente desde el grupo del ALB; Fineract acepta `8443` únicamente desde el grupo del Hub; cada RDS acepta `3306` únicamente desde el grupo del servicio que le corresponde. El Hub no obtiene acceso JDBC a la base de Fineract.
* **Autenticación del consumidor:** el ALB puede usar la acción `authenticate-cognito` con Amazon Cognito User Pools (o un proveedor OIDC compatible) antes de enrutar solicitudes al Hub. Esto es una capacidad de la arquitectura objetivo que aún requiere configuración e integración; no implica que la API actual del Hub ya autentique a sus consumidores.

### Cómputo e integración

Se proponen servicios ECS separados sobre AWS Fargate para el Hub y Apache Fineract 1.12.1. La imagen del Hub (Java 21 y Spring Boot) se publica en Amazon ECR; la imagen y configuración de Fineract se despliegan como un servicio independiente. El Hub consulta el cronograma mediante la API REST HTTPS de Fineract en `8443`; no accede a sus tablas. En producción, el canal debe validar certificados TLS y las credenciales de Fineract deben limitarse a los permisos necesarios.

Los servicios privados necesitan acceso controlado a ECR y a los servicios de AWS requeridos para secretos y telemetría, mediante VPC endpoints cuando estén disponibles o una ruta de salida controlada. El ALB no expone la API de Fineract al consumidor externo.

### Persistencia

Amazon RDS for MariaDB aloja bases separadas para el Hub y Fineract; separar las instancias reduce el acoplamiento de disponibilidad, permisos, respaldos y mantenimiento. RDS se configura en subredes privadas, con cifrado en reposo usando AWS KMS y respaldos automáticos. Flyway administra el esquema del Hub. Fineract administra su propio esquema y migraciones mediante Liquibase; no se ejecutan migraciones de Fineract desde el Hub.

La versión de MariaDB de cada instancia RDS debe seleccionarse entre las versiones ofrecidas por AWS y validarse contra los requisitos de Apache Fineract 1.12.1 antes del despliegue. El diagrama muestra la topología lógica, no sustituye esa validación de compatibilidad ni define tamaños, RPO/RTO o una estrategia final de alta disponibilidad.

### Secretos y observabilidad

AWS Secrets Manager almacena credenciales de base de datos y de Fineract; AWS Systems Manager Parameter Store puede alojar configuración no secreta, como el tenant y las URL. Las tareas ECS reciben solo los secretos que necesitan mediante roles IAM de mínimo privilegio. AWS KMS protege secretos y datos cifrados en reposo.

Amazon CloudWatch centraliza logs de las tareas y métricas de infraestructura y aplicación, con alarmas para fallos de sincronización y errores de integración. No se debe registrar contraseñas, tokens ni datos sensibles en logs.

## Relación con BIAN

La separación de responsabilidades refleja el mapeo conceptual de [arquitectura-bian.md](arquitectura-bian.md): Fineract conserva la responsabilidad operativa del préstamo y su cronograma, relacionada con **Consumer Loan**; el Hub ofrece una vista local de cuotas pendientes que puede apoyar actividades de seguimiento relacionadas parcialmente con **Delinquent Account Handling**. El Hub no implementa por sí solo esos Service Domains ni ejecuta pagos, gestión de morosidad o recuperación. La elección de AWS describe el despliegue de los componentes, no cambia esas responsabilidades de negocio.

## Alcance del diagrama

El diagrama es una vista lógica de alto nivel de una arquitectura AWS propuesta: presenta el límite de la VPC, subredes públicas y privadas, servicios de cómputo, bases de datos, flujos de red y servicios transversales. No es un diagrama de la instalación Docker local ni un diseño listo para desplegar sin decisiones adicionales de capacidad, continuidad, monitoreo, IAM, certificados, compatibilidad de motores y controles de seguridad.