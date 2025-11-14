**POST  -  /hb/api/datos-confirmacion-producto**

**Request**
```
{
    "nroSolicitud":"30486912"
}
```

**Response exito**
```
{
    "estado": "0",
    "aceptado": true,
    "integrantes": [
        {
            "cobis": "803",
            "estadoSolicitud": "2",
            "fechaAceptado": "29/10/2024",
            "nombreCompleto": "EMELDA THEODORIC",
            "documento": "17165508",
            "fecha": "29/10/2024",
            "nroSolicitud": "30486912",
            "aceptado": true,
            "titularidad": "0"
        },
        {
            "cobis": "",
            "estadoSolicitud": "1",
            "fechaAceptado": "29/10/2024",
            "nombreCompleto": "TATIANA A PASQUINI",
            "documento": "48328685",
            "fecha": "29/10/2024",
            "nroSolicitud": "30486912",
            "aceptado": false,
            "titularidad": "1"
        },
        {
            "cobis": "8348074",
            "estadoSolicitud": "1",
            "fechaAceptado": "29/10/2024",
            "nombreCompleto": "DANUTA SWITHIN",
            "documento": "31932422",
            "fecha": "29/10/2024",
            "nroSolicitud": "30486912",
            "aceptado": false,
            "titularidad": "2"
        }
    ],
    "productos": [
        {
            "nombre": "ADICIONALES DE TARJETA DE CRÉDITO VISA"
        }
    ]
}
```

**Response error**
```
{
    "estado":"ERROR"
}
```