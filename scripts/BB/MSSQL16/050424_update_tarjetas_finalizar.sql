USE buhobank
GO

IF OBJECT_ID('bb_tarjetas_finalizar', 'U') IS NOT NULL 
	DROP TABLE [dbo].[bb_tarjetas_finalizar]
GO

CREATE TABLE [dbo].[bb_tarjetas_finalizar](

	[id] [int] PRIMARY KEY IDENTITY(1,1) NOT NULL,
	[id_tarjeta][int] NOT NULL,
	[tipo][varchar](150) NOT NULL,
	[subtipo][varchar](150) NOT NULL,
	[titulo][varchar](255) NULL,
	[texto][varchar](255) NULL,
	[logo][varchar](255) NULL,
	[fecha_ultima_modificacion] datetime NOT NULL
)
GO


IF OBJECT_ID('bb_tarjetas', 'U') IS NOT NULL 
	ALTER TABLE [dbo].[bb_tarjetas_finalizar] ADD CONSTRAINT FK_finalizar_tarjetas
	FOREIGN KEY(id_tarjeta) REFERENCES [dbo].[bb_tarjetas](id)
	ON DELETE CASCADE ON UPDATE CASCADE
GO


INSERT INTO	[dbo].[bb_tarjetas_finalizar]
(
	[id_tarjeta],
	[tipo],
	[subtipo],
	[titulo],
	[texto],
	[logo],
	[fecha_ultima_modificacion]
)
VALUES 

--FLUJO_ONBOARDING
--BUHO INICIA + VIRTUAL
(
	1, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo]
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos todos los días las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	1, --[id_tarjeta]
	'item', --[tipo]
	'caja_inversor', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	1, --[id_tarjeta]
	'item', --[tipo]
	'virtual', --[subtipo]
	'Tu Tarjeta de Débito Virtual está lista', --[titulo]
	'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/credit-card-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	1, --[id_tarjeta]
	'item', --[tipo]
	'tarjetaFisica', --[subtipo],
	'Estamos preparando el envío de tu tarjeta fisica', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	1, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--BUHO INICIA
(
	2, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo]
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos todos los días las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	2, --[id_tarjeta]
	'item', --[tipo]
	'caja_inversor', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	2, --[id_tarjeta]
	'item', --[tipo]
	'tarjetaFisica', --[subtipo],
	'Estamos preparando el envío de tu tarjeta fisica', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	2, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--INTERNACIONAL PACK + VIRTUAL
