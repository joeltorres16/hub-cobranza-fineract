CREATE TABLE IF NOT EXISTS lote_cobranza (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_corte DATE NOT NULL,
    dias_horizonte INT NOT NULL,
    fecha_fin_horizonte DATE NOT NULL,
    prestamos_solicitados VARCHAR(500) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    total_ordenes INT NOT NULL DEFAULT 0,
    mensaje_error VARCHAR(1000) NULL,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS orden_cobranza (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    cliente_id BIGINT NULL,
    cliente_referencia_externa VARCHAR(100) NULL,
    cliente_nombre VARCHAR(200) NOT NULL,
    prestamo_id BIGINT NOT NULL,
    prestamo_referencia_externa VARCHAR(100) NULL,
    numero_cuota INT NOT NULL,
    moneda VARCHAR(10) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    capital_pendiente DECIMAL(19,2) NOT NULL,
    interes_pendiente DECIMAL(19,2) NOT NULL,
    total_por_cobrar DECIMAL(19,2) NOT NULL,
    situacion VARCHAR(30) NOT NULL,
    sincronizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orden_lote FOREIGN KEY (lote_id) REFERENCES lote_cobranza (id),
    CONSTRAINT uq_orden_lote_prestamo_cuota UNIQUE (lote_id, prestamo_id, numero_cuota)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_orden_prestamo_vencimiento ON orden_cobranza (prestamo_id, fecha_vencimiento);
CREATE INDEX idx_orden_situacion ON orden_cobranza (situacion);