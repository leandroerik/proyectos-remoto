USE buhobank
GO

DELETE [dbo].[bb_contenidos_dinamico]
WHERE tipo = 'tipo_tarjeta'
GO

INSERT INTO	[dbo].[bb_contenidos_dinamico]
(
	[id_plantilla_flujo],
	[tipo],
	[titulo],
	[texto_legales],
	[habilitado],
	[fecha_ultima_modificacion]
)
VALUES
(
	1, --[id_plantilla_flujo]
	'tipo_tarjeta', --[tipo]
	'<p><b>¡Listo, ya tenés tu Tarjeta de Débito Virtual!</b></p>', --[titulo]
	'<p style=\"color:#F37320;\"><b>¿Cómo funciona?</b></p>|<p>Esta tarjeta te permite comprar en tiendas online, pagar suscripciones digitales y retirar efectivo en cajeros. <b>Funciona igual que una tarjeta física con el saldo de tu cuenta.</b></p>|<p style=\"color:#F37320;\"><b>¿Dónde la encuentro?</b></p>|<p>1. Ingresá a la <b>App BH</b></p><p>2. Desde el menu seleccioná <b>\"Tus Tarjetas\"</b></p><p>3. <b>¡Listo!</b>. Ya podés visualizar tu tarjeta y sus datos para operar</p>|<p style=\"color:#F37320;\"><b>¿Cómo empiezo a usarla?</b></p>|<p>A través de MODO desde la App BH para hacer <b>pagos con QR</b></p>|<p>Adherila a tus billeteras virtuales favoritas</p>', --[texto_legales]
	1, --[habilitado]
	GETDATE() --[fecha_ultima_modificacion]
),
(
	1, --[id_plantilla_flujo]
	'tipo_tarjeta', --[tipo]
	'<p><b>¿Querés pedir también tu tarjeta física?<br/><span style=\"color:#F37320;\">Es gratis y te llega a tu domicilio</span></b></p>', --[titulo]
	'<p>Si, pedir la física también</p>|<p>No, gracias</p>', --[texto_legales]
	1, --[habilitado]
	GETDATE() --[fecha_ultima_modificacion]
),
(
	2, --[id_plantilla_flujo]
	'tipo_tarjeta', --[tipo]
	'<p><b>¡Listo, ya tenés tu Tarjeta de Débito Virtual!</b></p>', --[titulo]
	'<p style=\"color:#E3001B;\"><b>¿Cómo funciona?</b></p>|<p>Esta tarjeta te permite comprar en tiendas online, pagar suscripciones digitales y retirar efectivo en cajeros. <b>Funciona igual que una tarjeta física con el saldo de tu cuenta.</b></p>|<p style=\"color:#E3001B;\"><b>¿Dónde la encuentro?</b></p>|<p>1. Ingresá a la <b>App BH</b></p><p>2. Desde el menu seleccioná <b>\"Tus Tarjetas\"</b></p><p>3. <b>¡Listo!</b>. Ya podés visualizar tu tarjeta y sus datos para operar</p>|<p style=\"color:#E3001B;\"><b>¿Cómo empiezo a usarla?</b></p>|<p>A través de MODO desde la App BH para hacer <b>pagos con QR</b></p>|<p>Adherila a tus billeteras virtuales favoritas</p>', --[texto_legales]
	1, --[habilitado]
	GETDATE() --[fecha_ultima_modificacion]
),
(
	2, --[id_plantilla_flujo]
	'tipo_tarjeta', --[tipo]
	'<p><b>¿Querés pedir también tu tarjeta física?<br/><span style=\"color:#E3001B;\">Es gratis y te llega a tu domicilio</span></b></p>', --[titulo]
	'<p>Si, pedir la física también</p>|<p>No, gracias</p>', --[texto_legales]
	1, --[habilitado]
	GETDATE() --[fecha_ultima_modificacion]
)
GO

