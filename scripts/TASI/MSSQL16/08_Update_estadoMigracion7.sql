/*SQL_SERVER*/
USE hipotecarioTAS
GO

-- Verifica si ya existe la nueva funcionalidad
IF EXISTS (
    SELECT 1
    FROM TASEstadoMigracion
    WHERE id_bacuni = 7
)
BEGIN
-- Inserta nueva funcionalidad en TASEstadoMigracion
UPDATE TASEstadoMigracion 
	SET desc_bacuni = 'Auditoria - Plazo Fijos Logros - Cambio Clave - Alta Caja de Ahorro'
	WHERE id_bacuni = 7
    
END
GO


