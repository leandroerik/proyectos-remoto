USE [Homebanking]
GO

IF NOT EXISTS(SELECT 1 FROM dbo.parametria_tipificaciones WHERE tipificacion = 'LIBERACI&Oacute;N DE HIPOTECA AUTOMATIZADA')
BEGIN
	INSERT INTO dbo.parametria_tipificaciones (tipificacion, producto, tipo, fecha_ultima_modificacion, tipificacion_final, producto_final)
	VALUES( 'LIBERACI&Oacute;N DE HIPOTECA AUTOMATIZADA', 'PRESTAMO HIPOTECARIO', 'CLIENTE', GETDATE(), 'Pedido de Liberaci&oacute;n de Hipoteca', 'Pr&eacute;stamo Hipotecario' )
END

GO