IF @@SERVERNAME LIKE '%HOMOMSSQL16'
BEGIN
	INSERT INTO	[dbo].[bb_contenidos_dinamico]
	(
		[id_plantilla_flujo],
		[tipo],
		[titulo],
		[texto_legales],
		[habilitado],
		[fecha_ultima_modificacion]
	)
	VALUES
	(
        11, --[id_plantilla_flujo]
        'tipo_tarjeta', --[tipo]
        '<p><b>¡Listo, ya tenés tu Tarjeta de Débito Virtual!</b></p>', --[titulo]
        '<p style=\"color:#BBA675;\"><b>¿Cómo funciona?</b></p>|<p>Esta tarjeta te permite comprar en tiendas online, pagar suscripciones digitales y retirar efectivo en cajeros. <b>Funciona igual que una tarjeta física con el saldo de tu cuenta.</b></p>|<p style=\"color:#BBA675;\"><b>¿Dónde la encuentro?</b></p>|<p>1. Ingresá a la <b>App BH</b></p><p>2. Desde el menu seleccioná <b>\"Tus Tarjetas\"</b></p><p>3. <b>¡Listo!</b>. Ya podés visualizar tu tarjeta y sus datos para operar</p>|<p style=\"color:#BBA675;\"><b>¿Cómo empiezo a usarla?</b></p>|<p>A través de MODO desde la App BH para hacer <b>pagos con QR</b></p>|<p>Adherila a tus billeteras virtuales favoritas</p>', --[texto_legales]
        1, --[habilitado]
        GETDATE() --[fecha_ultima_modificacion]
    ),
    (
        11, --[id_plantilla_flujo]
        'tipo_tarjeta', --[tipo]
        '<p><b>¿Querés pedir también tu tarjeta física?<br/><span style=\"color:#BBA675;\">Es gratis y te llega a tu domicilio</span></b></p>', --[titulo]
        '<p>Si, pedir la física también</p>|<p>No, gracias</p>', --[texto_legales]
        1, --[habilitado]
        GETDATE() --[fecha_ultima_modificacion]
    )
END
GO

--FLUJO_INVERSIONES
IF @@SERVERNAME LIKE '%PRODMSSQL16'
BEGIN
	INSERT INTO	[dbo].[bb_contenidos_dinamico]
	(
		[id_plantilla_flujo],
		[tipo],
		[titulo],
		[texto_legales],
		[habilitado],
		[fecha_ultima_modificacion]
	)
	VALUES
	(
        3, --[id_plantilla_flujo]
        'tipo_tarjeta', --[tipo]
        '<p><b>¡Listo, ya tenés tu Tarjeta de Débito Virtual!</b></p>', --[titulo]
        '<p style=\"color:#BBA675;\"><b>¿Cómo funciona?</b></p>|<p>Esta tarjeta te permite comprar en tiendas online, pagar suscripciones digitales y retirar efectivo en cajeros. <b>Funciona igual que una tarjeta física con el saldo de tu cuenta.</b></p>|<p style=\"color:#BBA675;\"><b>¿Dónde la encuentro?</b></p>|<p>1. Ingresá a la <b>App BH</b></p><p>2. Desde el menu seleccioná <b>\"Tus Tarjetas\"</b></p><p>3. <b>¡Listo!</b>. Ya podés visualizar tu tarjeta y sus datos para operar</p>|<p style=\"color:#BBA675;\"><b>¿Cómo empiezo a usarla?</b></p>|<p>A través de MODO desde la App BH para hacer <b>pagos con QR</b></p>|<p>Adherila a tus billeteras virtuales favoritas</p>', --[texto_legales]
        1, --[habilitado]
        GETDATE() --[fecha_ultima_modificacion]
    ),
    (
        3, --[id_plantilla_flujo]
        'tipo_tarjeta', --[tipo]
        '<p><b>¿Querés pedir también tu tarjeta física?<br/><span style=\"color:#BBA675;\">Es gratis y te llega a tu domicilio</span></b></p>', --[titulo]
        '<p>Si, pedir la física también</p>|<p>No, gracias</p>', --[texto_legales]
        1, --[habilitado]
        GETDATE() --[fecha_ultima_modificacion]
    )
END
GO