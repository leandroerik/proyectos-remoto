USE [esales]
GO
 
SET ANSI_NULLS ON
GO
 
SET QUOTED_IDENTIFIER ON
GO

-- Creo tabla BB_DispositivoAdjust
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BB_DispositivoAdjust')
BEGIN
	CREATE TABLE BB_DispositivoAdjust (
		IdDispositivoAdjust BIGINT IDENTITY(1,1) PRIMARY KEY,
		adid VARCHAR(50)
	);
END

-- Creo indice unico para adid en BB_DispositivoAdjust
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_IdDispositivoAdjust' AND object_id = OBJECT_ID('BB_DispositivoAdjust'))
BEGIN
	CREATE UNIQUE INDEX IX_IdDispositivoAdjust ON BB_DispositivoAdjust(adid);
END

-- Dropeo la tabla para crearla si existe
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BB_EventoAdjust')
BEGIN
	DROP TABLE BB_EventoAdjust
END

-- Creo la tabla BB_EventoAdjust
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BB_EventoAdjust')
BEGIN
	CREATE TABLE BB_EventoAdjust (
		IdEventoAdjust BIGINT IDENTITY(1,1) PRIMARY KEY,
		IdDispositivoAdjust BIGINT,
		Clave VARCHAR(50),
		Valor VARCHAR(150),
		Fecha DATETIME,
		CONSTRAINT FK_IdDipositivoAdjust FOREIGN KEY (IdDispositivoAdjust) REFERENCES BB_DispositivoAdjust(IdDispositivoAdjust)
	);
END
GO

-- Elimino indice IX_IdDsiposistivoAdjust, mal tipeado y mal creado si existiera
IF EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_IdDsiposistivoAdjust' AND object_id = OBJECT_ID('BB_EventoAdjust'))
BEGIN
	DROP INDEX IX_IdDsiposistivoAdjust ON BB_EventoAdjust;
END

-- Creo indice no agrupado para IdDispositivoAdjust en BB_EventoAdjust
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_IdDispositivoAdjust' AND object_id = OBJECT_ID('BB_EventoAdjust'))
BEGIN
	CREATE NONCLUSTERED INDEX IX_IdDispositivoAdjust ON BB_EventoAdjust (IdDispositivoAdjust);
END

-- Creo indice no agrupado para Clave en BB_EventoAdjust 
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Clave' AND object_id = OBJECT_ID('BB_EventoAdjust'))
BEGIN
	CREATE NONCLUSTERED INDEX IX_Clave ON BB_EventoAdjust(Clave);
END

-- Dropeo SP BB_InsertarEventoAdjust si existiera
IF EXISTS(SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[BB_InsertarEventoAdjust]') AND type IN (N'P', N'PC'))
BEGIN
	DROP PROCEDURE [dbo].[BB_InsertarEventoAdjust]
END
GO

-- Creo SP BB_InsertarEventoAdjust
CREATE PROCEDURE [dbo].[BB_InsertarEventoAdjust]
	@adid VARCHAR(50),
	@claves VARCHAR(1000),
	@valores VARCHAR(1000)
AS
BEGIN
	DECLARE @idDispositivoAdjust BIGINT;

	-- Buscar el IdDispositivoAdjust basado en el adid
	SELECT @idDispositivoAdjust = IdDispositivoAdjust
	FROM BB_DispositivoAdjust
	WHERE adid = @adid;

	-- Si no se encuentra el IdDisposistivoAdjust, insertarlo
	IF @idDispositivoAdjust IS NULL
	BEGIN
		INSERT INTO BB_DispositivoAdjust (adid)
		VALUES (@adid);

		-- Obtener el IdDispositivoAdjust recién insertado
		SET @idDispositivoAdjust = SCOPE_IDENTITY();
	END
	
	;WITH ClavesCTE AS (
		SELECT Valor, ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS RowNum
		FROM dbo.SplitString2(@claves, '|')
	),
	ValoresCTE AS (
		SELECT Valor, ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS RowNum
		FROM dbo.SplitString2(@valores, '|')
	)
	
	-- Insertar los eventos en la tabla EventoAdjust
	INSERT INTO BB_EventoAdjust (IdDispositivoAdjust, Clave, Valor, Fecha)
	SELECT @idDispositivoAdjust, c.Valor AS Clave, v.Valor AS Valor, GETDATE() AS Fecha
	FROM ClavesCTE c
	JOIN ValoresCTE v ON c.RowNum = v.RowNum

	SELECT 'OK' as Resultado, 'Sin errores' as Detalle
END