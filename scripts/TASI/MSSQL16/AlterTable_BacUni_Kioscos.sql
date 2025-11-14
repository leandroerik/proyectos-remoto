/*SQL_SERVER*/
use hipotecarioTAS
GO

IF NOT EXISTS (
  SELECT
    *
  FROM
    INFORMATION_SCHEMA.COLUMNS
  WHERE
    TABLE_NAME = 'TasKioscos' AND COLUMN_NAME = 'bac_uni')
BEGIN
    PRINT 'Modificando TasKioscos'
    
	ALTER TABLE dbo.TasKioscos ADD bac_uni int;
END
GO
BEGIN
    PRINT 'Actualizando kioscoId'
	
	UPDATE TASKioscos SET bac_uni = 1 WHERE KioscoId = 125;
	UPDATE TASKioscos SET bac_uni = 1 WHERE DireccionIP = '10.99.101.212';

END
GO

