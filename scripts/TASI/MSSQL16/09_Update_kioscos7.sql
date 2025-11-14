/*SQL_SERVER*/
USE hipotecarioTAS
GO
-- Actualiza todas las terminales
	UPDATE TASKioscos SET bac_uni= 7;
-- Actualiza TASKioscos
	UPDATE TASKioscos SET bac_uni = 7 WHERE DireccionIP = '10.99.101.212';
	UPDATE TASKioscos SET bac_uni = 7 WHERE DireccionIP = '10.0.2.15';
	
GO


