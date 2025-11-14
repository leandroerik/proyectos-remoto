/*SQL_SERVER*/
USE hipotecarioTAS
GO

-- Verifica si existe la funcionalidad 4 y actualiza o inserta la descripción
IF NOT EXISTS (
    SELECT 1
    FROM TASEstadoMigracion
    WHERE id_bacuni = 4
)
BEGIN
    -- Inserta nueva funcionalidad
    INSERT INTO TASEstadoMigracion (id_bacuni, desc_bacuni)
    VALUES (4, 'cuentas - depositos - tarjetas consultivo - tarjetas pagos - tarjetas cambios pago - tarjetas envio resumen');
END
ELSE
BEGIN
    -- Actualiza la descripción de la funcionalidad 4
    UPDATE TASEstadoMigracion 
    SET desc_bacuni = 'cuentas - depositos - tarjetas consultivo - tarjetas pagos - tarjetas cambios pago - tarjetas envio resumen'
    WHERE id_bacuni = 4;
END
GO


