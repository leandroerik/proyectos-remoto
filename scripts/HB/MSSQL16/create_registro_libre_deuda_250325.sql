USE homebanking
GO

IF OBJECT_ID('parametria_tipificaciones', 'U') IS NOT NULL
	UPDATE parametria_tipificaciones SET tipificacion_final = 'Libre Deuda', producto_final = 'Documentaci&oacute;n' 
	WHERE tipificacion = 'LIBRE DEUDA AUTOMATIZADO'
GO
