USE [Mobile]
GO

IF NOT EXISTS (
    SELECT * 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'pagoconpuntos_offer' 
      AND TABLE_SCHEMA = 'dbo' 
      AND COLUMN_NAME = 'estado_crm'
)
BEGIN
    ALTER TABLE [Mobile].[dbo].[pagoconpuntos_offer]
    ADD estado_crm VARCHAR(1)
END
GO

IF NOT EXISTS (
    SELECT * 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'pagoconpuntos_offer' 
      AND TABLE_SCHEMA = 'dbo' 
      AND COLUMN_NAME = 'numero_caso_crm'
)
BEGIN
    ALTER TABLE [Mobile].[dbo].[pagoconpuntos_offer]
    ADD numero_caso_crm VARCHAR(50)
END
GO