USE [HipotecarioTAS]
GO

/****** Object:  Table [dbo].[RWlog]    Script Date: 25/11/2024 10:41:11 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.objects
    WHERE object_id = OBJECT_ID(N'[dbo].[RWlog]')
          AND type = N'U'
)
BEGIN
CREATE TABLE [dbo].[RWlog](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[fecha] [datetime] NOT NULL,
	[usuario] [varchar](40) NULL,
	[endpoint] [varchar](255) NULL,
	[evento] [varchar](50) NULL,
	[datos] [varchar](max) NULL,
	[error] [varchar](max) NULL,
	[ip] [varchar](50) NULL,
 CONSTRAINT [PK_RWlog] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
END


