**POST  -  /hb/api/x-insert-gestion**

**Request**
```
{
    "estadoSolicitud": "1",
    "nroSolicitud": "10204964",
    "usuarioCrm": "generic_user",
    "idCobis": "803",
    "canal": "CRM"
}
```

**Response exito**
```
{
    "estado":"0"
}
```

**Response error**
```
{
    "estado": "ERROR",
    "detalle": [
        {
            "estado": "ERROR",
            "mensaje": "Ya existe la solicitud para este cobis",
            "detalle": "803"
        },
        {
            "estado": "ERROR",
            "mensaje": "Ya existe la solicitud para este cobis",
            "detalle": ""
        },
        {
            "estado": "ERROR",
            "mensaje": "Ya existe la solicitud para este cobis",
            "detalle": "8348074"
        }
    ]
}
```