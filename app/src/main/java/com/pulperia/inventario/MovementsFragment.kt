package com.pulperia.inventario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.pulperia.inventario.databinding.FragmentMovementsBinding

class MovementsFragment : Fragment() {

    private var _b: FragmentMovementsBinding? = null
    private val b get() = _b!!
    private lateinit var db: DatabaseHelper
    private val adapter = MovementAdapter()
    private var products: List<Product> = emptyList()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMovementsBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        db = DatabaseHelper.getInstance(requireContext())
        b.rvMovements.layoutManager = LinearLayoutManager(requireContext())
        b.rvMovements.adapter = adapter

        // Seleccionar producto escribiendo su código de barras
        b.etBarcodeSearch.doAfterTextChanged { text ->
            val idx = products.indexOfFirst { it.barcode == text.toString().trim() }
            if (idx >= 0) b.spProduct.setSelection(idx)
        }
        b.btnRegister.setOnClickListener { register() }
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        products = db.getProducts()
        val labels = products.map { "${it.name} (stock: ${it.stock})" }
        b.spProduct.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels)
        adapter.submit(db.getMovements())
    }

    private fun register() {
        if (products.isEmpty()) {
            toast("Primero registra un producto"); return
        }
        val qty = b.etQty.text.toString().toIntOrNull()
        if (qty == null || qty <= 0) {
            b.etQty.error = "Cantidad inválida"; return
        }
        val product = products[b.spProduct.selectedItemPosition]
        val type = if (b.rbIn.isChecked) "IN" else "OUT"

        val error = db.registerMovement(product.id, qty, type)
        if (error != null) {
            toast(error)
        } else {
            toast("Movimiento registrado")
            b.etQty.text?.clear()
            refresh()
            b.spProduct.setSelection(products.indexOfFirst { it.id == product.id }.coerceAtLeast(0))
            alertIfLowStock(db.getProduct(product.id))
        }
    }

    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
