/*SQL_SERVER*/
USE hipotecarioTAS
GO

IF NOT EXISTS (
    SELECT 1
    FROM TASEstadoMigracion
    WHERE id_bacuni in (1,2)
)
BEGIN
  -- Inserta datos iniciales en TASEstadoMigracion
INSERT INTO TASEstadoMigracion (id_bacuni, desc_bacuni)
VALUES
    (1, 'consulta estado BACUNI - orden ingreso kiosco - login - logout - posicion consolidada'),
    (2, 'cuentas - detalle CA/CC - ultimos movimientos CA/CC');
END
GO
-- Actualiza TASKioscos
	UPDATE TASKioscos SET bac_uni = 2 WHERE KioscoId in (125,154);
	UPDATE TASKioscos SET bac_uni = 2 WHERE DireccionIP = '10.99.101.212';
	UPDATE TASKioscos SET bac_uni = 2 WHERE DireccionIP = '10.0.2.15';
	
GO



