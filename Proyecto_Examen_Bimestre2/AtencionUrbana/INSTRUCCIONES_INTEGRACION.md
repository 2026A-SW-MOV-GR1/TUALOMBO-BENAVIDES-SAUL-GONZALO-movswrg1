# AtencionUrbana — integración

## 1. Clave de Google Maps

En el archivo `local.properties` del proyecto, agrega:

```properties
MAPS_API_KEY=TU_CLAVE_REAL_DE_GOOGLE_MAPS
```

No subas `local.properties` al repositorio.

La clave debe tener habilitado **Maps SDK for Android** y estar restringida al
paquete:

```text
com.epn.atencionurbana.saul
```

También debe restringirse con la huella SHA-1 correspondiente.

## 2. Intent que Priscila envía a Saul

```kotlin
val intentSaul = Intent(
    "com.epn.atencionurbana.saul.action.RECIBIR_INCIDENCIA"
).apply {
    setPackage("com.epn.atencionurbana.saul")

    putExtra("idIncidente", idIncidente)
    putExtra("tipoIncidente", tipoIncidente)
    putExtra("descripcion", descripcion)
    putExtra("latitud", latitud)
    putExtra("longitud", longitud)
    putExtra("prioridad", prioridad)
    putExtra("fechaReporte", fechaReporte)
    putExtra("estado", "REPORTADO")
}

startActivity(intentSaul)
```

`latitud` y `longitud` deben ser `Double`.

En el manifest de Priscila conviene declarar:

```xml
<queries>
    <package android:name="com.epn.atencionurbana.saul" />
</queries>
```

## 3. Activity receptora de Claudio

La Activity de Claudio debe tener `android:exported="true"` y este filtro:

```xml
<intent-filter>
    <action android:name="com.epn.resolucionurbana.claudio.action.RECIBIR_INCIDENCIA" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

Claudio debe leer:

```text
idIncidente: String
tipoIncidente: String
descripcion: String
latitud: Double
longitud: Double
prioridad: String
fechaReporte: String
estado: String
nombreInspector: String
brigadaAsignada: String
latitudBrigada: Double
longitudBrigada: Double
resultadoInspeccion: String
fechaInspeccion: String
prioridadConfirmada: String
```

El estado enviado por Saul será siempre:

```text
EN_ATENCION
```

## 4. Verificación de extras

Datos reenviados sin alterar:

```text
idIncidente
tipoIncidente
descripcion
latitud
longitud
prioridad
fechaReporte
```

Dato actualizado por Saul:

```text
estado = EN_ATENCION
```

Datos agregados por Saul:

```text
nombreInspector
brigadaAsignada
latitudBrigada
longitudBrigada
resultadoInspeccion
fechaInspeccion
prioridadConfirmada
```

No se envían todavía:

```text
trabajoRealizado
materialesUtilizados
responsableResolucion
fechaResolucion
observacionesFinales
estadoFinal
```

## 5. Pruebas manuales

1. Abrir AtencionUrbana desde el launcher sin extras.
2. Confirmar que el botón de envío esté desactivado.
3. Enviar una incidencia desde ReporteUrbano.
4. Confirmar que se muestren ID, tipo, descripción, prioridad, fecha, estado y coordenadas.
5. Confirmar que el marcador de la incidencia esté centrado en el mapa.
6. Confirmar que se muestren tres brigadas.
7. Seleccionar cada brigada desde el selector.
8. Seleccionar una brigada tocando su marcador.
9. Confirmar que cambien la polilínea y la distancia.
10. Intentar enviar sin nombre del inspector.
11. Intentar enviar sin resultado de inspección.
12. Intentar enviar sin prioridad confirmada.
13. Confirmar que `latitud`, `longitud`, `latitudBrigada` y `longitudBrigada` lleguen como `Double`.
14. Confirmar que `prioridad` y `prioridadConfirmada` lleguen por separado.
15. Confirmar que `estado` llegue como `EN_ATENCION`.
16. Desinstalar ResolucionUrbana y comprobar el mensaje de error.
17. Enviar una segunda incidencia con AtencionUrbana ya abierta.
18. Rotar la pantalla y comprobar que no se pierda la selección ni falle la aplicación.
