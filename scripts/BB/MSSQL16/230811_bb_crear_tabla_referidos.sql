USE buhobank
GO

IF OBJECT_ID('bb_referidos', 'U') IS NOT NULL 
	DROP TABLE [dbo].[bb_referidos]
GO

CREATE TABLE [dbo].[bb_referidos](
	[id][int] PRIMARY KEY IDENTITY(1,1) NOT NULL,
	[cuil_referente][varchar](30) NOT NULL,
	[cuil_referido][varchar](30) NOT NULL,
	[estado][varchar](255) NOT NULL,
	[beneficio][varchar](255) NULL,
	[datos][varchar](MAX) NULL,
	[fecha_ultima_modificacion] datetime NOT NULL
)
GO



