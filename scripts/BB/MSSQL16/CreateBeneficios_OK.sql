/*SQL_SERVER*/
USE buhobank
GO

BEGIN TRANSACTION
IF EXISTS (SELECT 1 FROM sysobjects WHERE NAME = 'beneficios')
	DROP TABLE DBO.beneficios;

SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[beneficios](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[post_id] [int] NOT NULL,
	[titulo] [varchar](max) NOT NULL,
	[post_status] [varchar](50) NOT NULL,
	[legales] [varchar](max) NULL,
	[tyc] [varchar](max) NULL,
 CONSTRAINT [PK_beneficios] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO



INSERT INTO beneficios VALUES ('93701','3 Cuotas fijas - Todos los días','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93703','3 Cuotas sin interés - Todos los días','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93705','15% de ahorro en 3 cuotas sin interés - Jueves y viernes','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93707','3 cuotas fijas - Jueves y viernes','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93709','3 cuotas fijas - Jueves','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93711','3 Cuotas sin interés - Viernes, Sabados, Domingos','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93713','15% OFF 1 pago con Crédito - Sabado','publish', 'App YPF - Búho One', NULL);
INSERT INTO beneficios VALUES ('93715','Megatone TC 25% de ahorro y 3 cuotas sin interes - Sabados y Domingos','publish', 'Búho One', NULL);
INSERT INTO beneficios VALUES ('93717','25% de ahorro y 3 cuotas sin interes - Sabados y Domingos','publish', 'Búho One', NULL);
INSERT INTO beneficios VALUES ('93719','25% de ahorro y 6 cuotas sin interes - Sabados y Domingos','publish', 'Búho One', NULL);
INSERT INTO beneficios VALUES ('93721','Vinotecas 10% OFF y 3 cuotas sin interes','publish', 'Todos los clientes con credito', NULL);
INSERT INTO beneficios VALUES ('93723','6 Cuotas sin interés - Todos los días','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93725','6 Cuotas sin interés - Jueves','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93727','6 Cuotas fija - Todos los días','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93729','15% de ahorro en 6 cuotas sin interés - Jueves y viernes','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93731','25% de ahorro y 3 cuotas sin interes - Todos los días','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93733','10% de ahorro y 3 cuotas sin interes - Todos los días','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93735','6 Cuotas fija - jueves y viernes','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93737','6 Cuotas fija - jueves','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93739','Sodimac 6 Cuotas sin interés - Jueves','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93741','Sodimac 3 Cuotas sin interés - todos los dias','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93743','9 cuotas fijas - Jueves y viernes','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93745','9 cuotas fijas - Jueves','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93747','20% de ahorro y 9 cuotas - jueves','publish', 'sin tope', NULL);
INSERT INTO beneficios VALUES ('93749','12 cuotas fijas - Jueves y viernes','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93751','12 cuotas fijas - Jueves','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93753','12 cuotas sin interés - Jueves','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('93755','25% de ahorro - Lunes','publish', 'tope $1500', NULL);
INSERT INTO beneficios VALUES ('93757','25% de ahorro - Lunes','publish', 'Búho one', NULL);
INSERT INTO beneficios VALUES ('93759','25% de ahorro - Lunes','publish', 'Búho sueldo', NULL);
INSERT INTO beneficios VALUES ('93761','25% de ahorro - Lunes','publish', 'Búho emprendedor', NULL);
INSERT INTO beneficios VALUES ('93763','25% de ahorro - Lunes','publish', 'Búho jubilados', NULL);
INSERT INTO beneficios VALUES ('93765','25% de ahorro - Lunes','publish', 'Todos los clientes', NULL);
INSERT INTO beneficios VALUES ('93767','25% de ahorro - Martes y Miercoles','publish', 'Tope $1000', NULL);
INSERT INTO beneficios VALUES ('93769','25% de ahorro - Martes y Miercoles','publish', 'Búho One', NULL);
INSERT INTO beneficios VALUES ('93771','20% de ahorro - Martes y Miercoles','publish', 'Búho sueldo', NULL);
INSERT INTO beneficios VALUES ('93773','20% de ahorro - Martes y Miercoles','publish', 'Búho emprendedor', NULL);
INSERT INTO beneficios VALUES ('93775','20% de ahorro - Martes y Miercoles','publish', 'Búho jubilados', NULL);
INSERT INTO beneficios VALUES ('93777','15% de ahorro - Martes y Miercoles','publish', 'Todos los clientes', NULL);
INSERT INTO beneficios VALUES ('93779','25% de ahorro - Sabado y Domingos','publish', 'Tope $1000', NULL);
INSERT INTO beneficios VALUES ('93781','25% de ahorro - Sabado y Domingos','publish', 'Búho One', NULL);
INSERT INTO beneficios VALUES ('93783','20% de ahorro - Sabado y Domingos','publish', 'Búho sueldo', NULL);
INSERT INTO beneficios VALUES ('93785','20% de ahorro - Sabado y Domingos','publish', 'Búho emprendedor', NULL);
INSERT INTO beneficios VALUES ('93787','20% de ahorro - Sabado y Domingos','publish', 'Búho jubilados', NULL);
INSERT INTO beneficios VALUES ('93789','15% de ahorro - Sabado y Domingos','publish', 'Todos los clientes', NULL);
INSERT INTO beneficios VALUES ('93791','25% de ahorro Librerias - Miercoles','publish', 'Tope $3000', NULL);
INSERT INTO beneficios VALUES ('93793','25% de ahorro Librerias - Miercoles','publish', 'Búho One', NULL);
INSERT INTO beneficios VALUES ('93795','20% de ahorro Librerias - Miercoles','publish', 'Búho sueldo', NULL);
INSERT INTO beneficios VALUES ('93797','20% de ahorro Librerias - Miercoles','publish', 'Búho emprendedor', NULL);
INSERT INTO beneficios VALUES ('93799','20% de ahorro Librerias - Miercoles','publish', 'Búho jubilados', NULL);
INSERT INTO beneficios VALUES ('93801','15% de ahorro Librerias - Miercoles','publish', 'Todos los clientes', NULL);
INSERT INTO beneficios VALUES ('93803','5% de ahorro - Todos los días','publish', 'Sin tope', NULL);
INSERT INTO beneficios VALUES ('93805','10% de ahorro - Todos los días','publish', 'Sin tope', NULL);
INSERT INTO beneficios VALUES ('93807','15% de ahorro - Todos los días','publish', 'Sin tope', NULL);
INSERT INTO beneficios VALUES ('93809','20% de ahorro - Todos los días','publish', 'Sin tope', NULL);
INSERT INTO beneficios VALUES ('93811','20% de ahorro - jueves','publish', 'Sin tope', NULL);
INSERT INTO beneficios VALUES ('93813','25% de ahorro - todos los días','publish', 'Sin tope', NULL);
INSERT INTO beneficios VALUES ('93815','30% de ahorro - todos los días','publish', 'Sin tope', NULL);
INSERT INTO beneficios VALUES ('93817','25% de ahorro Sodimac - Jueves','publish', 'Búho One', NULL);
INSERT INTO beneficios VALUES ('93819','25% de ahorro Sodimac - Jueves','publish', 'Todos los clientes', NULL);
INSERT INTO beneficios VALUES ('93821','25% de ahorro Hogar&Deco - Jueves y Viernes','publish', 'Tope $5000', NULL);
INSERT INTO beneficios VALUES ('93823','25% de ahorro Hogar&Deco - Jueves y Viernes','publish', 'Búho One', NULL);
INSERT INTO beneficios VALUES ('93825','20% de ahorro Hogar&Deco - Jueves y Viernes','publish', 'Búho sueldo', NULL);
INSERT INTO beneficios VALUES ('93827','20% de ahorro Hogar&Deco - Jueves y Viernes','publish', 'Búho emprendedor', NULL);
INSERT INTO beneficios VALUES ('93829','20% de ahorro Hogar&Deco - Jueves y Viernes','publish', 'Búho jubilados', NULL);
INSERT INTO beneficios VALUES ('93831','15% de ahorro Hogar&Deco - Jueves y Viernes','publish', 'Todos los clientes', NULL);
INSERT INTO beneficios VALUES ('93833','10% de ahorro Fontenla','publish', 'Todos los dias', NULL);
INSERT INTO beneficios VALUES ('93835','25% de ahorro Hogar&Deco - Jueves','publish', 'Tope $5000', NULL);
INSERT INTO beneficios VALUES ('93837','Veterinarias & Petshops - 25% Sábados','publish', 'Tope $1500', NULL);
INSERT INTO beneficios VALUES ('93839','Veterinarias & Petshops - 25% Sábados','publish', 'Búho One', NULL);
INSERT INTO beneficios VALUES ('93841','Veterinarias & Petshops - 25% Sábados','publish', 'Búho sueldo', NULL);
INSERT INTO beneficios VALUES ('93843','Veterinarias & Petshops - 25% Sábados','publish', 'Búho emprendedor', NULL);
INSERT INTO beneficios VALUES ('93845','Veterinarias & Petshops - 25% Sábados','publish', 'Búho jubilados', NULL);
INSERT INTO beneficios VALUES ('93847','Veterinarias & Petshops - 25% Sábados','publish', 'Todos los clientes', NULL);
INSERT INTO beneficios VALUES ('93849','15% de ahorro - jueves','publish', 'Sin tope', NULL);
INSERT INTO beneficios VALUES ('93851','25% de ahorro - todos los días','publish', 'Sin tope', NULL);
INSERT INTO beneficios VALUES ('93853','25% de ahorro Supermercado','publish', 'Búho One', NULL);
INSERT INTO beneficios VALUES ('93855','25% de ahorro Supermercado','publish', 'Búho sueldo', NULL);
INSERT INTO beneficios VALUES ('93857','25% de ahorro Supermercado','publish', 'Búho emprendedor', NULL);
INSERT INTO beneficios VALUES ('93859','25% de ahorro Supermercado','publish', 'Búho Jubliados', NULL);
INSERT INTO beneficios VALUES ('93861','25% de ahorro Supermercado','publish', 'Todos los clientes', NULL);
INSERT INTO beneficios VALUES ('93863','20% de ahorro Markro - martes','publish', 'Tope $2500', NULL);
INSERT INTO beneficios VALUES ('93865','25% de ahorro Nini - martes','publish', 'Tope $3000', NULL);
INSERT INTO beneficios VALUES ('93867','25% de ahorro Libertad - debito - martes','publish', 'Tope $900 por transacción', NULL);
INSERT INTO beneficios VALUES ('93869','25% de ahorro Libertad - credito - martes','publish', 'Tope $900 por transacción', NULL);
INSERT INTO beneficios VALUES ('93871','25% OFF - Vinotecas','publish', 'Todos los clientes debito', NULL);
INSERT INTO beneficios VALUES ('105977','Cabify','publish', 'Búho emprendedor credito', NULL);
INSERT INTO beneficios VALUES ('105979','Debito 25% - Pedidos Ya - Jueves','publish', 'búho one', NULL);
INSERT INTO beneficios VALUES ('105981','Debito 25% - Pedidos Ya - Jueves','publish', 'Búho sueldo', NULL);
INSERT INTO beneficios VALUES ('105983','Debito 25% - Pedidos Ya - Jueves','publish', 'Búho emprendedor', NULL);
INSERT INTO beneficios VALUES ('105985','Debito 25% - Pedidos Ya - Jueves','publish', 'Búho jubilados', NULL);
INSERT INTO beneficios VALUES ('105987','Debito 25% - Pedidos Ya - Jueves','publish', 'Todos los clientes', NULL);
INSERT INTO beneficios VALUES ('105989','Debito 25% - Pedidos Ya - Jueves','publish', 'Búho one', NULL);
INSERT INTO beneficios VALUES ('105991','Jumbo Disco VEA - Crédito - Martes','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('105993','25% - MODO - Jumbo Disco VEA - Martes Débito','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('105995','25% - MODO - Chango Mas - Martes Débito','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('105997','25% - MODO - Farmaplus - Martes Débito','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('105999','25% - MODO - Farmaplus - Martes Crédito','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('106001','25% - MODO - Pigmento - Martes Débito','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('106003','25% - MODO - Pigmento - Martes Crédito','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('106005','15% de ahorro YPF  APP - Sabádos','publish', 'Crédito Búho One', NULL);
INSERT INTO beneficios VALUES ('110539','25% de ahorro y 3 cuotas sin interes - Sabados y Domingos','publish', 'Búho One .v1', NULL);
INSERT INTO beneficios VALUES ('110773','25% de ahorro y 3 cuotas sin interes - Todos los días','publish', 'Tienda online y física', NULL);
INSERT INTO beneficios VALUES ('110775','25% de ahorro  debito - Todos los días','publish', 'Tiendas físicas y Online - Sin tope', NULL);
INSERT INTO beneficios VALUES ('119657','Dia de la Niñez 2023','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('119767','Grupo Marquez 25% + 6C','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('120635','Semana de la Pintura Sep 2023  1','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('120637','Semana de la Pintura Sep 2023  2','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('120639','Semana de la Pintura Sep 2023  3','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('120641','Semana de la Pintura Sep 2023  4','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('120643','Semana de la Pintura Sep 2023  5','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('120645','Semana de la Pintura Sep 2023  6','publish', NULL, NULL);
INSERT INTO beneficios VALUES ('66241','ACEROS YA','trash', NULL, NULL);
INSERT INTO beneficios VALUES ('70443','La económica','trash', NULL, NULL);
INSERT INTO beneficios VALUES ('73427','Ñande roga srl','trash', NULL, NULL);
GO

GO

COMMIT TRANSACTION

GO