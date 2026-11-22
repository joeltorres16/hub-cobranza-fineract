# hub-cobranza-fineract

Microservicio de orquestación y gestión de cobranzas desarrollado en **Java 21** y **Spring Boot**, integrado con **Apache Fineract 1.12.1** como core de préstamos y **MariaDB** como base de datos de operación y trazabilidad.

A diferencia de consultar o sobrescribir el cronograma directamente en cada lectura, `hub-cobranza-fineract` coordina procesos de sincronización por lotes (`Lote`) y genera registros de cobranza (`OrdenCobranza`) por cuota dentro de una ventana definida (`fechaCorte` y `diasHorizonte`), manteniendo historial de estados (`PENDIENTE`, `PAGO_PARCIAL`, etc.) y conciliación ante pagos registrados en Fineract.

---

## Arquitectura y diseño

- **Alineación BIAN:** [docs/arquitectura-bian.md](docs/arquitectura-bian.md)
- **Arquitectura objetivo en AWS:** [docs/arquitectura-aws.md](docs/arquitectura-aws.md)
- **Evidencias de pruebas (Word):** [docs/evidencia_pruebas_hub_cobranza_fineract.docx](docs/evidencia_pruebas_hub_cobranza_fineract.docx)
- **Diagramas:**
  - [docs/diagramas/01-flujo-sincronizacion-bian.png](docs/diagramas/01-flujo-sincronizacion-bian.png)
  - [docs/diagramas/02-arquitectura-aws.png](docs/diagramas/02-arquitectura-aws.png)
  - [Fuente editable Draw.io de la arquitectura AWS](docs/diagramas/02-arquitectura-aws.drawio)

---

## Estructura del repositorio

```text
hub-cobranza-fineract/
├── src/                                                      # Código fuente Java 21 / Spring Boot y migraciones Flyway
├── docker/
│   └── mariadb-init/
│       └── 01-init-databases.sql                             # Creación inicial de bases de datos en MariaDB
├── docs/
│   ├── diagramas/
│   │   ├── 01-flujo-sincronizacion-bian.png
│   │   ├── 02-arquitectura-aws.png
│   │   └── 02-arquitectura-aws.drawio                       # Fuente editable del diagrama AWS
│   ├── postman/
│   │   ├── 01.configuracionFineract.postman_collection.json  # Configuración de fechas, cliente, producto, préstamo y desembolso
│   │   └── 02.EjecucionPruebaModelo.postman_collection.json    # Ejecución de sincronizaciones, pagos parciales/totales y conciliación
│   ├── arquitectura-aws.md                                   # Propuesta objetivo de despliegue AWS
│   ├── arquitectura-bian.md                                  # Mapeo conceptual y flujo bajo enfoque BIAN
│   └── evidencia_pruebas_hub_cobranza_fineract.docx          # Evidencias de ejecución y secciones de arquitectura
├── .env.example                                              # Plantilla de variables de entorno (sin secretos reales)
├── .env                                                      # Variables locales (ignorado en Git)
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```
---
## Requisitos previos ##
1. Docker y Docker Compose (docker compose version)
2. Postman (Desktop o Web) para importar y ejecutar las colecciones
3. Java 21 y Maven (opcional, solo si deseas compilar o correr pruebas unitarias fuera de Docker)
---
## Puesta en marcha y replicación
**1. Configurar variables de entorno:**

   Copia la plantilla .env.example a .env y ajusta únicamente los valores necesarios para tu entorno local (sin dejar contraseñas reales ni datos sensibles en el repositorio):

    ```bash
    cp .env.example .env
    ```
   (En PowerShell: Copy-Item .env.example .env)


**2. Levantar contenedores con Docker Compose:**

Desde la raíz del proyecto (hub-cobranza-fineract), construye y levanta MariaDB (11.5.2), Apache Fineract (1.12.1) y collection-hub (hub-cobranza-fineract):

    ```bash
        docker compose up -d --build
    ```
