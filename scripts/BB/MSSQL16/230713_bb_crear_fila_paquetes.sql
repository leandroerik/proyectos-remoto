/*SQL_SERVER*/
USE buhobank
GO


IF NOT EXISTS (
    SELECT 1 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'bb_paquetes' 
    AND COLUMN_NAME = 'paq_base'
)
BEGIN

	ALTER TABLE bb_paquetes
	ADD paq_base [int] NULL;
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM bb_paquetes
    WHERE numero_paquete in ('34','35','36','37','38')
)
BEGIN

INSERT INTO	[dbo].[bb_paquetes]
(
	[id_plantilla_flujo],
	[numero_paquete],
	[letra_tc],
	[nombre],
	[es_standalone],
	[td_virtual],
	[tc_virtual],
	[es_emprendedor],
	[envio_sucursal],
	[cuenta_inversor],
	[afinidad],
	[modelo_liquidacion],
	[codigo_distribucion],
	[ciclo],
	[caracteristica],
	[fecha_ultima_modificacion],
	[paq_base]
)
VALUES (
	1, --id_plantilla_flujo
	34, --numero_paquete
	NULL, --letra_tc
	'CS_FACIL_SUELDO', --nombre
	0, --es_standalone
	0, --td_virtual
	0, --tc_virtual
	0, --es_emprendedor
	1, --envio_sucursal
	1, --cuenta_inversor
	NULL,
	NULL,
	NULL,
	NULL,
	NULL, --[caracteristica]
	GETDATE(),
	0 --paq_base
),
(
	1, --id_plantilla_flujo
	35, --numero_paquete
	'I', --letra_tc
	'CS_INTERNACIONAL', --nombre
	0, --es_standalone
	0, --td_virtual
	0, --tc_virtual
	0, --es_emprendedor
	1, --envio_sucursal
	1, --cuenta_inversor
	NULL,
	NULL,
	NULL,
	NULL,
	NULL, --[caracteristica]
	GETDATE(),
	40 --paq_base
),
(
	1, --id_plantilla_flujo
	36, --numero_paquete
	'P', --letra_tc
	'CS_GOLD', --nombre
	0,
	0, --td_virtual
	0, --tc_virtual
	0, --es_emprendedor
	1, --envio_sucursal
	1, --cuenta_inversor
	NULL,
	NULL,
	NULL,
	NULL,
	NULL, --[caracteristica]
	GETDATE(),
	41 --paq_base
),
(
	1, --id_plantilla_flujo
	37, --numero_paquete
	'L', --letra_tc
	'CS_PLATINUM', --nombre
	0,
	0, --td_virtual
	0, --tc_virtual
	0, --es_emprendedor
	1, --envio_sucursal
	1, --cuenta_inversor
	NULL,
	NULL,
	NULL,
	NULL,
	NULL, --[caracteristica]
	GETDATE(),
	42 --paq_base
),
(
	1, --id_plantilla_flujo
	38, --numero_paquete
	'S', --letra_tc
	'CS_BLACK', --nombre
	0,
	0, --td_virtual
	0, --tc_virtual
	0, --es_emprendedor
	1, --envio_sucursal
	1, --cuenta_inversor
	NULL,
	NULL,
	NULL,
	NULL,
	NULL, --[caracteristica]
	GETDATE(),
	43 --paq_base
)

END
GO