USE buhobank
GO

IF OBJECT_ID('bb_tarjetas_beneficio_adicionales', 'U') IS NOT NULL 
	DROP TABLE [dbo].[bb_tarjetas_beneficio_adicionales]
GO

CREATE TABLE [dbo].[bb_tarjetas_beneficio_adicionales](

	[id] [int] IDENTITY(1,1) NOT NULL,
	[id_tarjeta] [int] NOT NULL,
	[desc_beneficio] [varchar](256) NOT NULL,
	[desc_beneficio_html] [varchar](MAX) NOT NULL,
	[icono_id] [varchar](256) NOT NULL,
	[icono_desc] [varchar](256) NOT NULL,
	[prioridad] [int] NOT NULL,
	[fecha_ultima_modificacion] datetime NOT NULL
)
GO


IF OBJECT_ID('bb_tarjetas', 'U') IS NOT NULL 
	ALTER TABLE [dbo].[bb_tarjetas_beneficio_adicionales] ADD CONSTRAINT FK_beneficio_adicionales_tarjetas
	FOREIGN KEY(id_tarjeta) REFERENCES [dbo].[bb_tarjetas](id)
	ON DELETE CASCADE ON UPDATE CASCADE
GO


--FLUJO_INVERSIONES
IF @@SERVERNAME LIKE '%HOMOMSSQL16'
BEGIN
	INSERT INTO	[dbo].[bb_tarjetas_beneficio_adicionales]
	(
		[id_tarjeta],
		[desc_beneficio],
		[desc_beneficio_html],
		[icono_id],
		[icono_desc],
		[prioridad],
		[fecha_ultima_modificacion]
	)
	VALUES 
	(
		191, --[id_tarjeta]
		'Opera Dólar MEP, Bonos, Obligaciones Negociables, Acciones, Cedears, sin comisión por 1 año.', --[desc_beneficio]
		'Opera Dólar MEP, Bonos, Obligaciones Negociables, Acciones, Cedears, sin comisión por 1 año.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[icono_id]
		'Inversor', --[icono_desc]
		1, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		191, --[id_tarjeta]
		'Invertí en Fondos Comunes de Inversión las 24hs, los 7 días de la semana.', --[desc_beneficio]
		'Invertí en Fondos Comunes de Inversión las 24hs, los 7 días de la semana.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[icono_id]
		'Inversor', --[icono_desc]
		2, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		191, --[id_tarjeta]
		'Constituí Plazos fijos, compra y vende dólar oficial.', --[desc_beneficio]
		'Constituí Plazos fijos, compra y vende dólar oficial.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[icono_id]
		'Inversor', --[icono_desc]
		3, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		191, --[id_tarjeta]
		'Accedé a informes y charlas exclusivas de mercado, economía e inversiones.', --[desc_beneficio]
		'Accedé a informes y charlas exclusivas de mercado, economía e inversiones.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-computadora.png', --[icono_id]
		'Compu', --[icono_desc]
		4, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		191, --[id_tarjeta]
		'Promociones y descuentos en todo el país.', --[desc_beneficio]
		'Promociones y descuentos en todo el país.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-shop.png', --[icono_id]
		'Cartera', --[icono_desc]
		5, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		191, --[id_tarjeta]
		'Pone límites y mira tus consumos cuando quieras.', --[desc_beneficio]
		'Pone límites y mira tus consumos cuando quieras.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/1-icono-seguro.png', --[icono_id]
		'Seguro', --[icono_desc]
		6, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),

	(
		192, --[id_tarjeta]
		'Opera Dólar MEP, Bonos, Obligaciones Negociables, Acciones, Cedears, sin comisión por 1 año.', --[desc_beneficio]
		'Opera Dólar MEP, Bonos, Obligaciones Negociables, Acciones, Cedears, sin comisión por 1 año.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[icono_id]
		'Inversor', --[icono_desc]
		1, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		192, --[id_tarjeta]
		'Invertí en Fondos Comunes de Inversión las 24hs, los 7 días de la semana.', --[desc_beneficio]
		'Invertí en Fondos Comunes de Inversión las 24hs, los 7 días de la semana.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[icono_id]
		'Inversor', --[icono_desc]
		2, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		192, --[id_tarjeta]
		'Constituí Plazos fijos, compra y vende dólar oficial.', --[desc_beneficio]
		'Constituí Plazos fijos, compra y vende dólar oficial.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[icono_id]
		'Inversor', --[icono_desc]
		3, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		192, --[id_tarjeta]
		'Accedé a informes y charlas exclusivas de mercado, economía e inversiones.', --[desc_beneficio]
		'Accedé a informes y charlas exclusivas de mercado, economía e inversiones.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-computadora.png', --[icono_id]
		'Compu', --[icono_desc]
		4, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		192, --[id_tarjeta]
		'Promociones y descuentos en todo el país.', --[desc_beneficio]
		'Promociones y descuentos en todo el país.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-shop.png', --[icono_id]
		'Cartera', --[icono_desc]
		5, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		192, --[id_tarjeta]
		'Pone límites y mira tus consumos cuando quieras.', --[desc_beneficio]
		'Pone límites y mira tus consumos cuando quieras.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/1-icono-seguro.png', --[icono_id]
		'Seguro', --[icono_desc]
		6, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	)
