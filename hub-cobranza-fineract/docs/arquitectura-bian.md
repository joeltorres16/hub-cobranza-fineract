Propósito y rol de hub-cobranza-fineract bajo enfoque BIAN

hub-cobranza-fineract actúa como un microservicio especializado de gestión y orquestación de cobranzas que se integra con Apache Fineract 1.12.1 (core bancario responsable de la cuenta de préstamo, cronograma de cuotas, estrategia de imputación mifos-standard-strategy y registro contable/transaccional de pagos) y persiste su propio modelo operativo y de auditoría en MariaDB (hub_cobranza).

En lugar de acoplar consumidores externos directamente a las estructuras internas del core o sobrescribir el historial de cada consulta, el microservicio expone una interfaz propia (POST /api/v1/lotes/sincronizar, consultas de lotes y consultas de órdenes de cobranza) y mantiene trazabilidad de cada corrida mediante lotes y órdenes de cobranza.



| Concepto en `hub-cobranza-fineract` / Fineract | Bloque funcional / dominio de referencia BIAN | Responsabilidad en el flujo |
| :--- | :--- | :--- |
| Préstamo, cronograma y pagos (`LOAN-DEMO-001` en Fineract) | Gestión de préstamos / cuentas de crédito y pagos (`Loan` / `Repayment`) | Fuente de verdad del cronograma de cuotas, capital e interés por cuota, aplicación de pagos (`S/ 500,00` y `S/ 620,00`) y estado actual de deuda. |
| Lote de sincronización (`Lote`) | Orquestación / control de procesos batch de cobranza | Define la ventana de evaluación (`fechaCorte`, `diasHorizonte`, `fechaFinHorizonte`), los préstamos consultados, el estado (`COMPLETADO` / `FALLIDO`), cantidad de órdenes y marcas de tiempo. |
| Orden de cobranza (`OrdenCobranza`) | Gestión de cobranzas / seguimiento de cuotas por cobrar (`Collection` / `Receivable`) | Representa cada cuota exigible dentro de la ventana con desglose de `capitalPendiente`, `interesPendiente`, `totalPorCobrar` y `situacion` (`PENDIENTE`, `PAGO_PARCIAL`; se omite cuando queda saldada). |
| Historial de lotes (`9`, `10`, `11`) | Registro y auditoría de estados operativos | Conserva la fotografía de cada sincronización para contrastar el estado inicial, el pago parcial y la cancelación de la cuota 1 sin perder trazabilidad. |