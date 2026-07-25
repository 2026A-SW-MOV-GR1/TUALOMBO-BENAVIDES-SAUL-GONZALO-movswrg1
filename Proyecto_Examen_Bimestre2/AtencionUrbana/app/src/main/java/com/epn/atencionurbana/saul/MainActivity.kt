package com.epn.atencionurbana.saul

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var contenidoPrincipal: View
    private lateinit var tarjetaMensaje: MaterialCardView
    private lateinit var txtMensajeSinIncidencia: TextView

    private lateinit var txtIdIncidente: TextView
    private lateinit var txtTipoIncidente: TextView
    private lateinit var txtDescripcion: TextView
    private lateinit var txtPrioridadOriginal: TextView
    private lateinit var txtFechaReporte: TextView
    private lateinit var txtEstadoRecibido: TextView
    private lateinit var txtCoordenadas: TextView

    private lateinit var tarjetaBrigadaSeleccionada: MaterialCardView
    private lateinit var txtBrigadaSeleccionada: TextView
    private lateinit var txtDistancia: TextView
    private lateinit var txtTiempoEstimado: TextView

    private lateinit var tilNombreInspector: TextInputLayout
    private lateinit var edtNombreInspector: TextInputEditText
    private lateinit var tilBrigada: TextInputLayout
    private lateinit var autoBrigada: MaterialAutoCompleteTextView
    private lateinit var tilResultadoInspeccion: TextInputLayout
    private lateinit var edtResultadoInspeccion: TextInputEditText
    private lateinit var tilPrioridadConfirmada: TextInputLayout
    private lateinit var autoPrioridad: MaterialAutoCompleteTextView

    private lateinit var btnCentrarIncidente: MaterialButton
    private lateinit var btnVerTodo: MaterialButton
    private lateinit var btnBrigadaCercana: MaterialButton
    private lateinit var btnCambiarMapa: MaterialButton
    private lateinit var btnAlternarBrigadas: MaterialButton
    private lateinit var btnAbrirMaps: MaterialButton
    private lateinit var btnLimpiarRuta: MaterialButton
    private lateinit var btnEnviarResolucion: MaterialButton

    private var mapaGoogle: GoogleMap? = null
    private var marcadorIncidente: Marker? = null
    private val marcadoresBrigadas = mutableListOf<Marker>()

    private var polilineaActual: Polyline? = null
    private var marcadorConsulta: Marker? = null
    private var polilineaConsulta: Polyline? = null

    private var idIncidente: String = ""
    private var tipoIncidente: String = ""
    private var descripcion: String = ""
    private var latitudIncidente: Double = Double.NaN
    private var longitudIncidente: Double = Double.NaN
    private var prioridadOriginal: String = ""
    private var fechaReporte: String = ""
    private var estadoRecibido: String = ""

    private var brigadas: List<Brigada> = emptyList()
    private var brigadaSeleccionada: Brigada? = null

    private var nombreBrigadaRestaurada: String = ""
    private var prioridadRestaurada: String = ""
    private var mapaSatelital = false
    private var brigadasVisibles = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nombreBrigadaRestaurada =
            savedInstanceState?.getString(CLAVE_BRIGADA).orEmpty()
        prioridadRestaurada =
            savedInstanceState?.getString(CLAVE_PRIORIDAD).orEmpty()
        mapaSatelital =
            savedInstanceState?.getBoolean(CLAVE_MAPA_SATELITAL, false) ?: false
        brigadasVisibles =
            savedInstanceState?.getBoolean(CLAVE_BRIGADAS_VISIBLES, true) ?: true

        vincularVistas()
        configurarSelectorPrioridad()
        configurarSelectorBrigadas()
        configurarAccionesMapa()
        configurarBotonEnvio()
        configurarMapa()

        procesarIntent(
            intent = intent,
            limpiarFormulario = savedInstanceState == null
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapaGoogle = googleMap

        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isMapToolbarEnabled = false
            isCompassEnabled = true
            isScrollGesturesEnabled = true
            isZoomGesturesEnabled = true
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
        }

        googleMap.setMinZoomPreference(4f)
        googleMap.setMaxZoomPreference(20f)
        googleMap.setPadding(
            0,
            convertirDpAPixeles(12),
            0,
            convertirDpAPixeles(12)
        )

        googleMap.mapType = if (mapaSatelital) {
            GoogleMap.MAP_TYPE_HYBRID
        } else {
            GoogleMap.MAP_TYPE_NORMAL
        }

        googleMap.setOnMarkerClickListener { marcador ->
            val indiceBrigada = marcador.tag as? Int

            if (indiceBrigada != null && indiceBrigada in brigadas.indices) {
                val brigada = brigadas[indiceBrigada]
                autoBrigada.setText(brigada.nombre, false)
                seleccionarBrigada(brigada, moverCamara = false)
                marcador.showInfoWindow()
                true
            } else {
                false
            }
        }

        googleMap.setOnMapLongClickListener { posicion ->
            consultarDistanciaEnMapa(posicion)
        }

        actualizarEtiquetasBotonesMapa()
        actualizarMapa()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        nombreBrigadaRestaurada = ""
        prioridadRestaurada = ""

        procesarIntent(
            intent = intent,
            limpiarFormulario = true
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(
            CLAVE_BRIGADA,
            brigadaSeleccionada?.nombre.orEmpty()
        )
        outState.putString(
            CLAVE_PRIORIDAD,
            autoPrioridad.text?.toString().orEmpty()
        )
        outState.putBoolean(
            CLAVE_MAPA_SATELITAL,
            mapaSatelital
        )
        outState.putBoolean(
            CLAVE_BRIGADAS_VISIBLES,
            brigadasVisibles
        )
        super.onSaveInstanceState(outState)
    }

    private fun vincularVistas() {
        contenidoPrincipal = findViewById(R.id.contenidoPrincipal)
        tarjetaMensaje = findViewById(R.id.tarjetaMensaje)
        txtMensajeSinIncidencia = findViewById(R.id.txtMensajeSinIncidencia)

        txtIdIncidente = findViewById(R.id.txtIdIncidente)
        txtTipoIncidente = findViewById(R.id.txtTipoIncidente)
        txtDescripcion = findViewById(R.id.txtDescripcion)
        txtPrioridadOriginal = findViewById(R.id.txtPrioridadOriginal)
        txtFechaReporte = findViewById(R.id.txtFechaReporte)
        txtEstadoRecibido = findViewById(R.id.txtEstadoRecibido)
        txtCoordenadas = findViewById(R.id.txtCoordenadas)

        tarjetaBrigadaSeleccionada =
            findViewById(R.id.tarjetaBrigadaSeleccionada)
        txtBrigadaSeleccionada =
            findViewById(R.id.txtBrigadaSeleccionada)
        txtDistancia = findViewById(R.id.txtDistancia)
        txtTiempoEstimado = findViewById(R.id.txtTiempoEstimado)

        tilNombreInspector = findViewById(R.id.tilNombreInspector)
        edtNombreInspector = findViewById(R.id.edtNombreInspector)
        tilBrigada = findViewById(R.id.tilBrigada)
        autoBrigada = findViewById(R.id.autoBrigada)
        tilResultadoInspeccion = findViewById(R.id.tilResultadoInspeccion)
        edtResultadoInspeccion = findViewById(R.id.edtResultadoInspeccion)
        tilPrioridadConfirmada =
            findViewById(R.id.tilPrioridadConfirmada)
        autoPrioridad = findViewById(R.id.autoPrioridad)

        btnCentrarIncidente = findViewById(R.id.btnCentrarIncidente)
        btnVerTodo = findViewById(R.id.btnVerTodo)
        btnBrigadaCercana = findViewById(R.id.btnBrigadaCercana)
        btnCambiarMapa = findViewById(R.id.btnCambiarMapa)
        btnAlternarBrigadas = findViewById(R.id.btnAlternarBrigadas)
        btnAbrirMaps = findViewById(R.id.btnAbrirMaps)
        btnLimpiarRuta = findViewById(R.id.btnLimpiarRuta)
        btnEnviarResolucion = findViewById(R.id.btnEnviarResolucion)
    }

    private fun configurarMapa() {
        val fragmentoMapa = supportFragmentManager
            .findFragmentById(R.id.fragmentMapa) as? SupportMapFragment

        if (fragmentoMapa == null) {
            txtMensajeSinIncidencia.text =
                getString(R.string.error_cargar_mapa)
            tarjetaMensaje.visibility = View.VISIBLE
            return
        }

        fragmentoMapa.getMapAsync(this)
    }

    private fun configurarSelectorPrioridad() {
        val prioridades = resources.getStringArray(
            R.array.prioridades_inspeccion
        )

        val adaptadorPrioridades = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            prioridades
        )

        autoPrioridad.apply {
            threshold = 0
            keyListener = null
            setAdapter(adaptadorPrioridades)

            setOnClickListener {
                tilPrioridadConfirmada.error = null
                showDropDown()
            }

            setOnFocusChangeListener { _, tieneFoco ->
                if (tieneFoco) {
                    showDropDown()
                }
            }

            setOnItemClickListener { _, _, _, _ ->
                tilPrioridadConfirmada.error = null
            }
        }

        tilPrioridadConfirmada.setEndIconOnClickListener {
            autoPrioridad.showDropDown()
        }

        if (prioridadRestaurada.isNotBlank()) {
            autoPrioridad.setText(prioridadRestaurada, false)
        }
    }

    private fun configurarSelectorBrigadas() {
        autoBrigada.apply {
            threshold = 0
            keyListener = null

            setOnClickListener {
                if (brigadas.isEmpty()) {
                    tilBrigada.error =
                        getString(R.string.error_sin_incidencia_valida)
                    mostrarMensaje(R.string.error_sin_incidencia_valida)
                } else {
                    tilBrigada.error = null
                    showDropDown()
                }
            }

            setOnFocusChangeListener { _, tieneFoco ->
                if (tieneFoco && brigadas.isNotEmpty()) {
                    showDropDown()
                }
            }

            setOnItemClickListener { _, _, posicion, _ ->
                val brigada = brigadas.getOrNull(posicion)

                if (brigada != null) {
                    seleccionarBrigada(
                        brigada = brigada,
                        moverCamara = false
                    )
                }
            }
        }

        tilBrigada.setEndIconOnClickListener {
            if (brigadas.isEmpty()) {
                tilBrigada.error =
                    getString(R.string.error_sin_incidencia_valida)
                mostrarMensaje(R.string.error_sin_incidencia_valida)
            } else {
                tilBrigada.error = null
                autoBrigada.showDropDown()
            }
        }
    }

    private fun actualizarSelectorBrigadas() {
        val nombresBrigadas = brigadas.map { brigada ->
            brigada.nombre
        }

        val adaptadorBrigadas = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            nombresBrigadas
        )

        autoBrigada.setAdapter(adaptadorBrigadas)

        val brigadaRestaurada = brigadas.firstOrNull { brigada ->
            brigada.nombre == nombreBrigadaRestaurada
        }

        val brigadaInicial = brigadaRestaurada
            ?: brigadas.minByOrNull { brigada ->
                calcularDistanciaMetros(brigada)
            }

        if (brigadaInicial != null) {
            autoBrigada.setText(brigadaInicial.nombre, false)
            seleccionarBrigada(
                brigada = brigadaInicial,
                moverCamara = false
            )
        } else {
            autoBrigada.setText("", false)
            brigadaSeleccionada = null
            ocultarResumenBrigada()
        }
    }

    private fun configurarAccionesMapa() {
        btnCentrarIncidente.setOnClickListener {
            centrarEnIncidente()
        }

        btnVerTodo.setOnClickListener {
            mostrarTodosLosPuntos()
        }

        btnBrigadaCercana.setOnClickListener {
            seleccionarBrigadaMasCercana()
        }

        btnCambiarMapa.setOnClickListener {
            alternarTipoMapa()
        }

        btnAlternarBrigadas.setOnClickListener {
            alternarVisibilidadBrigadas()
        }

        btnAbrirMaps.setOnClickListener {
            abrirRutaEnGoogleMaps()
        }

        btnLimpiarRuta.setOnClickListener {
            limpiarSeleccionBrigada()
        }

        actualizarEtiquetasBotonesMapa()
    }

    private fun configurarBotonEnvio() {
        btnEnviarResolucion.setOnClickListener {
            enviarIncidenciaAClaudio()
        }
    }

    private fun procesarIntent(
        intent: Intent?,
        limpiarFormulario: Boolean
    ) {
        val tieneIdentificador = intent?.hasExtra(
            ContratoIncidencia.EXTRA_ID_INCIDENTE
        ) == true

        if (!tieneIdentificador) {
            mostrarEstadoSinIncidencia(
                getString(R.string.mensaje_sin_incidencia)
            )
            return
        }

        idIncidente = intent
            ?.getStringExtra(ContratoIncidencia.EXTRA_ID_INCIDENTE)
            .orEmpty()
            .trim()

        tipoIncidente = intent
            ?.getStringExtra(ContratoIncidencia.EXTRA_TIPO_INCIDENTE)
            .orEmpty()
            .trim()

        descripcion = intent
            ?.getStringExtra(ContratoIncidencia.EXTRA_DESCRIPCION)
            .orEmpty()
            .trim()

        latitudIncidente = intent?.getDoubleExtra(
            ContratoIncidencia.EXTRA_LATITUD,
            Double.NaN
        ) ?: Double.NaN

        longitudIncidente = intent?.getDoubleExtra(
            ContratoIncidencia.EXTRA_LONGITUD,
            Double.NaN
        ) ?: Double.NaN

        prioridadOriginal = intent
            ?.getStringExtra(ContratoIncidencia.EXTRA_PRIORIDAD)
            .orEmpty()
            .trim()

        fechaReporte = intent
            ?.getStringExtra(ContratoIncidencia.EXTRA_FECHA_REPORTE)
            .orEmpty()
            .trim()

        estadoRecibido = intent
            ?.getStringExtra(ContratoIncidencia.EXTRA_ESTADO)
            .orEmpty()
            .trim()

        if (!incidenciaRecibidaEsValida()) {
            mostrarEstadoSinIncidencia(
                getString(R.string.mensaje_incidencia_invalida)
            )
            return
        }

        tarjetaMensaje.visibility = View.GONE
        actualizarDisponibilidadAcciones(true)
        mostrarInformacionRecibida()
        generarBrigadasDisponibles()

        if (limpiarFormulario) {
            limpiarFormularioInspeccion()
        }

        actualizarSelectorBrigadas()
        actualizarMapa()
    }

    private fun incidenciaRecibidaEsValida(): Boolean {
        return idIncidente.isNotBlank() &&
                latitudIncidente.isFinite() &&
                longitudIncidente.isFinite() &&
                latitudIncidente in -90.0..90.0 &&
                longitudIncidente in -180.0..180.0
    }

    private fun mostrarEstadoSinIncidencia(mensaje: String) {
        idIncidente = ""
        tipoIncidente = ""
        descripcion = ""
        latitudIncidente = Double.NaN
        longitudIncidente = Double.NaN
        prioridadOriginal = ""
        fechaReporte = ""
        estadoRecibido = ""

        brigadas = emptyList()
        brigadaSeleccionada = null

        tarjetaMensaje.visibility = View.VISIBLE
        txtMensajeSinIncidencia.text = mensaje

        txtTipoIncidente.text = getString(R.string.sin_incidencia)
        txtIdIncidente.text = getString(R.string.id_sin_dato)
        txtEstadoRecibido.text = getString(R.string.estado_sin_dato)
        txtPrioridadOriginal.text = getString(R.string.prioridad_sin_dato)
        txtDescripcion.text = getString(R.string.descripcion_sin_dato)
        txtFechaReporte.text = getString(R.string.fecha_sin_dato)
        txtCoordenadas.text = getString(R.string.coordenadas_sin_dato)

        autoBrigada.setText("", false)
        actualizarSelectorBrigadas()
        ocultarResumenBrigada()
        actualizarDisponibilidadAcciones(false)

        mapaGoogle?.clear()
        limpiarObjetosMapa()
    }

    private fun mostrarInformacionRecibida() {
        txtTipoIncidente.text = valorVisible(tipoIncidente)
        txtIdIncidente.text =
            getString(R.string.formato_id, valorVisible(idIncidente))
        txtEstadoRecibido.text =
            getString(
                R.string.formato_estado_chip,
                valorVisible(estadoRecibido)
            )
        txtPrioridadOriginal.text =
            getString(
                R.string.formato_prioridad_chip,
                valorVisible(prioridadOriginal)
            )
        txtDescripcion.text = valorVisible(descripcion)
        txtFechaReporte.text =
            getString(
                R.string.formato_fecha_reporte,
                valorVisible(fechaReporte)
            )
        txtCoordenadas.text =
            getString(
                R.string.formato_coordenadas,
                latitudIncidente,
                longitudIncidente
            )
    }

    private fun valorVisible(valor: String): String {
        return valor.ifBlank {
            getString(R.string.sin_dato)
        }
    }

    private fun generarBrigadasDisponibles() {
        val desplazamiento = 0.006

        brigadas = listOf(
            Brigada(
                nombre = getString(R.string.brigada_norte),
                latitud = (latitudIncidente + desplazamiento)
                    .coerceIn(-90.0, 90.0),
                longitud = longitudIncidente
            ),
            Brigada(
                nombre = getString(R.string.brigada_centro),
                latitud = (latitudIncidente - 0.002)
                    .coerceIn(-90.0, 90.0),
                longitud = (longitudIncidente + 0.005)
                    .coerceIn(-180.0, 180.0)
            ),
            Brigada(
                nombre = getString(R.string.brigada_sur),
                latitud = (latitudIncidente - desplazamiento)
                    .coerceIn(-90.0, 90.0),
                longitud = (longitudIncidente - 0.003)
                    .coerceIn(-180.0, 180.0)
            )
        )
    }

    private fun actualizarMapa() {
        val mapa = mapaGoogle ?: return

        mapa.clear()
        limpiarObjetosMapa()

        if (!incidenciaRecibidaEsValida()) {
            return
        }

        val posicionIncidente = LatLng(
            latitudIncidente,
            longitudIncidente
        )

        mapa.addCircle(
            CircleOptions()
                .center(posicionIncidente)
                .radius(RADIO_ATENCION_METROS)
                .strokeWidth(3f)
                .strokeColor(Color.argb(190, 198, 40, 40))
                .fillColor(Color.argb(34, 198, 40, 40))
        )

        marcadorIncidente = mapa.addMarker(
            MarkerOptions()
                .position(posicionIncidente)
                .title(
                    getString(
                        R.string.marcador_incidente,
                        valorVisible(tipoIncidente)
                    )
                )
                .snippet(
                    getString(
                        R.string.marcador_estado,
                        valorVisible(estadoRecibido)
                    )
                )
                .icon(
                    BitmapDescriptorFactory.defaultMarker(
                        colorMarcadorIncidente()
                    )
                )
        )

        marcadoresBrigadas.clear()

        brigadas.forEachIndexed { indice, brigada ->
            val marcador = mapa.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(brigada.latitud, brigada.longitud)
                    )
                    .title(brigada.nombre)
                    .snippet(
                        getString(
                            R.string.marcador_brigada_distancia,
                            formatearDistancia(
                                calcularDistanciaMetros(brigada)
                            )
                        )
                    )
                    .visible(brigadasVisibles)
                    .icon(
                        BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE
                        )
                    )
            )

            marcador?.tag = indice

            if (marcador != null) {
                marcadoresBrigadas.add(marcador)
            }
        }

        mapa.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                posicionIncidente,
                14.5f
            )
        )

        if (brigadaSeleccionada != null) {
            dibujarRutaYDistancia(moverCamara = false)
        }
    }

    private fun colorMarcadorIncidente(): Float {
        val tipoNormalizado = tipoIncidente.lowercase(Locale.getDefault())

        return when {
            "agua" in tipoNormalizado ||
                    "fuga" in tipoNormalizado -> BitmapDescriptorFactory.HUE_CYAN

            "basura" in tipoNormalizado -> BitmapDescriptorFactory.HUE_GREEN
            "luminaria" in tipoNormalizado -> BitmapDescriptorFactory.HUE_YELLOW
            "semáforo" in tipoNormalizado ||
                    "semaforo" in tipoNormalizado -> BitmapDescriptorFactory.HUE_ORANGE

            "acera" in tipoNormalizado -> BitmapDescriptorFactory.HUE_VIOLET
            "alcantarilla" in tipoNormalizado -> BitmapDescriptorFactory.HUE_ROSE
            else -> BitmapDescriptorFactory.HUE_RED
        }
    }

    private fun seleccionarBrigada(
        brigada: Brigada,
        moverCamara: Boolean
    ) {
        brigadaSeleccionada = brigada
        nombreBrigadaRestaurada = brigada.nombre
        tilBrigada.error = null
        autoBrigada.setText(brigada.nombre, false)
        dibujarRutaYDistancia(moverCamara)
    }

    private fun dibujarRutaYDistancia(moverCamara: Boolean) {
        val mapa = mapaGoogle ?: return
        val brigada = brigadaSeleccionada ?: return

        if (!incidenciaRecibidaEsValida()) {
            return
        }

        polilineaActual?.remove()

        val posicionIncidente = LatLng(
            latitudIncidente,
            longitudIncidente
        )
        val posicionBrigada = LatLng(
            brigada.latitud,
            brigada.longitud
        )

        polilineaActual = mapa.addPolyline(
            PolylineOptions()
                .add(posicionBrigada, posicionIncidente)
                .width(10f)
                .color(Color.rgb(11, 87, 208))
                .geodesic(true)
        )

        val distanciaMetros = calcularDistanciaMetros(brigada)
        val minutosEstimados = calcularTiempoEstimado(distanciaMetros)

        tarjetaBrigadaSeleccionada.visibility = View.VISIBLE
        txtBrigadaSeleccionada.text = brigada.nombre
        txtDistancia.text =
            getString(
                R.string.formato_distancia_resumen,
                formatearDistancia(distanciaMetros)
            )
        txtTiempoEstimado.text =
            getString(
                R.string.tiempo_estimado,
                minutosEstimados
            )

        if (moverCamara) {
            enfocarRuta(posicionBrigada, posicionIncidente)
        }
    }

    private fun calcularDistanciaMetros(brigada: Brigada): Double {
        val resultado = FloatArray(1)

        Location.distanceBetween(
            brigada.latitud,
            brigada.longitud,
            latitudIncidente,
            longitudIncidente,
            resultado
        )

        return resultado.firstOrNull()?.toDouble() ?: 0.0
    }

    private fun calcularTiempoEstimado(distanciaMetros: Double): Int {
        val distanciaKilometros = distanciaMetros / 1000.0
        val minutos = distanciaKilometros / VELOCIDAD_URBANA_KM_H * 60.0

        return max(1, minutos.roundToInt())
    }

    private fun formatearDistancia(distanciaMetros: Double): String {
        return if (distanciaMetros < 1000.0) {
            getString(
                R.string.valor_distancia_metros,
                distanciaMetros
            )
        } else {
            getString(
                R.string.valor_distancia_kilometros,
                distanciaMetros / 1000.0
            )
        }
    }

    private fun centrarEnIncidente() {
        val mapa = mapaGoogle ?: return

        if (!incidenciaRecibidaEsValida()) {
            mostrarMensaje(R.string.error_sin_incidencia_valida)
            return
        }

        mapa.stopAnimation()
        mapa.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(latitudIncidente, longitudIncidente),
                16f
            )
        )

        marcadorIncidente?.showInfoWindow()
    }

    private fun mostrarTodosLosPuntos() {
        val mapa = mapaGoogle ?: return

        if (!incidenciaRecibidaEsValida()) {
            mostrarMensaje(R.string.error_sin_incidencia_valida)
            return
        }

        val constructor = LatLngBounds.Builder()
        constructor.include(
            LatLng(latitudIncidente, longitudIncidente)
        )

        brigadas.forEach { brigada ->
            constructor.include(
                LatLng(brigada.latitud, brigada.longitud)
            )
        }

        try {
            mapa.stopAnimation()
            mapa.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    constructor.build(),
                    PADDING_MAPA
                )
            )
        } catch (_: IllegalStateException) {
            centrarEnIncidente()
        }
    }

    private fun seleccionarBrigadaMasCercana() {
        if (!incidenciaRecibidaEsValida() || brigadas.isEmpty()) {
            mostrarMensaje(R.string.error_sin_incidencia_valida)
            return
        }

        val brigadaCercana = brigadas.minByOrNull { brigada ->
            calcularDistanciaMetros(brigada)
        } ?: return

        if (!brigadasVisibles) {
            brigadasVisibles = true
            marcadoresBrigadas.forEach { marcador ->
                marcador.isVisible = true
            }
            actualizarEtiquetasBotonesMapa()
        }

        seleccionarBrigada(brigadaCercana, moverCamara = true)

        mostrarMensaje(
            getString(
                R.string.mensaje_brigada_cercana,
                brigadaCercana.nombre
            )
        )
    }

    private fun alternarTipoMapa() {
        val mapa = mapaGoogle ?: return

        mapaSatelital = !mapaSatelital
        mapa.mapType = if (mapaSatelital) {
            GoogleMap.MAP_TYPE_HYBRID
        } else {
            GoogleMap.MAP_TYPE_NORMAL
        }

        actualizarEtiquetasBotonesMapa()
    }

    private fun alternarVisibilidadBrigadas() {
        if (!incidenciaRecibidaEsValida()) {
            mostrarMensaje(R.string.error_sin_incidencia_valida)
            return
        }

        brigadasVisibles = !brigadasVisibles

        marcadoresBrigadas.forEach { marcador ->
            marcador.isVisible = brigadasVisibles
        }

        actualizarEtiquetasBotonesMapa()
    }

    private fun actualizarEtiquetasBotonesMapa() {
        if (::btnCambiarMapa.isInitialized) {
            btnCambiarMapa.text = if (mapaSatelital) {
                getString(R.string.accion_vista_normal)
            } else {
                getString(R.string.accion_vista_satelite)
            }
        }

        if (::btnAlternarBrigadas.isInitialized) {
            btnAlternarBrigadas.text = if (brigadasVisibles) {
                getString(R.string.accion_ocultar_brigadas)
            } else {
                getString(R.string.accion_mostrar_brigadas)
            }
        }
    }

    private fun consultarDistanciaEnMapa(posicion: LatLng) {
        val mapa = mapaGoogle ?: return

        if (!incidenciaRecibidaEsValida()) {
            return
        }

        marcadorConsulta?.remove()
        polilineaConsulta?.remove()

        marcadorConsulta = mapa.addMarker(
            MarkerOptions()
                .position(posicion)
                .title(getString(R.string.marcador_punto_consultado))
                .icon(
                    BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_ORANGE
                    )
                )
        )

        val posicionIncidente = LatLng(
            latitudIncidente,
            longitudIncidente
        )

        polilineaConsulta = mapa.addPolyline(
            PolylineOptions()
                .add(posicionIncidente, posicion)
                .width(7f)
                .color(Color.rgb(230, 126, 34))
                .pattern(
                    listOf(
                        Dash(24f),
                        Gap(16f)
                    )
                )
        )

        val resultado = FloatArray(1)
        Location.distanceBetween(
            latitudIncidente,
            longitudIncidente,
            posicion.latitude,
            posicion.longitude,
            resultado
        )

        val distancia = resultado.firstOrNull()?.toDouble() ?: 0.0

        mostrarMensaje(
            getString(
                R.string.mensaje_distancia_consultada,
                formatearDistancia(distancia)
            )
        )
    }

    private fun enfocarRuta(
        origen: LatLng,
        destino: LatLng
    ) {
        val mapa = mapaGoogle ?: return
        val limites = LatLngBounds.Builder()
            .include(origen)
            .include(destino)
            .build()

        try {
            mapa.stopAnimation()
            mapa.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    limites,
                    PADDING_RUTA
                )
            )
        } catch (_: IllegalStateException) {
            mapa.animateCamera(
                CameraUpdateFactory.newLatLngZoom(destino, 15f)
            )
        }
    }

    private fun abrirRutaEnGoogleMaps() {
        val brigada = brigadaSeleccionada

        if (brigada == null) {
            tilBrigada.error = getString(R.string.error_brigada)
            autoBrigada.requestFocus()
            return
        }

        val uri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1" +
                    "&origin=${brigada.latitud},${brigada.longitud}" +
                    "&destination=$latitudIncidente,$longitudIncidente" +
                    "&travelmode=driving"
        )

        val intentGoogleMaps = Intent(
            Intent.ACTION_VIEW,
            uri
        ).apply {
            setPackage(PAQUETE_GOOGLE_MAPS)
        }

        try {
            startActivity(intentGoogleMaps)
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (_: ActivityNotFoundException) {
                mostrarMensaje(R.string.error_abrir_mapa_externo)
            }
        }
    }

    private fun limpiarSeleccionBrigada() {
        brigadaSeleccionada = null
        nombreBrigadaRestaurada = ""
        autoBrigada.setText("", false)

        polilineaActual?.remove()
        polilineaActual = null

        ocultarResumenBrigada()
        centrarEnIncidente()
    }

    private fun ocultarResumenBrigada() {
        tarjetaBrigadaSeleccionada.visibility = View.GONE
        txtBrigadaSeleccionada.text = ""
        txtDistancia.text = ""
        txtTiempoEstimado.text = ""
    }

    private fun limpiarObjetosMapa() {
        marcadorIncidente = null
        marcadoresBrigadas.clear()
        polilineaActual = null
        marcadorConsulta = null
        polilineaConsulta = null
    }

    private fun actualizarDisponibilidadAcciones(habilitar: Boolean) {
        btnEnviarResolucion.isEnabled = habilitar
        btnCentrarIncidente.isEnabled = habilitar
        btnVerTodo.isEnabled = habilitar
        btnBrigadaCercana.isEnabled = habilitar
        btnCambiarMapa.isEnabled = habilitar
        btnAlternarBrigadas.isEnabled = habilitar

        // Estos dos campos siempre deben permanecer disponibles.
        tilBrigada.isEnabled = true
        autoBrigada.isEnabled = true
        tilPrioridadConfirmada.isEnabled = true
        autoPrioridad.isEnabled = true
    }

    private fun limpiarFormularioInspeccion() {
        edtNombreInspector.setText("")
        edtResultadoInspeccion.setText("")
        autoBrigada.setText("", false)
        autoPrioridad.setText("", false)

        tilNombreInspector.error = null
        tilBrigada.error = null
        tilResultadoInspeccion.error = null
        tilPrioridadConfirmada.error = null

        brigadaSeleccionada = null
        nombreBrigadaRestaurada = ""
        prioridadRestaurada = ""

        polilineaActual?.remove()
        polilineaActual = null
        ocultarResumenBrigada()
    }

    private fun validarFormulario(): Boolean {
        tilNombreInspector.error = null
        tilBrigada.error = null
        tilResultadoInspeccion.error = null
        tilPrioridadConfirmada.error = null

        var primerCampoConError: View? = null

        if (!incidenciaRecibidaEsValida()) {
            tarjetaMensaje.visibility = View.VISIBLE
            txtMensajeSinIncidencia.text =
                getString(R.string.error_sin_incidencia_valida)
            actualizarDisponibilidadAcciones(false)
            contenidoPrincipal.requestFocus()
            return false
        }

        val nombreInspector = edtNombreInspector
            .text
            ?.toString()
            .orEmpty()
            .trim()

        if (nombreInspector.isBlank()) {
            tilNombreInspector.error =
                getString(R.string.error_nombre_inspector)
            primerCampoConError = edtNombreInspector
        }

        if (brigadaSeleccionada == null) {
            tilBrigada.error =
                getString(R.string.error_brigada)

            if (primerCampoConError == null) {
                primerCampoConError = autoBrigada
            }
        }

        val resultadoInspeccion = edtResultadoInspeccion
            .text
            ?.toString()
            .orEmpty()
            .trim()

        if (resultadoInspeccion.isBlank()) {
            tilResultadoInspeccion.error =
                getString(R.string.error_resultado_inspeccion)

            if (primerCampoConError == null) {
                primerCampoConError = edtResultadoInspeccion
            }
        }

        val prioridadConfirmada = autoPrioridad
            .text
            ?.toString()
            .orEmpty()
            .trim()

        if (prioridadConfirmada.isBlank()) {
            tilPrioridadConfirmada.error =
                getString(R.string.error_prioridad_confirmada)

            if (primerCampoConError == null) {
                primerCampoConError = autoPrioridad
            }
        }

        primerCampoConError?.requestFocus()

        return primerCampoConError == null
    }

    private fun enviarIncidenciaAClaudio() {
        if (!validarFormulario()) {
            return
        }

        val brigada = brigadaSeleccionada ?: return

        val nombreInspector = edtNombreInspector
            .text
            ?.toString()
            .orEmpty()
            .trim()

        val resultadoInspeccion = edtResultadoInspeccion
            .text
            ?.toString()
            .orEmpty()
            .trim()

        val prioridadConfirmada = autoPrioridad
            .text
            ?.toString()
            .orEmpty()
            .trim()

        val fechaInspeccion = SimpleDateFormat(
            FORMATO_FECHA,
            Locale.getDefault()
        ).format(Date())

        val intentClaudio = Intent(
            ContratoIncidencia.ACTION_RECIBIR_EN_RESOLUCION
        ).apply {
            setPackage(ContratoIncidencia.PAQUETE_CLAUDIO)

            putExtra(
                ContratoIncidencia.EXTRA_ID_INCIDENTE,
                idIncidente
            )
            putExtra(
                ContratoIncidencia.EXTRA_TIPO_INCIDENTE,
                tipoIncidente
            )
            putExtra(
                ContratoIncidencia.EXTRA_DESCRIPCION,
                descripcion
            )
            putExtra(
                ContratoIncidencia.EXTRA_LATITUD,
                latitudIncidente
            )
            putExtra(
                ContratoIncidencia.EXTRA_LONGITUD,
                longitudIncidente
            )
            putExtra(
                ContratoIncidencia.EXTRA_PRIORIDAD,
                prioridadOriginal
            )
            putExtra(
                ContratoIncidencia.EXTRA_FECHA_REPORTE,
                fechaReporte
            )
            putExtra(
                ContratoIncidencia.EXTRA_ESTADO,
                EstadoIncidencia.EN_ATENCION
            )
            putExtra(
                ContratoIncidencia.EXTRA_NOMBRE_INSPECTOR,
                nombreInspector
            )
            putExtra(
                ContratoIncidencia.EXTRA_BRIGADA_ASIGNADA,
                brigada.nombre
            )
            putExtra(
                ContratoIncidencia.EXTRA_LATITUD_BRIGADA,
                brigada.latitud
            )
            putExtra(
                ContratoIncidencia.EXTRA_LONGITUD_BRIGADA,
                brigada.longitud
            )
            putExtra(
                ContratoIncidencia.EXTRA_RESULTADO_INSPECCION,
                resultadoInspeccion
            )
            putExtra(
                ContratoIncidencia.EXTRA_FECHA_INSPECCION,
                fechaInspeccion
            )
            putExtra(
                ContratoIncidencia.EXTRA_PRIORIDAD_CONFIRMADA,
                prioridadConfirmada
            )
        }

        val actividadDisponible = packageManager.resolveActivity(
            intentClaudio,
            PackageManager.MATCH_DEFAULT_ONLY
        )

        if (actividadDisponible == null) {
            mostrarMensaje(
                R.string.error_aplicacion_claudio_no_disponible
            )
            return
        }

        try {
            startActivity(intentClaudio)
        } catch (_: ActivityNotFoundException) {
            mostrarMensaje(
                R.string.error_aplicacion_claudio_no_disponible
            )
        }
    }

    private fun mostrarMensaje(recurso: Int) {
        Snackbar.make(
            contenidoPrincipal,
            recurso,
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun mostrarMensaje(mensaje: String) {
        Snackbar.make(
            contenidoPrincipal,
            mensaje,
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun convertirDpAPixeles(dp: Int): Int {
        return (dp * resources.displayMetrics.density)
            .roundToInt()
    }

    companion object {
        private const val FORMATO_FECHA = "yyyy-MM-dd HH:mm:ss"
        private const val CLAVE_BRIGADA = "brigadaSeleccionada"
        private const val CLAVE_PRIORIDAD = "prioridadSeleccionada"
        private const val CLAVE_MAPA_SATELITAL = "mapaSatelital"
        private const val CLAVE_BRIGADAS_VISIBLES = "brigadasVisibles"

        private const val PAQUETE_GOOGLE_MAPS =
            "com.google.android.apps.maps"

        private const val VELOCIDAD_URBANA_KM_H = 25.0
        private const val RADIO_ATENCION_METROS = 250.0
        private const val PADDING_MAPA = 120
        private const val PADDING_RUTA = 150
    }
}