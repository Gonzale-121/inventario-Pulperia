package com.pulperia.inventario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.pulperia.inventario.databinding.FragmentSummaryBinding

class SummaryFragment : Fragment() {

    private var _b: FragmentSummaryBinding? = null
    private val b get() = _b!!
    private val adapter = SummaryAdapter()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSummaryBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.rvTop.layoutManager = LinearLayoutManager(requireContext())
        b.rvTop.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        val db = DatabaseHelper.getInstance(requireContext())
        b.tvTotal.text = db.getTotalValue().money()
        adapter.submit(db.getTopProducts(5))

        val low = db.getLowStockProducts()
        if (low.isEmpty()) {
            b.tvLowStock.visibility = View.GONE
        } else {
            b.tvLowStock.visibility = View.VISIBLE
            b.tvLowStock.text = "⚠ Stock bajo:\n" +
                    low.joinToString("\n") { "• ${it.name}: ${it.stock} (mín. ${it.minStock})" }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
