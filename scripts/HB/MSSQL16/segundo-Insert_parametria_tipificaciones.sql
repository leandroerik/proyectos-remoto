USE [Homebanking]
GO
MERGE homebanking.[dbo].[parametria_tipificaciones] AS target
USING (SELECT UPPER('BUHO PUNTOS - PUNTOS X PESOS') AS tipificacion) AS source
ON (target.tipificacion = source.tipificacion)
WHEN MATCHED THEN 
    UPDATE SET 
        tipificacion_final = 'Procesamos con &eacute;xito tu canje de Puntos por Pesos',
        fecha_ultima_modificacion = GETDATE()
WHEN NOT MATCHED THEN
    INSERT ([tipificacion],
            [producto],
            [tipo],
            [fecha_ultima_modificacion],
            [tipificacion_final],
            [producto_final])
    VALUES (UPPER('BUHO PUNTOS - PUNTOS X PESOS'),
            UPPER('PROGRAMA DE BENEFICIOS'),
            UPPER('CLIENTE'),
            GETDATE(),
            'Procesamos con &eacute;xito tu canje de Puntos por Pesos',
            'Programa de Beneficios');
GO