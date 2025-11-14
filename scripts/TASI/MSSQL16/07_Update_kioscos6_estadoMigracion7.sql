/*SQL_SERVER*/
USE hipotecarioTAS
GO

-- Verifica si ya existe la nueva funcionalidad
IF NOT EXISTS (
    SELECT 1
    FROM TASEstadoMigracion
    WHERE id_bacuni = 7
)
BEGIN
-- Verifica si existen las funcionalidades anteriores
    IF EXISTS (
        SELECT 1
        FROM TASEstadoMigracion
        WHERE id_bacuni IN (1, 2, 3, 4, 5,6)
    )
BEGIN
-- Inserta nueva funcionalidad en TASEstadoMigracion
INSERT INTO TASEstadoMigracion (id_bacuni, desc_bacuni)
VALUES
    (7, 'Auditoria')
    
END
END
GO
-- Actualiza todas las terminales
	UPDATE TASKioscos SET bac_uni= 6;
-- Actualiza TASKioscos
	UPDATE TASKioscos SET bac_uni = 7 WHERE KioscoId in (125,154);
	UPDATE TASKioscos SET bac_uni = 7 WHERE DireccionIP = '10.99.101.212';
	UPDATE TASKioscos SET bac_uni = 7 WHERE DireccionIP = '10.0.2.15';
	
GO


