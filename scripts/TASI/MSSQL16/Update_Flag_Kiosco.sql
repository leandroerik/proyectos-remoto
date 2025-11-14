/*SQL_SERVER*/
USE hipotecarioTAS
GO

-- Actualiza TASKioscos
	UPDATE TASKioscos SET bac_uni = 3 WHERE KioscoId in (125,154);
	
GO