END
GO


--FLUJO_INVERSIONES
IF @@SERVERNAME LIKE '%PRODMSSQL16'
BEGIN
	INSERT INTO	[dbo].[bb_tarjetas_beneficio_adicionales]
	(
		[id_tarjeta],
		[desc_beneficio],
		[desc_beneficio_html],
		[icono_id],
		[icono_desc],
		[prioridad],
		[fecha_ultima_modificacion]
	)
	VALUES 
	(
		32, --[id_tarjeta]
		'Opera Dólar MEP, Bonos, Obligaciones Negociables, Acciones, Cedears, sin comisión por 1 año.', --[desc_beneficio]
		'Opera Dólar MEP, Bonos, Obligaciones Negociables, Acciones, Cedears, sin comisión por 1 año.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[icono_id]
		'Inversor', --[icono_desc]
		1, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		32, --[id_tarjeta]
		'Invertí en Fondos Comunes de Inversión las 24hs, los 7 días de la semana.', --[desc_beneficio]
		'Invertí en Fondos Comunes de Inversión las 24hs, los 7 días de la semana.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[icono_id]
		'Inversor', --[icono_desc]
		2, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		32, --[id_tarjeta]
		'Constituí Plazos fijos, compra y vende dólar oficial.', --[desc_beneficio]
		'Constituí Plazos fijos, compra y vende dólar oficial.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[icono_id]
		'Inversor', --[icono_desc]
		3, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		32, --[id_tarjeta]
		'Accedé a informes y charlas exclusivas de mercado, economía e inversiones.', --[desc_beneficio]
		'Accedé a informes y charlas exclusivas de mercado, economía e inversiones.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-computadora.png', --[icono_id]
		'Compu', --[icono_desc]
		4, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		32, --[id_tarjeta]
		'Promociones y descuentos en todo el país.', --[desc_beneficio]
		'Promociones y descuentos en todo el país.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-shop.png', --[icono_id]
		'Cartera', --[icono_desc]
		5, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		32, --[id_tarjeta]
		'Pone límites y mira tus consumos cuando quieras.', --[desc_beneficio]
		'Pone límites y mira tus consumos cuando quieras.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/1-icono-seguro.png', --[icono_id]
		'Seguro', --[icono_desc]
		6, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),

	(
		33, --[id_tarjeta]
		'Opera Dólar MEP, Bonos, Obligaciones Negociables, Acciones, Cedears, sin comisión por 1 año.', --[desc_beneficio]
		'Opera Dólar MEP, Bonos, Obligaciones Negociables, Acciones, Cedears, sin comisión por 1 año.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[icono_id]
		'Inversor', --[icono_desc]
		1, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		33, --[id_tarjeta]
		'Invertí en Fondos Comunes de Inversión las 24hs, los 7 días de la semana.', --[desc_beneficio]
		'Invertí en Fondos Comunes de Inversión las 24hs, los 7 días de la semana.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[icono_id]
		'Inversor', --[icono_desc]
		2, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		33, --[id_tarjeta]
		'Constituí Plazos fijos, compra y vende dólar oficial.', --[desc_beneficio]
		'Constituí Plazos fijos, compra y vende dólar oficial.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[icono_id]
		'Inversor', --[icono_desc]
		3, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		33, --[id_tarjeta]
		'Accedé a informes y charlas exclusivas de mercado, economía e inversiones.', --[desc_beneficio]
		'Accedé a informes y charlas exclusivas de mercado, economía e inversiones.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-computadora.png', --[icono_id]
		'Compu', --[icono_desc]
		4, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		33, --[id_tarjeta]
		'Promociones y descuentos en todo el país.', --[desc_beneficio]
		'Promociones y descuentos en todo el país.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/icono-shop.png', --[icono_id]
		'Cartera', --[icono_desc]
		5, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	),
	(
		33, --[id_tarjeta]
		'Pone límites y mira tus consumos cuando quieras.', --[desc_beneficio]
		'Pone límites y mira tus consumos cuando quieras.', --[desc_beneficio_html]
		'https://www.hipotecario.com.ar/media/buhobank/1-icono-seguro.png', --[icono_id]
		'Seguro', --[icono_desc]
		6, --[prioridad]
		GETDATE() --[fecha_ultima_modificacion]
	)
END
GO
