/*SQL_SERVER*/
USE buhobank
GO


IF OBJECT_ID('bb_log_crm', 'U') IS NULL 
	
	CREATE TABLE [dbo].[bb_log_crm](
		[id][int] PRIMARY KEY IDENTITY(1,1) NOT NULL,
		[fecha] DATETIME NOT NULL,
		[token_sesion][varchar](MAX) NOT NULL,
		[cuil][varchar](30) NOT NULL,
		[id_solicitud][varchar](50) NULL,
		[evento][varchar](MAX) NULL,
		[error][varchar](MAX) NULL,
		[datos][varchar](MAX) NULL,
		[datos_adicionales][varchar](MAX) NULL,
		[ip][varchar](50) NULL
	)
GO
