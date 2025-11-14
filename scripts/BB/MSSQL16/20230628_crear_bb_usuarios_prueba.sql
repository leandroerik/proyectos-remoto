USE [esales]
GO


IF OBJECT_ID('bb_usuarios_prueba', 'U') IS NOT NULL 
	DROP TABLE [dbo].[bb_usuarios_prueba]
GO

CREATE TABLE [dbo].[bb_usuarios_prueba](
	[id] [int] PRIMARY KEY IDENTITY(1,1) NOT NULL,
	[cuil][varchar](60) NOT NULL,
	[ejecutar_motor]BIT NOT NULL,
	[oferta_motor][VARCHAR](60) NULL,
	[es_virtual]BIT NOT NULL,
	[retomar_sesion]BIT NOT NULL,
	[finalizar]BIT NOT NULL,
	[habilitado]BIT NOT NULL,
	[fecha_ultima_modificacion] DATETIME NOT NULL
)
GO



