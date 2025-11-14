USE [esales]
GO


IF OBJECT_ID('hitorico_remarketing_2', 'U') IS NULL 
	CREATE TABLE [dbo].[hitorico_remarketing_2] (
		[fecha_inicio] [datetime] NULL,
		[cuil] [varchar](20) NULL,
		[telefono] [varchar](100) NULL
	)
GO


IF OBJECT_ID('[dbo].[SP_actualizar_historico_remarketing_whatsapp]', 'P') IS NOT NULL 
	DROP PROCEDURE [dbo].[SP_actualizar_historico_remarketing_whatsapp]
GO

SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE PROCEDURE [dbo].[SP_actualizar_historico_remarketing_whatsapp] @fechaDesde DATETIME, @estados VARCHAR(MAX), @eventos VARCHAR(MAX)
AS
BEGIN

--BASE GENERAL
IF OBJECT_ID ('esales.dbo.remarketing_whatsapp') IS NOT NULL
	DROP TABLE esales.dbo.remarketing_whatsapp;

SELECT '549' + SUBSTRING(telefono_celular_ddn,2,4) + telefono_celular_caract + telefono_celular_nro AS telefono, fecha_inicio, cuil
INTO esales.dbo.remarketing_whatsapp      
FROM esales.dbo.Sesion a 
INNER JOIN Sesion_Esales_BB2 b
ON a.id = b.sesion_id
WHERE a.fecha_inicio >= @fechaDesde
AND a.cuil NOT IN(SELECT cuil FROM esales.dbo.hitorico_remarketing_2)
AND LEN(a.telefono_celular_ddn) > 0
AND LEN(a.telefono_celular_caract) > 0
AND LEN(a.telefono_celular_nro) > 0
AND b.telefono_otp_validado = 1
AND @estados NOT LIKE CONCAT('%', a.estado, '%')


---deleteo el día de hoy y los null de teléfono y aquellos que les vendí
DELETE FROM esales.dbo.remarketing_whatsapp 
WHERE telefono IS NULL

 
DELETE FROM esales.dbo.remarketing_whatsapp 
WHERE CONVERT(DATE, fecha_inicio, 112) IN  
(SELECT CONVERT(DATE, fecha_inicio, 112) from
( SELECT MAX(fecha_inicio) fecha_inicio
FROM esales.dbo.Sesion) a)


DELETE FROM esales.dbo.remarketing_whatsapp
WHERE cuil IN (SELECT cuit FROM buhobank.dbo.log
WHERE @eventos LIKE CONCAT('%', evento, '%'))

DELETE FROM esales.dbo.remarketing_whatsapp
WHERE cuil IN (
SELECT cuit FROM buhobank.dbo.log
WHERE momento >= @fechaDesde
AND evento IN ('BB_BATCH_CORRIENDO', 'BB_PREVIO_BATCH_CORRIENDO'))

 
-------dejo valores únicos
IF OBJECT_ID ('esales.dbo.remarketing_whatsapp_2') IS NOT NULL
  DROP TABLE esales.dbo.remarketing_whatsapp_2;

 
SELECT DISTINCT cuil ,telefono,fecha INTO esales.dbo.remarketing_whatsapp_2  
FROM (SELECT cuil, telefono, CONVERT(DATE, fecha_inicio, 112) fecha 
FROM esales.dbo.remarketing_whatsapp ) a


--HISTÓRICO 
INSERT INTO esales.dbo.hitorico_remarketing_2 (telefono, fecha_inicio, cuil)

SELECT telefono, fecha fecha_inicio, cuil 
FROM esales.dbo.remarketing_whatsapp_2


END
GO



