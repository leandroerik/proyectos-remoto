USE [esales]
GO

IF OBJECT_ID('Alerta_Push', 'U') IS NOT NULL 
	DROP TABLE [dbo].[Alerta_Push]
GO

CREATE TABLE [dbo].[Alerta_Push](
	[id] [int] PRIMARY KEY IDENTITY(1,1) NOT NULL,
	[codigoAlerta] [varchar](30) NOT NULL,
	[minutosDesdeAbandono] [int] NOT NULL,

	[ultimaEtapa] [varchar](60) NULL,
	[dependencia] [varchar](30) NULL,

	[titulo] [varchar](250) NULL,
	[texto] [varchar](250) NULL,
	[imagenUrl] [varchar](250) NULL,
	[url] [varchar](250) NULL,

	[horarioDesdePush] [varchar](2) NULL,
	[horarioHastaPush] [varchar](2) NULL,

	[plantillaMail] [varchar](250) NULL,
	[asuntoMail] [varchar](250) NULL,
	[horarioDesdeMail] [varchar](2) NULL,
	[horarioHastaMail] [varchar](2) NULL,
	
	[pushHabilitado] [bit] NOT NULL,
	[mailHabilitado] [bit] NOT NULL
)
GO

INSERT INTO	[dbo].[Alerta_Push]
(
	[codigoAlerta], [minutosDesdeAbandono], 
	[ultimaEtapa], [dependencia],
	[titulo], [texto], [url],
	[horarioDesdePush],[horarioHastaPush],
	[plantillaMail], [asuntoMail],
	[horarioDesdeMail], [horarioHastaMail],
	[pushHabilitado], [mailHabilitado]
)
VALUES 
(
	'FVUPRIMER',
	30, 
	'ETAPA_FLUJO_VU',
	NULL,
	'No te quedes sin tu tarjeta bonificada.', 
	'Volvé al registro y descubrí la oferta que el banco del hogar tiene para vos.', 
	NULL, --[url]
	NULL, --[horarioDesdePush]
	NULL, --[horarioHastaPush]
	'bb_alerta_abandono_general', --[plantillaMail]
	'BuhoBank - No te quedes sin tu tarjeta bonificada', --[asuntoMail]
	NULL, --[horarioDesdeMail]
	NULL, --[horarioHastaMail]
	1, --[pushHabilitado]
	1  --[mailHabilitado]
),
(
	'POSTOFERTAINICIA',
	30, 
	'ETAPA_POST_OFERTA', 
	NULL,
	'Tu tarjeta de débito te está esperando.', 
	'Ya casi es tuya. Completá el envío y recibila donde más te guste. Disfrutá las mejores promociones en todo el país.', 
	NULL, --[url]
	NULL, --[horarioDesdePush]
	NULL, --[horarioHastaPush]
	'bb_alerta_post_oferta_inicia', --[plantillaMail]
	'BuhoBank - Ya completaste casi todo el registro.', --[asuntoMail]
	NULL, --[horarioDesdeMail]
	NULL, --[horarioHastaMail]
	1, --[pushHabilitado]
	1  --[mailHabilitado]
),
(
	'POSTOFERTACREDITICIA',
	30, 
	'ETAPA_POST_OFERTA', 
	NULL,
	'Tu tarjeta de crédito bonificada te sigue esperando.', 
	'Ya casi es tuya. Completá el envío y recibila donde más te guste. Recodá que tenés 12 meses bonificados y las mejores promos en todo el país.', 
	NULL, --[url]
	NULL, --[horarioDesdePush]
	NULL, --[horarioHastaPush]
	'bb_alerta_post_oferta_credito', --[plantillaMail]
	'BuhoBank - Ya completaste casi todo el registro.', --[asuntoMail]
	NULL, --[horarioDesdeMail]
	NULL, --[horarioHastaMail]
	1, --[pushHabilitado]
	1  --[mailHabilitado]
)
GO



