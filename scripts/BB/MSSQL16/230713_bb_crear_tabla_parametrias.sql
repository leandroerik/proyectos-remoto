/*SQL_SERVER*/
USE buhobank
GO


INSERT INTO [dbo].[bb_parametrias]
           ([id_plantilla_flujo]
           ,[nombre]
           ,[valor_android]
           ,[valor_ios]
           ,[fecha_ultima_modificacion])
SELECT
    1,
    'CUENTA_SUELDO',
    0,
    0,
    GETDATE()
WHERE NOT EXISTS (
    SELECT 1
    FROM [dbo].[bb_parametrias]
    WHERE [nombre] = 'CUENTA_SUELDO'
);

GO