(
	3, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	3, --[id_tarjeta]
	'item', --[tipo]
	'virtual', --[subtipo]
	'Tu Tarjeta de Débito Virtual está lista', --[titulo]
	'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/credit-card-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	3, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Internacional Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	3, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--INTERNACIONAL PACK
(
	4, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	4, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Internacional Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	4, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--GOLD + VIRTUAL
(
	5, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	5, --[id_tarjeta]
	'item', --[tipo]
	'virtual', --[subtipo]
	'Tu Tarjeta de Débito Virtual está lista', --[titulo]
	'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/credit-card-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	5, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Gold Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	5, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--GOLD
(
	6, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	6, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Gold Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	6, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--PLATINUM + VIRTUAL
(
	7, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	7, --[id_tarjeta]
	'item', --[tipo]
	'virtual', --[subtipo]
	'Tu Tarjeta de Débito Virtual está lista', --[titulo]
	'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/credit-card-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	7, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Platinum Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	7, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--PLATINUM
(
	8, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	8, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Platinum Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	8, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--BLACK + VIRTUAL
(
	9, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	9, --[id_tarjeta]
	'item', --[tipo]
	'virtual', --[subtipo]
	'Tu Tarjeta de Débito Virtual está lista', --[titulo]
	'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/credit-card-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	9, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Black Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	9, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--BLACK
(
	10, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	10, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Black Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	10, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--EMPRENDEDOR INTERNACIONAL
(
	11, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	11, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Internacional Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	11, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--EMPRENDEDOR INTERNACIONAL + VIRTUAL
(
	22, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	22, --[id_tarjeta]
	'item', --[tipo]
	'virtual', --[subtipo]
	'Tu Tarjeta de Débito Virtual está lista', --[titulo]
	'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/credit-card-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	22, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Internacional Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	22, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--EMPRENDEDOR GOLD
(
	12, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	12, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Internacional Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	12, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--EMPRENDEDOR GOLD + VIRTUAL
(
	23, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	23, --[id_tarjeta]
	'item', --[tipo]
	'virtual', --[subtipo]
	'Tu Tarjeta de Débito Virtual está lista', --[titulo]
	'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/credit-card-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	23, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Gold Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	23, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--EMPRENDEDOR PLATINUM
(
	13, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	13, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Platinum Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	13, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--EMPRENDEDOR PLATINUM + VIRTUAL
(
	24, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	24, --[id_tarjeta]
	'item', --[tipo]
	'virtual', --[subtipo]
	'Tu Tarjeta de Débito Virtual está lista', --[titulo]
	'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/credit-card-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	24, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Platinum Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	24, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--EMPRENDEDOR BLACK
(
	14, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	14, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Black Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	14, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--EMPRENDEDOR BLACK + VIRTUAL
(
	25, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	25, --[id_tarjeta]
	'item', --[tipo]
	'virtual', --[subtipo]
	'Tu Tarjeta de Débito Virtual está lista', --[titulo]
	'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/credit-card-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	25, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Black Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	25, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
--FLUJO_LIBERTAD
--BUHO INICIA LIBERTAD
(
	15, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo]
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos todos los días las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-ahorro.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	15, --[id_tarjeta]
	'item', --[tipo]
	'caja_inversor', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-ahorro.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	15, --[id_tarjeta]
	'item', --[tipo]
	'tarjetaFisica', --[subtipo],
	'Estamos preparando el envío de tu tarjeta fisica', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	15, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--BUHO INICIA LIBERTAD + VIRTUAL
(
	26, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo]
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos todos los días las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-ahorro.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	26, --[id_tarjeta]
	'item', --[tipo]
	'caja_inversor', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-ahorro.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	26, --[id_tarjeta]
	'item', --[tipo]
	'virtual', --[subtipo]
	'Tu Tarjeta de Débito Virtual está lista', --[titulo]
	'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	26, --[id_tarjeta]
	'item', --[tipo]
	'tarjetaFisica', --[subtipo],
	'Estamos preparando el envío de tu tarjeta fisica', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	26, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--INTERNACINAL LIBERTAD
(
	16, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-ahorro.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	16, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Libertad Internacional Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	16, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--INTERNACIONAL LIBERTAD + VIRTUAL
(
	27, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-ahorro.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	27, --[id_tarjeta]
	'item', --[tipo]
	'virtual', --[subtipo]
	'Tu Tarjeta de Débito Virtual está lista', --[titulo]
	'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	27, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Libertad Internacional Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	27, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--GOLD LIBERTAD
(
	17, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-ahorro.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	17, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Libertad Gold Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	17, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--GOLD LIBERTAD + VIRTUAL
(
	28, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-ahorro.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	28, --[id_tarjeta]
	'item', --[tipo]
	'virtual', --[subtipo]
	'Tu Tarjeta de Débito Virtual está lista', --[titulo]
	'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	28, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Libertad Gold Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	28, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--PLATINUM LIBERTAD
(
	18, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-ahorro.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	18, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Libertad Platinum Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	18, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--PLATINUM LIBERTAD + VIRTUAL
(
	29, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-ahorro.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	29, --[id_tarjeta]
	'item', --[tipo]
	'virtual', --[subtipo]
	'Tu Tarjeta de Débito Virtual está lista', --[titulo]
	'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	29, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Libertad Platinum Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	29, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--BLACK LIBERTAD
(
	19, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-ahorro.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	19, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Libertad Black Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	19, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--BLACK LIBERTAD + VIRTUAL
(
	30, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo],
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos y dólares las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-ahorro.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	30, --[id_tarjeta]
	'item', --[tipo]
	'virtual', --[subtipo]
	'Tu Tarjeta de Débito Virtual está lista', --[titulo]
	'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	30, --[id_tarjeta]
	'item', --[tipo]
	'tarjeta', --[subtipo],
	'Estamos preparando el envío de las tarjetas de tu Libertad Black Pack', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	30, --[id_tarjeta]
	'item', --[tipo]
	'inversor', --[subtipo],
	'Tu Cuenta Inversor está en proceso', --[titulo]
	'Te avisaremos cuando esté disponible para que puedas empezar a invertir.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--CGU (FLUJO_ONBOARDING)
(
	20, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo]
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos todos los días las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	20, --[id_tarjeta]
	'item', --[tipo]
	'tarjetaFisica', --[subtipo],
	'Estamos preparando el envío de tu tarjeta fisica', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),

--CGU + VIRTUAL (FLUJO_ONBOARDING)
(
	21, --[id_tarjeta]
	'item', --[tipo]
	'caja', --[subtipo]
	'Ya contás con una caja de ahorro', --[titulo]
	'Podés operar en pesos todos los días las 24 hs.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/icono_caja.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	21, --[id_tarjeta]
	'item', --[tipo]
	'virtual', --[subtipo]
	'Tu Tarjeta de Débito Virtual está lista', --[titulo]
	'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/credit-card-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
),
(
	21, --[id_tarjeta]
	'item', --[tipo]
	'tarjetaFisica', --[subtipo],
	'Estamos preparando el envío de tu tarjeta fisica', --[titulo]
	'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
	'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
	GETDATE()--[fecha_ultima_modificacion]
)
GO

--FLUJO_INVERSIONES
IF @@SERVERNAME LIKE '%HOMOMSSQL16'
BEGIN
	INSERT INTO	[dbo].[bb_tarjetas_finalizar]
	(
		[id_tarjeta],
		[tipo],
		[subtipo],
		[titulo],
		[texto],
		[logo],
		[fecha_ultima_modificacion]
	)
	VALUES 
	--BUHO INICIA + VIRTUAL
	(
		191, --[id_tarjeta]
		'item', --[tipo]
		'inversor', --[subtipo]
		'Tu Cuenta Inversor está lista', --[titulo]
		'Ya podés empezar a invertir, solo tenes que ingresar dinero en tu cuenta.', --[texto]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[logo]
		GETDATE()--[fecha_ultima_modificacion]
	),
	(
		191, --[id_tarjeta]
		'item', --[tipo]
		'virtual', --[subtipo]
		'Tu Tarjeta de Débito Virtual está lista', --[titulo]
		'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
		'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
		GETDATE()--[fecha_ultima_modificacion]
	),
	(
		191, --[id_tarjeta]
		'item', --[tipo]
		'tarjetaFisica', --[subtipo],
		'Estamos preparando el envío de tu tarjeta fisica', --[titulo]
		'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
		'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
		GETDATE()--[fecha_ultima_modificacion]
	),

	--BUHO INICIA
	(
		192, --[id_tarjeta]
		'item', --[tipo]
		'inversor', --[subtipo]
		'Tu Cuenta Inversor está lista', --[titulo]
		'Ya podés empezar a invertir, solo tenes que ingresar dinero en tu cuenta.', --[texto]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[logo]
		GETDATE()--[fecha_ultima_modificacion]
	),
	(
		192, --[id_tarjeta]
		'item', --[tipo]
		'tarjetaFisica', --[subtipo],
		'Estamos preparando el envío de tu tarjeta fisica', --[titulo]
		'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
		'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
		GETDATE()--[fecha_ultima_modificacion]
	)
END
GO

--FLUJO_INVERSIONES
IF @@SERVERNAME LIKE '%PRODMSSQL16'
BEGIN
	INSERT INTO	[dbo].[bb_tarjetas_finalizar]
	(
		[id_tarjeta],
		[tipo],
		[subtipo],
		[titulo],
		[texto],
		[logo],
		[fecha_ultima_modificacion]
	)
	VALUES 
	--BUHO INICIA + VIRTUAL
	(
		32, --[id_tarjeta]
		'item', --[tipo]
		'inversor', --[subtipo]
		'Tu Cuenta Inversor está lista', --[titulo]
		'Ya podés empezar a invertir, solo tenes que ingresar dinero en tu cuenta.', --[texto]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[logo]
		GETDATE()--[fecha_ultima_modificacion]
	),
	(
		32, --[id_tarjeta]
		'item', --[tipo]
		'virtual', --[subtipo]
		'Tu Tarjeta de Débito Virtual está lista', --[titulo]
		'Recordá tener dinero en tu cuenta para empezar a usarla.', --[texto]
		'https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png', --[logo]
		GETDATE()--[fecha_ultima_modificacion]
	),
	(
		32, --[id_tarjeta]
		'item', --[tipo]
		'tarjetaFisica', --[subtipo],
		'Estamos preparando el envío de tu tarjeta fisica', --[titulo]
		'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
		'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
		GETDATE()--[fecha_ultima_modificacion]
	),

	--BUHO INICIA
	(
		33, --[id_tarjeta]
		'item', --[tipo]
		'inversor', --[subtipo]
		'Tu Cuenta Inversor está lista', --[titulo]
		'Ya podés empezar a invertir, solo tenes que ingresar dinero en tu cuenta.', --[texto]
		'https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png', --[logo]
		GETDATE()--[fecha_ultima_modificacion]
	),
	(
		33, --[id_tarjeta]
		'item', --[tipo]
		'tarjetaFisica', --[subtipo],
		'Estamos preparando el envío de tu tarjeta fisica', --[titulo]
		'Llegará en un máximo de 20 días hábiles al domicilio que elegiste.', --[texto]
		'https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png', --[logo]
		GETDATE()--[fecha_ultima_modificacion]
	)
END
GO