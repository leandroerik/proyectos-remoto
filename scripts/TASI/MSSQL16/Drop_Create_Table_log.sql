USE [HipotecarioTAS]
GO

/****** Object:  Table [dbo].[log]    Script Date: 1/7/2024 17:31:55 ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[log]') AND type in (N'U'))
DROP TABLE [dbo].[log]
GO

/****** Object:  Table [dbo].[log]    Script Date: 1/7/2024 17:31:55 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[log](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[momento] [datetime] NOT NULL,
	[cobis] [bigint] NULL,
	[cuit] [bigint] NULL,
	[documento] [bigint] NULL,
	[kiosco_id] [bigint] NULL,
	[endpoint] [varchar](255) NULL,
	[evento] [varchar](50) NULL,
	[datos] [varchar](max) NULL,
	[error] [varchar](max) NULL,
	[idProceso] [int] NULL,
	[ip] [varchar](50) NULL,	
 CONSTRAINT [PK_log] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO