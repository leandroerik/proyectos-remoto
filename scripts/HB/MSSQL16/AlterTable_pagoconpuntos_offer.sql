USE [Mobile]
IF EXISTS (SELECT * FROM sys.columns 
           WHERE Name = N'numero_caso_crm' AND Object_ID = Object_ID(N'Mobile.dbo.pagoconpuntos_offer'))
AND EXISTS (SELECT * FROM sys.columns 
            WHERE Name = N'estado_crm' AND Object_ID = Object_ID(N'Mobile.dbo.pagoconpuntos_offer'))
BEGIN
ALTER TABLE Mobile.[dbo].[pagoconpuntos_offer]
DROP COLUMN [estado_crm], [numero_caso_crm]
END