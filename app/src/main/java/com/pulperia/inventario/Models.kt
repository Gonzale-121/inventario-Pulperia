package com.pulperia.inventario

import java.util.Locale

data class Product(
    val id: Long = 0,
    val name: String,
    val price: Double,
    val stock: Int,
    val barcode: String? = null,
    val minStock: Int = 5
) {
    val value: Double get() = price * stock
    val isLowStock: Boolean get() = stock <= minStock
}

data class Movement(
    val id: Long,
    val productId: Long,
    val productName: String,
    val qty: Int,
    val type: String, // "IN" o "OUT"
    val date: String
)

fun Double.money(): String = String.format(Locale.US, "L %,.2f", this)
