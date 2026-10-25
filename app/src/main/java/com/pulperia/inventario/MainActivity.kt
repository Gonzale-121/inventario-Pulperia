package com.pulperia.inventario

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pulperia.inventario.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_products -> show(ProductsFragment())
                R.id.nav_movements -> show(MovementsFragment())
                R.id.nav_summary -> show(SummaryFragment())
            }
            true
        }
        if (savedInstanceState == null) binding.bottomNav.selectedItemId = R.id.nav_products
    }

    private fun show(f: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.container, f).commit()
    }
}

/** Alerta de stock mínimo, reutilizable desde cualquier fragment. */
fun Fragment.alertIfLowStock(p: Product?) {
    if (p == null || !p.isLowStock) return
    MaterialAlertDialogBuilder(requireContext())
        .setTitle("⚠ Stock bajo")
        .setMessage("\"${p.name}\" tiene ${p.stock} unidades (mínimo: ${p.minStock}). Conviene reabastecer.")
        .setPositiveButton("Entendido", null)
        .show()
}
