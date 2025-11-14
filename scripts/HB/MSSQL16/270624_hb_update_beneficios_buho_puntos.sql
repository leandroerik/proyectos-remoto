USE esales
GO

UPDATE paquete_beneficio SET desc_beneficio = 'Búho Puntos: Sumá puntos con tus compras para canjear por premios.', 
desc_beneficio_html = 'B&uacute;ho Puntos: Sum&aacute; puntos con tus compras para canjear por premios.'
WHERE desc_beneficio like '%primeros 3 meses para canjear por productos%'
GO