package com.pulperia.inventario

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pulperia.inventario.databinding.ItemMovementBinding
import com.pulperia.inventario.databinding.ItemProductBinding
import com.pulperia.inventario.databinding.ItemSummaryBinding

class ProductAdapter(
    private val onClick: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.VH>() {

    private var items: List<Product> = emptyList()
    class VH(val b: ItemProductBinding) : RecyclerView.ViewHolder(b.root)

    fun submit(list: List<Product>) { items = list; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val p = items[position]
        h.b.tvName.text = p.name
        h.b.tvDetail.text = "${p.price.money()}  •  Stock: ${p.stock}" +
                if (p.isLowStock) "  ⚠ BAJO" else ""
        h.b.tvDetail.setTextColor(if (p.isLowStock) Color.parseColor("#C62828") else Color.GRAY)
        h.b.tvBarcode.text = p.barcode?.let { "Código: $it" } ?: "Sin código de barras"
        h.b.root.setOnClickListener { onClick(p) }
        h.b.btnDelete.setOnClickListener { onDelete(p) }
    }
}

class MovementAdapter : RecyclerView.Adapter<MovementAdapter.VH>() {
    private var items: List<Movement> = emptyList()
    class VH(val b: ItemMovementBinding) : RecyclerView.ViewHolder(b.root)

    fun submit(list: List<Movement>) { items = list; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemMovementBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val m = items[position]
        val isIn = m.type == "IN"
        h.b.tvTitle.text = "${if (isIn) "＋ Entrada" else "－ Salida"}: ${m.qty} × ${m.productName}"
        h.b.tvTitle.setTextColor(Color.parseColor(if (isIn) "#2E7D32" else "#C62828"))
        h.b.tvDate.text = m.date
    }
}

class SummaryAdapter : RecyclerView.Adapter<SummaryAdapter.VH>() {
    private var items: List<Product> = emptyList()
    class VH(val b: ItemSummaryBinding) : RecyclerView.ViewHolder(b.root)

    fun submit(list: List<Product>) { items = list; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val p = items[position]
        h.b.tvRank.text = "${position + 1}."
        h.b.tvName.text = "${p.name} (${p.stock} × ${p.price.money()})"
        h.b.tvValue.text = p.value.money()
    }
}
