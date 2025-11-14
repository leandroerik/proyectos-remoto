USE buhobank
GO

--FLUJO_INVERSIONES
IF @@SERVERNAME NOT LIKE '%PRODMSSQL16'
BEGIN 
	UPDATE bb_paquetes SET numero_paquete = '4'
	WHERE id_plantilla_flujo = 11 AND numero_paquete = '0'
END
GO

--FLUJO_INVERSIONES
IF @@SERVERNAME LIKE '%PRODMSSQL16'
BEGIN 
	UPDATE bb_paquetes SET numero_paquete = '4'
	WHERE id_plantilla_flujo = 3 AND numero_paquete = '0'
END
GO
