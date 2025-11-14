/*SQL_SERVER*/
USE hipotecarioTAS
GO

-- Verifica si ya existe la nueva funcionalidad
IF NOT EXISTS (
    SELECT 1
    FROM TASEstadoMigracion
    WHERE id_bacuni = 5
)
BEGIN
-- Verifica si existen las funcionalidades anteriores
    IF EXISTS (
        SELECT 1
        FROM TASEstadoMigracion
        WHERE id_bacuni IN (1, 2, 3, 4)
    )
BEGIN
-- Inserta nueva funcionalidad en TASEstadoMigracion
INSERT INTO TASEstadoMigracion (id_bacuni, desc_bacuni)
VALUES
    (5, 'Tarjetas de Debito consultivo - Tarjetas de Debito acciones - Prestamos consultivo - Prestamos acciones')
    
END
END
GO
-- Actualiza todas las terminales
	UPDATE TASKioscos SET bac_uni= 4;
-- Actualiza TASKioscos
	UPDATE TASKioscos SET bac_uni = 5 WHERE KioscoId in (125,154);
	UPDATE TASKioscos SET bac_uni = 5 WHERE DireccionIP = '10.99.101.212';
	UPDATE TASKioscos SET bac_uni = 5 WHERE DireccionIP = '10.0.2.15';
	
GO


