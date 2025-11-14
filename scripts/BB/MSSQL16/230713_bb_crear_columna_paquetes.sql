/*SQL_SERVER*/
USE buhobank
GO

IF NOT EXISTS (
    SELECT * 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'bb_paquetes' 
    AND COLUMN_NAME = 'paq_base'
)
BEGIN

	ALTER TABLE bb_paquetes
	ADD paq_base [int] NULL;
END
GO