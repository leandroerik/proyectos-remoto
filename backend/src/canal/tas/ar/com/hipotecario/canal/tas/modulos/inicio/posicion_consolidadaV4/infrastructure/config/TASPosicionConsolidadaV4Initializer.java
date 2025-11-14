package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.config;

import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.assembler.TASPosicionConsolidadaV4Assembler;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.adapter.TASRestPaquetesV4Adapter;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port.*;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.useCase.TASPosicionConsolidadaV4UseCase;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.controller.TASPosicionConsolidadaV4Controller;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.adapter.*;

public class TASPosicionConsolidadaV4Initializer {
    public static void init(){
        // Crear los port concretos
        TASPosicionConsolidadaV4Port port = new TASRestPosicionConsolidadaV4Adapter();
        TASCuentasV4Port portCta = new TASRestCuentasV4Adapter();
        TASCajasSeguridadV4Port portCaja = new TASRestCajasSeguridadV4Adapter();
        TASInversionesV4Port portInv = new TASRestInversionesV4Adapter();
        TASPrestamosV4Port portPrestamo = new TASRestPrestamosV4Adapter();
        TASTarjetasDebitoV4Port portTD = new TASRestTarjetasDebitoV4Adapter();
        TASPlazosFijosV4Port portPF = new TASRestPlazosFijosV4Adapter();
        TASPaquetesV4Port portPaq = new TASRestPaquetesV4Adapter();

        // Inyectar el port en el UseCase
        TASPosicionConsolidadaV4UseCase.init(port);
        // Inyectar el UseCase en el Controller
        TASPosicionConsolidadaV4Controller.init();
        // Inyectar los ports en el Assembler
        TASPosicionConsolidadaV4Assembler.init(portCta, portCaja, portInv,
                portPrestamo, portTD, portPF, portPaq);
    }
}
