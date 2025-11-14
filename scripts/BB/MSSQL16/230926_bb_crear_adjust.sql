/*SQL_SERVER*/
USE [buhobank]
GO


IF OBJECT_ID('adjust', 'U') IS NOT NULL 
	DROP TABLE [dbo].[adjust]

GO
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[adjust](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[gps_adid] [varchar](1000) NOT NULL,
	[idfa] [varchar](1000) NOT NULL,
	[idfv] [varchar](1000) NOT NULL,
	[adid] [varchar](1000) NOT NULL,
	[app_name] [varchar](1000) NOT NULL,
	[event_name] [varchar](1000) NOT NULL,
	[tracker_token] [varchar](1000) NOT NULL,
	[tracker_name] [varchar](1000) NOT NULL,
	[activity_kind] [varchar](1000) NOT NULL,
	[created_at] [datetime] NULL,
	[event_token] [varchar](1000) NOT NULL,
 CONSTRAINT [PK_adjust] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO