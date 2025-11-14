package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.officebanking.dto.cuentas.DescargaMovimientosDTO;
import ar.com.hipotecario.canal.officebanking.dto.pagosMasivos.OrdenDePagoDTO;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CsvOB {

    public static byte[] descargaMovimientos(Map<String, Object> parametros)  {
        try {
            Objeto movimientos = (Objeto) parametros.get("MOVIMIENTOS");
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator('.');
            symbols.setDecimalSeparator(',');
            DecimalFormat df = new DecimalFormat("#,##0.00", symbols);
            List<DescargaMovimientosDTO> movimientosDTO = new ArrayList<>();
            List<Objeto> lstMovimientos = movimientos.objetos();

            BigDecimal totalDebito = BigDecimal.ZERO;
            BigDecimal totalCredito = BigDecimal.ZERO;
            for (Objeto mov : lstMovimientos) {
                try{
                    BigDecimal nuevoDeb =new BigDecimal(mov.get("debito").toString());
                    totalDebito = totalDebito.add(nuevoDeb);
                }catch (Exception e){}
                try{
                    BigDecimal nuevoCred = new BigDecimal(mov.get("credito").toString());
                    totalCredito = totalCredito.add(nuevoCred);
                }catch (Exception e){}
            }
            Collections.reverse(lstMovimientos);
            lstMovimientos.forEach(movimiento -> {
                String saldoFormateado = "";
                if (movimiento.get("saldo") != null) {
                    saldoFormateado = df.format(new BigDecimal(movimiento.get("saldo").toString()));
                }
                BigDecimal debito = new BigDecimal(movimiento.get("debito").toString());
                BigDecimal credito = new BigDecimal(movimiento.get("credito").toString());


                String debitoFormateado = df.format(debito);
                String creditoFormateado = df.format(credito);

                movimientosDTO.add(
                        new DescargaMovimientosDTO(
                                movimiento.get("fecha").toString(),
                                movimiento.get("descripcion").toString(),
                                movimiento.get("sucursal").toString(),
                                movimiento.get("referencia").toString(),
                                debitoFormateado.equals("0,00") ? "" : debitoFormateado,
                                creditoFormateado.equals("0,00") ? "" : creditoFormateado,
                                saldoFormateado));
            });

            String totalDebitoFormateado = df.format(totalDebito);
            String totalCreditoFormateado = df.format(totalCredito);
            movimientosDTO.add(
                    new DescargaMovimientosDTO(
                            "",
                            "",
                            "",
                            "Total:",
                            totalDebitoFormateado,
                            totalCreditoFormateado,
                            ""
                    ));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter streamWriter = new OutputStreamWriter(baos, Charset.forName(StandardCharsets.UTF_8.name()));
            CSVWriter writer = new CSVWriter(streamWriter, ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

            var mappingStrategy = new OBManejoArchivos.CustomColumnPositionStrategy<DescargaMovimientosDTO>();
            String[] headers = {"FECHA", "DESCRIPCION", "SUCURSAL", "REFERENCIA", "DEBITO EN $", "CREDITO EN $", "SALDO EN $"};
            mappingStrategy.setColumnMapping(headers);
            mappingStrategy.setType(DescargaMovimientosDTO.class);

            StatefulBeanToCsv<DescargaMovimientosDTO> builder = new StatefulBeanToCsvBuilder<DescargaMovimientosDTO>(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .withSeparator(';')
                    .withMappingStrategy(mappingStrategy)
                    .build();
            builder.write(movimientosDTO);
            streamWriter.flush();
            String footerText = "\n\nEl presente documento no constituye el resumen original de su Caja de Ahorro o Cuenta Corriente sino que representa un extracto de los ultimos movimientos. El resumen correspondiente sera emitido por el Banco Hipotecario SA en los plazos previstos contractualmente.";
            baos.write(footerText.getBytes(StandardCharsets.UTF_8));

            writer.close();

            byte[] file = baos.toByteArray();

            return file;
        } catch (Exception e) {
            // Log error
            e.printStackTrace();
            return new byte[1];
        }
}
}