* **Bases de datos y migraciones:** Al inicializar el volumen de MariaDB por primera vez, [docker/mariadb-init/01-init-databases.sql](docker/mariadb-init/01-init-databases.sql) crea las bases fineract_tenants, fineract_default y hub_cobranza. Liquibase (dentro de Fineract) genera las tablas internas del core, y Flyway ([src/main/resources/db/migration/V1__init.sql](src/main/resources/db/migration/V1__init.sql), dentro de collection-hub) crea las tablas lote_cobranza y orden_cobranza con sus índices.
* **Tiempo de arranque:** El primer arranque de Fineract puede demorar mientras ejecuta las migraciones de Liquibase; espera a que los servicios estén saludables antes de lanzar las colecciones.

**Verifica que los contenedores estén activos y saludables:**

    ```bash
    docker compose ps
    ```

**Para detener los servicios conservando datos:**

    ```bash
    docker compose down
    ```

**Para detener los servicios y eliminar volúmenes (reiniciar la base de datos desde cero):**

    ```bash
    docker compose down -v
    ```

**3. Contrato API (OpenAPI/Swagger)**

El contrato de la API REST del Hub de Cobranza (`collection-hub`) se expone mediante OpenAPI / Swagger y está documentado en:

- **Especificación OpenAPI:** `docs/api-docs.yaml`
- **Swagger UI (entorno levantado):** `http://localhost:8080/swagger-ui.html`
- **Documento OpenAPI en vivo:** `http://localhost:8080/v3/api-docs`

**Operaciones principales incluidas en el contrato:**
- **Sincronización por lote:** inicia una sincronización para uno o varios préstamos indicando fecha de corte y horizonte de consulta, incluyendo cuotas vencidas o con vencimiento hasta el fin del horizonte que mantengan saldo pendiente.
- **Consulta de lotes:** permite consultar la información y el estado de un lote de sincronización ejecutado.
- **Consulta de órdenes de cobranza:** permite consultar las órdenes locales filtrando por préstamo, vencimiento y situación, exponiendo cliente, préstamo, cuota de origen, moneda, vencimiento, capital pendiente, interés pendiente y total por cobrar.


**4. Ejecutar la configuración base en Fineract (Postman 1)**

   1. Abre Postman e importa 
   [docs/postman/01.configuracionFineract.postman_collection](docs/postman/01.configuracionFineract.postman_collection)

   2. Ejecuta los requests en el orden establecido para preparar el escenario base

5. Ejecutar las pruebas del Hub de Cobranza (Postman 2)

   1. Importa [docs/postman/02.EjecucionPruebaModelo.postman_collection](docs/postman/02.EjecucionPruebaModelo.postman_collection)
   2. Ejecuta la secuencia de pruebas de hub-cobranza-fineract y fineract como validador de información.
   3. Si los IDs generados en una base limpia difieren de corridas anteriores (por ejemplo, loanId o número de lote), actualiza la variable o parámetro correspondiente en los requests antes de continuar.

6. Revisar evidencias y resultados

   1. Consulta el documento de evidencias en docs/evidencia_pruebas_hub_cobranza_fineract.docx para revisar las capturas, respuestas y evolución de lotes y cuotas.
   2. También puedes consultar directamente los endpoints de lotes y órdenes de cobranza de hub-cobranza-fineract o inspeccionar la base hub_cobranza en MariaDB.

---
## Notas de seguridad y mantenimiento

1. El archivo .env está excluido mediante .gitignore; utiliza .env.example como referencia de variables.
2. En entornos productivos (como la arquitectura AWS descrita en docs/arquitectura-aws.md), las credenciales y secretos deben administrarse mediante AWS Secrets Manager / SSM Parameter Store e inyectarse en tiempo de ejecución.

### Alcance de seguridad del laboratorio

El entorno Docker está destinado a pruebas locales con datos ficticios. La API del Hub no implementa autenticación ni autorización; por ello, no debe exponerse a redes compartidas o públicas. Para producción se propone autenticar a los consumidores mediante OIDC, aplicar autorización por permisos, guardar credenciales en AWS Secrets Manager y validar los certificados TLS de las conexiones con Fineract. La configuración que omite la validación TLS, si se utiliza para esta prueba técnica, es exclusiva de desarrollo y no debe utilizarse en producción.