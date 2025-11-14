USE Mobile
GO

BEGIN
	ALTER TABLE [dbo].[pagoconpuntos_offer] ADD fecha_de_canje VARCHAR(255) NULL
END
GO