USE [Homebanking]
GO
BEGIN TRANSACTION;
ALTER TABLE homebanking.[dbo].[parametria_tipificaciones]
ALTER COLUMN [tipificacion_final] VARCHAR(70)
USE [Homebanking]
GO