USE Hbs
GO
 
IF OBJECT_ID('agenda_transferencias', 'U') IS NOT NULL
	IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE COLUMN_NAME = 'apodo' AND TABLE_NAME = 'agenda_transferencias')
		BEGIN
			ALTER TABLE [dbo].[agenda_transferencias] ADD apodo VARCHAR(60) NULL
		END
GO 
