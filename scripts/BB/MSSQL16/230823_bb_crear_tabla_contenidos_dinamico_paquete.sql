USE buhobank
GO

IF OBJECT_ID('bb_contenidos_dinamico_paquete', 'U') IS NOT NULL 
	DROP TABLE [dbo].[bb_contenidos_dinamico_paquete]
GO

CREATE TABLE [dbo].[bb_contenidos_dinamico_paquete](

	[id] [int] PRIMARY KEY IDENTITY(1,1) NOT NULL,
	[id_paquete][int] NOT NULL,
	[tipo][varchar](255) NOT NULL,
	[imagen][varchar](MAX) NULL,
	[titulo][varchar](MAX) NULL,
	[descripcion][varchar](MAX) NULL,
	[texto][varchar](MAX) NULL,
	[texto_legales][varchar](MAX) NULL,
	[habilitado][bit] NOT NULL,
	[fecha_ultima_modificacion] datetime NOT NULL
)
GO


IF OBJECT_ID('bb_paquetes', 'U') IS NOT NULL 
	ALTER TABLE [dbo].[bb_contenidos_dinamico_paquete] ADD CONSTRAINT FK_contenido_dinamico_paquetes
	FOREIGN KEY(id_paquete) REFERENCES [dbo].[bb_paquetes](id)
	ON DELETE CASCADE ON UPDATE CASCADE
GO


