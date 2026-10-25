package com.pulperia.inventario

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pulperia.inventario.databinding.DialogProductBinding
import com.pulperia.inventario.databinding.FragmentProductsBinding

class ProductsFragment : Fragment() {

    private var _b: FragmentProductsBinding? = null
    private val b get() = _b!!
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentProductsBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        db = DatabaseHelper.getInstance(requireContext())
        adapter = ProductAdapter(onClick = { showDialog(it) }, onDelete = { confirmDelete(it) })
        b.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        b.rvProducts.adapter = adapter
        b.fabAdd.setOnClickListener { showDialog(null) }
        b.etSearch.doAfterTextChanged { load() }
        load()
    }

    private fun load() = adapter.submit(db.getProducts(b.etSearch.text.toString()))

    private fun showDialog(existing: Product?) {
        val d = DialogProductBinding.inflate(layoutInflater)
        existing?.let {
            d.etName.setText(it.name)
            d.etPrice.setText(it.price.toString())
            d.etStock.setText(it.stock.toString())
            d.etMinStock.setText(it.minStock.toString())
            d.etBarcode.setText(it.barcode ?: "")
        } ?: d.etMinStock.setText("5")

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) "Nuevo producto" else "Editar producto")
            .setView(d.root)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = d.etName.text.toString().trim()
                val price = d.etPrice.text.toString().toDoubleOrNull()
                val stock = d.etStock.text.toString().toIntOrNull()
                val min = d.etMinStock.text.toString().toIntOrNull()
                val barcode = d.etBarcode.text.toString().trim().ifEmpty { null }

                when {
                    name.isEmpty() -> d.etName.error = "Requerido"
                    price == null || price < 0 -> d.etPrice.error = "Precio inválido"
                    stock == null || stock < 0 -> d.etStock.error = "Stock inválido"
                    min == null || min < 0 -> d.etMinStock.error = "Valor inválido"
                    else -> {
                        val p = Product(existing?.id ?: 0, name, price, stock, barcode, min)
                        val ok = if (existing == null) db.insertProduct(p) else db.updateProduct(p)
                        if (!ok) {
                            d.etBarcode.error = "Este código ya está registrado"
                        } else {
                            dialog.dismiss()
                            load()
                            alertIfLowStock(p)
                        }
                    }
                }
            }
        }
        dialog.show()
    }

    private fun confirmDelete(p: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar producto")
            .setMessage("¿Eliminar \"${p.name}\"? También se borrarán sus movimientos.")
            .setPositiveButton("Eliminar") { _, _ ->
                db.deleteProduct(p.id)
                load()
                Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
