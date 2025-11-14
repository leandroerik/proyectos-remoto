/*SQL_SERVER*/
USE buhobank
GO


IF OBJECT_ID('bb_scores', 'U') IS NOT NULL 
	DROP TABLE [dbo].[bb_scores]
GO

CREATE TABLE [dbo].[bb_scores](

	[id] [int] PRIMARY KEY IDENTITY(1,1) NOT NULL,
	[id_cobis][varchar](30) NOT NULL,
	[cuil][varchar](30) NOT NULL,
	[resolucion][varchar](50) NULL,
	[letra_tc][varchar](50) NOT NULL,
	[resultado][varchar](50) NOT NULL,
	[fecha_ultima_modificacion] DATETIME NOT NULL
)
GO

