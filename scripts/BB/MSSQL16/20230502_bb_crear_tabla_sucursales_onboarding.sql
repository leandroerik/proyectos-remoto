/*SQL_SERVER*/
USE esales
GO 


IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE COLUMN_NAME = 'punto_venta' AND TABLE_NAME = 'sucursales_onboarding')
BEGIN
  ALTER TABLE [dbo].[sucursales_onboarding] ADD punto_venta [varchar](255) NULL
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE COLUMN_NAME = 'punto_venta_img' AND TABLE_NAME = 'sucursales_onboarding')
BEGIN
  ALTER TABLE [dbo].[sucursales_onboarding] ADD punto_venta_img [varchar](255) NULL
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE COLUMN_NAME = 'punto_venta_texto' AND TABLE_NAME = 'sucursales_onboarding')
BEGIN
  ALTER TABLE [dbo].[sucursales_onboarding] ADD punto_venta_texto [varchar](255) NULL
END
GO