INSERT INTO	[dbo].[bb_contenidos_dinamico_paquete]
(
	[id_paquete],
	[tipo],
	[imagen],
	[titulo],
	[descripcion],
	[texto],
	[texto_legales],
	[habilitado],
	[fecha_ultima_modificacion]
)
VALUES
(
	10, --[id_paquete]
	'ar_plus', --[tipo]
	'https://www.hipotecario.com.ar/media/buhobank/Log_Aero_Plus.png', --[imagen]
	'Tarjeta Signature', --[titulo]
	'Con tus compras sumá millas al programa de Aerolíneas Argentinas y vola por Argentina y el mundo.', --[descripcion]
	'<b>Membresía bonificada</b>
	<ul>
		<li> Después de diez (10) meses de permanencia y habiendo generado consumos con tu tarjeta de crédito, <b>debitaremos el costo de tu membresía en 3 cuotas y te las reintegraremos.</b></li>
		<li> Una vez solicitada la adhesión, el alta se realiza pasados diez (10) días hábiles.</li>
	</ul> 
	<b>Beneficios adicionales</b>
	<ul>
		<li> Los clientes Signature acumulan <b>un 25% más</b> en millas con sus compras.</li>
	</ul> 
	<b>¿Cómo se calculan las millas?</b>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos, sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto]
	'<b>Membresía bonificada</b>
	<br>
	<ul>
		<li> Después de diez (10) meses de permanencia y habiendo generado consumos con tu tarjeta de crédito, <b>debitaremos el costo de tu membresía en 3 cuotas y te las reintegraremos.</b><br></li>
		<li> Una vez solicitada la adhesión, el alta se realiza pasados diez (10) días hábiles.</li>
	</ul> 
	<br>
	<b>Beneficios adicionales</b>
	<br>
	<ul>
		<li> Los clientes Signature acumulan <b>un 25% más</b> en millas con sus compras.</li>
	</ul> 
	<br>
	<b>¿Cómo se calculan las millas?</b>
	<br>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos, sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto_legales]
	1, --[habilitado]
	GETDATE() --[fecha_ultima_modificacion]
),
(
	9, --[id_paquete]
	'ar_plus', --[tipo]
	'https://www.hipotecario.com.ar/media/buhobank/Log_Aero_Plus.png', --[imagen]
	'Tarjeta Platinum', --[titulo]
	'Con tus compras sumá millas al programa de Aerolíneas Argentinas y vola por Argentina y el mundo.', --[descripcion]
	'<b>Costo de membresía</b>
	<ul>
		<li> <b> 3 coutas de $18.867,17 + IVA. Total: $56.601,51</b> luego de 10 meses de permanencia en el Programa Ar Plus</li>
		<li> Una vez solicitada la adhesión, el alta se realiza pasados diez (10) días hábiles.</li>
	</ul>
	<b>Beneficios adicionales</b>
	<ul>
		<li> Los clientes Platinum acumulan <b>un 15% más</b> en millas con sus compras.</li>
	</ul>
	<b>¿Cómo se calculan las millas?</b>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos, sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto]
	'<b>Costo de membresía</b>
	<br>
	<ul>
		<li> <b> 3 coutas de $18.867,17 + IVA. Total: $56.601,51</b> luego de 10 meses de permanencia en el Programa Ar Plus<br></li>
		<li> Una vez solicitada la adhesión, el alta se realiza pasados diez (10) días hábiles.</li>
	</ul>
	<br>
	<b>Beneficios adicionales</b>
	<br>
	<ul>
		<li> Los clientes Platinum acumulan <b>un 15% más</b> en millas con sus compras.</li>
	</ul>
	<br>
	<b>¿Cómo se calculan las millas?</b>
	<br>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos, sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto_legales]
	1, --[habilitado]
	GETDATE() --[fecha_ultima_modificacion]
),
(
	8, --[id_paquete]
	'ar_plus', --[tipo]
	'https://www.hipotecario.com.ar/media/buhobank/Log_Aero_Plus.png', --[imagen]
	'Tarjeta Gold', --[titulo]
	'Con tus compras sumá millas al programa de Aerolíneas Argentinas y vola por Argentina y el mundo.', --[descripcion]
	'<b>Costo de membresía</b>
	<ul>
		<li> <b> 3 coutas de $18.867,17 + IVA. Total: $56.601,51</b> luego de 10 meses de permanencia en el Programa Ar Plus</li>
		<li> Una vez solicitada la adhesión,el alta se realiza pasados diez (10) días hábiles.</li>
	</ul>
	<b>¿Cómo se calculan las millas?</b>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos,sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto]
	'<b>Costo de membresía</b>
	<br>
	<ul>
		<li> <b> 3 coutas de $18.867,17 + IVA. Total: $56.601,51</b> luego de 10 meses de permanencia en el Programa Ar Plus<br></li>
		<li> Una vez solicitada la adhesión,el alta se realiza pasados diez (10) días hábiles.</li>
	</ul>
	<br>
	<b>¿Cómo se calculan las millas?</b>
	<br>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos,sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto_legales]
	1, --[habilitado]
	GETDATE() --[fecha_ultima_modificacion]
),
(
	7, --[id_paquete]
	'ar_plus', --[tipo]
	'https://www.hipotecario.com.ar/media/buhobank/Log_Aero_Plus.png', --[imagen]
	'Tarjeta Internacional', --[titulo]
	'Con tus compras sumá millas al programa de Aerolíneas Argentinas y vola por Argentina y el mundo.', --[descripcion]
	'<b>Costo de membresía</b>
	<ul>
		<li> <b> 3 coutas de $18.867,17 + IVA. Total: $56.601,51</b> luego de 10 meses de permanencia en el Programa Ar Plus</li>
		<li> Una vez solicitada la adhesión,el alta se realiza pasados diez (10) días hábiles.</li>
	</ul>
	<b>¿Cómo se calculan las millas?</b>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos,sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto]
	'<b>Costo de membresía</b>
	<ul>
		<li> <b> 3 coutas de $18.867,17 + IVA. Total: $56.601,51</b> luego de 10 meses de permanencia en el Programa Ar Plus</li>
		<li> Una vez solicitada la adhesión,el alta se realiza pasados diez (10) días hábiles.</li>
	</ul>
	<br>
	<b>¿Cómo se calculan las millas?</b>
	<br>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos,sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto_legales]
	1, --[habilitado]
	GETDATE() --[fecha_ultima_modificacion]
),
(
	6, --[id_paquete]
	'ar_plus', --[tipo]
	'https://www.hipotecario.com.ar/media/buhobank/Log_Aero_Plus.png', --[imagen]
	'Tarjeta Signature', --[titulo]
	'Con tus compras sumá millas al programa de Aerolíneas Argentinas y vola por Argentina y el mundo.', --[descripcion]
	'<b>Membresía bonificada</b>
	<ul>
		<li> Después de diez (10) meses de permanencia y habiendo generado consumos con tu tarjeta de crédito, <b>debitaremos el costo de tu membresía en 3 cuotas y te las reintegraremos.</b></li>
		<li> Una vez solicitada la adhesión, el alta se realiza pasados diez (10) días hábiles.</li>
	</ul> 
	<b>Beneficios adicionales</b>
	<ul>
		<li> Los clientes Signature acumulan <b>un 25% más</b> en millas con sus compras.</li>
	</ul>
	<b>¿Cómo se calculan las millas?</b>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos, sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto]
	'<b>Membresía bonificada</b>
	<br>
	<ul>
		<li> Después de diez (10) meses de permanencia y habiendo generado consumos con tu tarjeta de crédito, <b>debitaremos el costo de tu membresía en 3 cuotas y te las reintegraremos.</b><br></li>
		<li> Una vez solicitada la adhesión, el alta se realiza pasados diez (10) días hábiles.</li>
	</ul> 
	<br>
	<b>Beneficios adicionales</b>
	<br>
	<ul>
		<li> Los clientes Signature acumulan <b>un 25% más</b> en millas con sus compras.</li>
	</ul>
	<br> 
	<b>¿Cómo se calculan las millas?</b>
	<br>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos, sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto_legales]
	1, --[habilitado]
	GETDATE() --[fecha_ultima_modificacion]
),
(
	5, --[id_paquete]
	'ar_plus', --[tipo]
	'https://www.hipotecario.com.ar/media/buhobank/Log_Aero_Plus.png', --[imagen]
	'Tarjeta Platinum', --[titulo]
	'Con tus compras sumá millas al programa de Aerolíneas Argentinas y vola por Argentina y el mundo.', --[descripcion]
	'<b>Costo de membresía</b>
	<ul>
		<li> <b> 3 coutas de $18.867,17 + IVA. Total: $56.601,51</b> luego de 10 meses de permanencia en el Programa Ar Plus</li>
		<li> Una vez solicitada la adhesión, el alta se realiza pasados diez (10) días hábiles.</li>
	</ul>
	<b>Beneficios adicionales</b>
	<ul>
		<li> Los clientes Platinum acumulan <b>un 15% más</b> en millas con sus compras.</li>
	</ul>
	<b>¿Cómo se calculan las millas?</b>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos, sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto]
	'<b>Costo de membresía</b>
	<br>
	<ul>
		<li> <b> 3 coutas de $18.867,17 + IVA. Total: $56.601,51</b> luego de 10 meses de permanencia en el Programa Ar Plus<br></li>
		<li> Una vez solicitada la adhesión, el alta se realiza pasados diez (10) días hábiles.</li>
	</ul>
	<br>
	<b>Beneficios adicionales</b>
	<br>
	<ul>
		<li> Los clientes Platinum acumulan <b>un 15% más</b> en millas con sus compras.</li>
	</ul>
	<br>
	<b>¿Cómo se calculan las millas?</b>
	<br>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos, sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto_legales]
	1, --[habilitado]
	GETDATE() --[fecha_ultima_modificacion]
),
(
	4, --[id_paquete]
	'ar_plus', --[tipo]
	'https://www.hipotecario.com.ar/media/buhobank/Log_Aero_Plus.png', --[imagen]
	'Tarjeta Gold', --[titulo]
	'Con tus compras sumá millas al programa de Aerolíneas Argentinas y vola por Argentina y el mundo.', --[descripcion]
	'<b>Costo de membresía</b>
	<ul>
		<li> <b> 3 coutas de $18.867,17 + IVA. Total: $56.601,51</b> luego de 10 meses de permanencia en el Programa Ar Plus</li>
		<li> Una vez solicitada la adhesión,el alta se realiza pasados diez (10) días hábiles.</li>
	</ul>
	<b>¿Cómo se calculan las millas?</b>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos,sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto]
	'<b>Costo de membresía</b>
	<br>
	<ul>
		<li> <b> 3 coutas de $18.867,17 + IVA. Total: $56.601,51</b> luego de 10 meses de permanencia en el Programa Ar Plus<br></li>
		<li> Una vez solicitada la adhesión,el alta se realiza pasados diez (10) días hábiles.</li>
	</ul>
	<br>
	<b>¿Cómo se calculan las millas?</b>
	<br>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos,sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto_legales]
	1, --[habilitado]
	GETDATE() --[fecha_ultima_modificacion]
),
(
	3, --[id_paquete]
	'ar_plus', --[tipo]
	'https://www.hipotecario.com.ar/media/buhobank/Log_Aero_Plus.png', --[imagen]
	'Tarjeta Internacional', --[titulo]
	'Con tus compras sumá millas al programa de Aerolíneas Argentinas y vola por Argentina y el mundo.', --[descripcion]
	'<b>Costo de membresía</b>
	<ul>
		<li> <b> 3 coutas de $18.867,17 + IVA. Total: $56.601,51</b> luego de 10 meses de permanencia en el Programa Ar Plus</li>
		<li> Una vez solicitada la adhesión,el alta se realiza pasados diez (10) días hábiles.</li>
	</ul>
	<b>¿Cómo se calculan las millas?</b>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos,sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto]
	'<b>Costo de membresía</b>
	<br>
	<ul>
		<li> <b> 3 coutas de $18.867,17 + IVA. Total: $56.601,51</b> luego de 10 meses de permanencia en el Programa Ar Plus<br></li>
		<li> Una vez solicitada la adhesión,el alta se realiza pasados diez (10) días hábiles.</li>
	</ul>
	<br>
	<b>¿Cómo se calculan las millas?</b>
	<br>
	<ul>
		<li> Las millas se calculan por consumos. Por cada dolar (U$S 1,00) gastado o su equivalente en pesos,sumás 1 Milla Aerolíneas Plus.</li>
	</ul>', --[texto_legales]
	1, --[habilitado]
	GETDATE() --[fecha_ultima_modificacion]
)
GO
