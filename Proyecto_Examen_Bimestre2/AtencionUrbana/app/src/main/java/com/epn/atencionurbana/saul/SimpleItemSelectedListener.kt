package com.epn.atencionurbana.saul

import android.view.View
import android.widget.AdapterView

class SimpleItemSelectedListener(
    private val alSeleccionar: (Int) -> Unit
) : AdapterView.OnItemSelectedListener {

    override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ) {
        alSeleccionar(position)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        alSeleccionar(0)
    }
}
