/*SQL_SERVER*/
USE [buhobank]
GO


IF OBJECT_ID('bb_puntos_venta', 'U') IS NOT NULL 
	DROP TABLE [dbo].[bb_puntos_venta]
GO 

CREATE TABLE [dbo].[bb_puntos_venta](
	[id] [int] PRIMARY KEY IDENTITY(1,1) NOT NULL,
	[punto_venta][varchar](60) NOT NULL,
	[imagen][VARCHAR](MAX) NULL,
	[texto][VARCHAR](MAX) NULL,
	[fecha_ultima_modificacion] DATETIME NOT NULL
)
GO

INSERT INTO [dbo].[bb_puntos_venta] ([punto_venta], [imagen], [texto], [fecha_ultima_modificacion]) VALUES 
(
	'pintureria_rex',
	'https://www.hipotecario.com.ar/media/buhobank/logos/Logo_rex.png',
	'Pinturerias Rex',
	GETDATE()
)
GO