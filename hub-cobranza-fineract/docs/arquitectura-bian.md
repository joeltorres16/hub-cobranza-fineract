# Alineamiento de `hub-cobranza-fineract` con BIAN

## Alcance y versión

Este análisis toma como referencia **BIAN Service Landscape 14.0**, publicado en febrero de 2026. La versión identifica los Service Domains y sus responsabilidades de negocio; el mapeo siguiente es una correspondencia arquitectónica conceptual, no afirma que este Hub implemente íntegramente ningún Service Domain ni que su API sea una implementación de las Semantic APIs de BIAN.

El caso de uso es acotado: Apache Fineract mantiene los préstamos, sus cronogramas y los pagos; `hub-cobranza-fineract` consulta esa información por HTTP y conserva una vista local de las cuotas con saldo pendiente. El Hub no origina ni administra el préstamo y tampoco inicia pagos, contacta clientes o ejecuta estrategias de recuperación.

## Service Domains relacionados

| Service Domain BIAN (nombre oficial) | Responsabilidad descrita por BIAN | Relación con la solución | Alcance / límite |
|---|---|---|---|
| **Consumer Loan** | Gestiona la realización de un producto de préstamo de consumo, incluyendo la configuración inicial del préstamo y el procesamiento programado o eventual. | Es la referencia funcional más cercana al préstamo individual del caso. Fineract es el sistema que administra el préstamo y entrega al Hub el cronograma y sus saldos mediante su API pública. | El Hub solo consume datos del préstamo; no crea, aprueba, desembolsa ni modifica el préstamo. La alineación corresponde principalmente a la responsabilidad del sistema fuente, no a una implementación del dominio por el Hub. |
| **Delinquent Account Handling** | Trata cuentas morosas mediante revisión y seguimiento de pagos debidos; el dominio descrito por BIAN se orienta típicamente a cuentas activas de crédito o charge card y puede transferir casos a Collections. | Las órdenes locales identifican cuotas con saldo pendiente, vencidas o próximas a vencer y permiten consultarlas por préstamo, vencimiento y situación. Esta vista puede apoyar un proceso de seguimiento de cobranza. | La solución no gestiona contactos, promesas de pago, acciones de recuperación ni transferencia de casos. Tampoco se debe interpretar cada cuota pendiente como una cuenta morosa: el Hub incluye cuotas futuras dentro del horizonte. Por ello, es una afinidad parcial, no una equivalencia funcional. |

## Responsabilidades del Hub que no se presentan como Service Domains

El **lote de sincronización** (`LoteCobranza`) es una decisión de diseño técnico para agrupar una ejecución, registrar su ventana, resultado y errores. La **orden de cobranza** (`OrdenCobranza`) es el modelo local de una cuota pendiente para consulta operacional, no una orden de pago ni una instrucción para mover fondos. El historial de lotes y la auditoría de las lecturas también son responsabilidades internas del Hub. En este diseño no se les asigna un Service Domain BIAN específico porque no hay evidencia de que sean una implementación directa de uno.

El Service Domain **Collections** de BIAN no se usa como correspondencia directa: su definición en el artefacto consultado se refiere a liquidar activos para compensar pérdidas en cuentas problemáticas, responsabilidad distinta de listar cuotas pendientes para su cobranza.

## Flujo y límites de responsabilidad

1. Un consumidor solicita al Hub una sincronización con préstamos, fecha de corte y horizonte.
2. El Hub consulta a Fineract por HTTP; Fineract es la fuente de verdad del préstamo, el cronograma y los saldos derivados de pagos.
3. El Hub aplica el filtro de ventana y saldo pendiente, crea el lote y persiste las órdenes en su propia base de datos.
4. El consumidor consulta los lotes y las órdenes locales. Los pagos de demostración se registran directamente en Fineract; una nueva sincronización refleja los saldos que devuelve el core.

Esta separación evita atribuir al Hub cálculos de intereses, registro de pagos o mantenimiento del préstamo. El total local por cobrar es la suma del capital e interés pendientes que se leen de Fineract.

## Fuentes

- BIAN, [Service Landscape](https://bian.org/deliverables/service-landscape/): identifica la versión 14.0 y su publicación en febrero de 2026.
- BIAN, [repositorio oficial de Semantic APIs, Consumer Loan, release 14.0.0](https://github.com/bian-official/public/blob/main/release14.0.0/semantic-apis/oas3%20/yamls/ConsumerLoan.yaml): definición y contrato publicados para el Service Domain.
- BIAN, [repositorio oficial de Semantic APIs, Delinquent Account Handling, release 14.0.0](https://github.com/bian-official/public/blob/main/release14.0.0/semantic-apis/oas3%20/yamls/DelinquentAccountHandling.yaml): definición y contrato publicados para el Service Domain.
- BIAN, [repositorio oficial de Semantic APIs, Collections, release 14.0.0](https://github.com/bian-official/public/blob/main/release14.0.0/semantic-apis/oas3%20/yamls/Collections.yaml): definición consultada para delimitar por qué no se usa como correspondencia directa.

Las definiciones de los Service Domains se resumen y traducen aquí al español; para el texto normativo y sus operaciones, consultar los artefactos oficiales enlazados. Los Service Domains se usan como referencias de capacidades de negocio, no como una afirmación de conformidad o certificación BIAN del Hub.
