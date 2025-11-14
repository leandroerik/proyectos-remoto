/*SQL_SERVER*/
USE [buhobank]
GO 

IF OBJECT_ID('[dbo].[sp_bb_actualizar_historico_push]', 'P') IS NOT NULL 
	DROP PROCEDURE [dbo].[sp_bb_actualizar_historico_push]
GO 

SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[sp_bb_actualizar_historico_push] @fechaDesde DATETIME
AS
BEGIN

--log
IF OBJECT_ID ('[dbo].[bb_historico_push_log]') IS NOT NULL
	DROP TABLE [dbo].[bb_historico_push_log];

IF OBJECT_ID ('[dbo].[bb_historico_push_log_escliente]') IS NOT NULL
	DROP TABLE [dbo].[bb_historico_push_log_escliente];

IF OBJECT_ID ('[dbo].[bb_historico_push_log_registrosvu]') IS NOT NULL
	DROP TABLE [dbo].[bb_historico_push_log_registrosvu];

IF OBJECT_ID ('[dbo].[bb_historico_push_log_final]') IS NOT NULL
	DROP TABLE [dbo].[bb_historico_push_log_final];


--sesion y sesion_bb2
IF OBJECT_ID ('[dbo].[bb_historico_push_esales]') IS NOT NULL
	DROP TABLE [dbo].[bb_historico_push_esales];

IF OBJECT_ID ('[dbo].[bb_historico_push_sesion]') IS NOT NULL
	DROP TABLE [dbo].[bb_historico_push_sesion];

IF OBJECT_ID ('[dbo].[bb_historico_push_finalizados]') IS NOT NULL
	DROP TABLE [dbo].[bb_historico_push_finalizados];

IF OBJECT_ID ('[dbo].[bb_historico_push_sesion_sinregistrosvu]') IS NOT NULL
	DROP TABLE [dbo].[bb_historico_push_sesion_sinregistrosvu];


--log basico
SELECT * INTO [dbo].[bb_historico_push_log]
FROM [buhobank].[dbo].[log] WITH (NOLOCK)
WHERE momento >= @fechaDesde
AND evento NOT LIKE '%REQUEST%'
AND evento NOT LIKE '%PUSH%'
AND evento NOT LIKE '%PROCESO%'
AND evento NOT LIKE '%MANUAL%'
AND endpoint NOT LIKE '%vista%'
AND endpoint NOT LIKE '%plazoFijo%' 
AND endpoint NOT LIKE '%salir%' 

--logs
SELECT cuit INTO [dbo].[bb_historico_push_log_escliente]
FROM [dbo].[bb_historico_push_log] WITH (NOLOCK)
WHERE evento IN ('BB_FINALIZAR_OK', 'BB_ES_CLIENTE')

SELECT * INTO [dbo].[bb_historico_push_log_final]
FROM [dbo].[bb_historico_push_log] WITH (NOLOCK)
WHERE cuit NOT IN (SELECT cuit FROM bb_historico_push_log_escliente WITH (NOLOCK))

SELECT cuit, momento INTO [dbo].[bb_historico_push_log_registrosvu]
FROM [dbo].[bb_historico_push_log_final] WITH (NOLOCK)
WHERE endpoint IN ('/bb/api/guardarvucompleto', '/bb/api/retomarSesion')
OR evento LIKE 'BB_FRONT_VU%'


--Sesion y sesion_bb2 BASICO
SELECT t1.*, t2.telefono_otp_validado, t2.token_firebase, t2.plataforma, t2.sucursal_onboarding INTO [dbo].[bb_historico_push_esales]
FROM [esales].[dbo].[Sesion] AS t1 WITH (NOLOCK)
INNER JOIN [esales].[dbo].[Sesion_Esales_BB2] AS t2 WITH (NOLOCK)
ON t1.id = t2.sesion_id
WHERE fecha_ultima_modificacion >= @fechaDesde

--Sesion y sesion_bb2
SELECT * INTO [dbo].[bb_historico_push_sesion]
FROM [dbo].[bb_historico_push_esales] WITH (NOLOCK)
WHERE estado NOT IN ('BATCH_CORRIENDO')
AND cuil NOT IN (SELECT cuit FROM bb_historico_push_log_escliente WITH (NOLOCK))

SELECT * INTO [dbo].[bb_historico_push_sesion_sinregistrosvu]
FROM [dbo].[bb_historico_push_sesion] WITH (NOLOCK)
WHERE cuil NOT IN (SELECT cuit FROM bb_historico_push_log_registrosvu WITH (NOLOCK))

SELECT * INTO [dbo].[bb_historico_push_finalizados]
FROM [dbo].[bb_historico_push_esales] WITH (NOLOCK)
WHERE estado IN ('FINALIZAR_OK')


END
GO



