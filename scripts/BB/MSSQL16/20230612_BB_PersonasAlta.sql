USE [esales]
GO

/****** Si la tabla no existe se crea ******/
IF NOT EXISTS (SELECT * 
                   FROM sys.objects 
                     WHERE object_id = OBJECT_ID(N'[dbo].[BB_PersonasAlta]') AND type in (N'U'))
BEGIN
CREATE TABLE [dbo].[BB_PersonasAlta](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[cuil] [varchar](64) NULL,
	[estado] [varchar](255) NULL,
	[algoritmo] [varchar](255) NULL,
	[digitos] [varchar](255) NULL,
	[periodo] [varchar](255) NULL,
	[claveSecreta] [varchar](255) NULL,
	[urlClaveSecreta] [varchar](255) NULL,
	[username] [varchar](255) NULL,
	[id_dispositivo] [varchar](255) NULL,
	[sesion_id] [int] NOT NULL,
	[fecha_ultima_modificacion] [datetime] NULL,

PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

ALTER TABLE [dbo].[BB_PersonasAlta]  WITH CHECK ADD FOREIGN KEY([sesion_id])
REFERENCES [dbo].[Sesion] ([id])

END

