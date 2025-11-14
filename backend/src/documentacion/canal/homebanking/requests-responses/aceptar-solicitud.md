**POST  -  /hb/api/aceptar-solicitud**

**Request**
```
{
    "nroSolicitud":"30486912"
}
```

**Response exito**

* Solicitud con mas de 1 integrante.
```
{
    "estado": "0",
    "pendiente": {
        "titular": false,
        "cotitular": true
    }
}
```

**Response error**
```
{
    "estado":"ERROR"
}
```