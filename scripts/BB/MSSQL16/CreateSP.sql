/*SQL_SERVER*/
USE buhobank
GO

BEGIN TRANSACTION
IF EXISTS (SELECT 1 FROM sysobjects WHERE NAME = 'SP_BuscarPorLatitudLongitud')
	DROP PROCEDURE DBO.SP_BuscarPorLatitudLongitud;

SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO


CREATE PROCEDURE [dbo].[SP_BuscarPorLatitudLongitud]
    @latitud DECIMAL(9, 6),
    @longitud DECIMAL(9, 6),
	@radio_km DECIMAL(10, 2)
AS
BEGIN 
    SELECT
        a.logo, 
		a.titulo as titulo,
		s.titulo sucu_titulo, 
		a.highlight,
		a.categoria as categoria,
		a.orden, 
		a.id_plantilla_flujo,
		b.titulo as beneficio,
		b.legales,
		b.tyc,
		CONCAT(s.localidad,', ',s.provincia),
		s.lat,
		s.lng ,
		 (
            GEOGRAPHY::Point(lat, lng, 4326)
            .STDistance(GEOGRAPHY::Point(@latitud, @longitud, 4326)) / 1000
        ) AS DistanciaKm
    FROM
        sucursales s 
		INNER JOIN alianzas a ON a.sucursales LIKE CONCAT('%', s.post_id, '%')
		LEFT JOIN beneficios b ON a.beneficios LIKE CONCAT('%', b.post_id, '%')
    WHERE
        (
            GEOGRAPHY::Point(lat, lng, 4326)
            .STDistance(GEOGRAPHY::Point(@latitud, @longitud, 4326)) / 1000
        )  <= @radio_km
		and s.post_status = 'publish'
		and b.post_status = 'publish'
		order by 
		(
            GEOGRAPHY::Point(lat, lng, 4326)
            .STDistance(GEOGRAPHY::Point(@latitud, @longitud, 4326)) / 1000
        ) asc
		--order by s.titulo, a.orden ASC
END;
GO





COMMIT TRANSACTION

GO