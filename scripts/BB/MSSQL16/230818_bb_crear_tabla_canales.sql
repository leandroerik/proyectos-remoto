USE buhobank
GO

--bb_flujos
IF EXISTS (
	SELECT CONSTRAINT_NAME, CONSTRAINT_TYPE 
	FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
	WHERE TABLE_NAME = 'bb_flujos' 
	AND CONSTRAINT_TYPE = 'FOREIGN KEY'
	AND CONSTRAINT_NAME = 'FK_canales_flujo'
)
BEGIN 
	ALTER TABLE [dbo].[bb_flujos]
	DROP CONSTRAINT FK_canales_flujo
END
GO

IF OBJECT_ID('bb_canales', 'U') IS NOT NULL 
	DROP TABLE [dbo].[bb_canales]
GO

CREATE TABLE [dbo].[bb_canales](

	[id][int] PRIMARY KEY IDENTITY(1,1) NOT NULL,
	[descripcion][varchar](60) NULL,
	[usuario][varchar](60) NOT NULL,
	[canal][varchar](60) NOT NULL,
	[subcanal_venta][varchar](60) NOT NULL,
	[canal_venta1][varchar](60) NOT NULL,
	[canal_venta2][varchar](60) NOT NULL,
	[canal_venta3][varchar](60) NOT NULL,
	[canal_venta4][varchar](60) NOT NULL,
	[canal_originacion1][varchar](60) NOT NULL,
	[canal_originacion2][varchar](60) NOT NULL,
	[canal_originacion3][varchar](60) NOT NULL,
	[fecha_ultima_modificacion] datetime NOT NULL
)
GO

IF @@SERVERNAME NOT LIKE '%PRODMSSQL16'
BEGIN
	INSERT INTO	[dbo].[bb_canales]
	(
		[descripcion],
		[usuario],
		[canal],
		[subcanal_venta],
		[canal_venta1],
		[canal_venta2],
		[canal_venta3],
		[canal_venta4],
		[canal_originacion1],
		[canal_originacion2],
		[canal_originacion3],
		[fecha_ultima_modificacion]
	)
	VALUES
	(
		'BB', --[descripcion]
		'ESACCS', --[usuario]
		'BB', --[canal]
		'1', --[subcanal_venta]
		'25', --[canal_venta1]
		'2211', --[canal_venta2]
		'2230', --[canal_venta3]
		'25001', --[canal_venta4]
		'40', --[canal_originacion1]
		'1063', --[canal_originacion2]
		'ESACCS', --[canal_originacion3]
		GETDATE()--[fecha_ultima_modificacion]
	),
	(
		'CH', --[descripcion]
		'ESACCS', --[usuario]
		'BB', --[canal]
		'1', --[subcanal_venta]
		'25', --[canal_venta1]
		'2211', --[canal_venta2]
		'2230', --[canal_venta3]
		'25001', --[canal_venta4]
		'40', --[canal_originacion1]
		'1063', --[canal_originacion2]
		'ESACCS', --[canal_originacion3]
		GETDATE()--[fecha_ultima_modificacion]
	)
END
GO

IF @@SERVERNAME LIKE '%PRODMSSQL16'
BEGIN
	INSERT INTO	[dbo].[bb_canales]
	(
		[descripcion],
		[usuario],
		[canal],
		[subcanal_venta],
		[canal_venta1],
		[canal_venta2],
		[canal_venta3],
		[canal_venta4],
		[canal_originacion1],
		[canal_originacion2],
		[canal_originacion3],
		[fecha_ultima_modificacion]
	)
	VALUES
	(
		'BB', --[descripcion]
		'ESAPRD', --[usuario]
		'BB', --[canal]
		'1', --[subcanal_venta]
		'25', --[canal_venta1]
		'2211', --[canal_venta2]
		'2230', --[canal_venta3]
		'25001', --[canal_venta4]
		'40', --[canal_originacion1]
		'1063', --[canal_originacion2]
		'ESAPRD', --[canal_originacion3]
		GETDATE()--[fecha_ultima_modificacion]
	),
	(
		'CH', --[descripcion]
		'ESAPRD', --[usuario]
		'BB', --[canal]
		'1', --[subcanal_venta]
		'25', --[canal_venta1]
		'2211', --[canal_venta2]
		'2230', --[canal_venta3]
		'25001', --[canal_venta4]
		'40', --[canal_originacion1]
		'1063', --[canal_originacion2]
		'ESAPRD', --[canal_originacion3]
		GETDATE()--[fecha_ultima_modificacion]
	)
END
GO

--bb_flujos
IF OBJECT_ID('bb_flujos', 'U') IS NOT NULL 
	ALTER TABLE [dbo].[bb_flujos] ADD CONSTRAINT FK_canales_flujo
	FOREIGN KEY(id_canal) REFERENCES [dbo].[bb_canales](id)
	ON DELETE CASCADE ON UPDATE CASCADE	
GO