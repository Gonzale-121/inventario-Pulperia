package com.pulperia.inventario

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, "pulperia.db", null, 1) {

    companion object {
        @Volatile private var instance: DatabaseHelper? = null
        fun getInstance(context: Context): DatabaseHelper =
            instance ?: synchronized(this) {
                instance ?: DatabaseHelper(context.applicationContext).also { instance = it }
            }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                price REAL NOT NULL,
                stock INTEGER NOT NULL DEFAULT 0,
                barcode TEXT UNIQUE,
                min_stock INTEGER NOT NULL DEFAULT 5
            )"""
        )
        db.execSQL(
            """CREATE TABLE movements (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                qty INTEGER NOT NULL,
                type TEXT NOT NULL CHECK (type IN ('IN','OUT')),
                date TEXT NOT NULL
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS movements")
        db.execSQL("DROP TABLE IF EXISTS products")
        onCreate(db)
    }

    // ---------------- PRODUCTOS (CRUD) ----------------

    private fun Product.toValues() = ContentValues().apply {
        put("name", name)
        put("price", price)
        put("stock", stock)
        put("barcode", barcode)
        put("min_stock", minStock)
    }

    private fun Cursor.toProduct() = Product(
        id = getLong(getColumnIndexOrThrow("id")),
        name = getString(getColumnIndexOrThrow("name")),
        price = getDouble(getColumnIndexOrThrow("price")),
        stock = getInt(getColumnIndexOrThrow("stock")),
        barcode = getString(getColumnIndexOrThrow("barcode")),
        minStock = getInt(getColumnIndexOrThrow("min_stock"))
    )

    /** Devuelve false si el código de barras ya existe. */
    fun insertProduct(p: Product): Boolean = try {
        writableDatabase.insertOrThrow("products", null, p.toValues()) > 0
    } catch (e: SQLiteConstraintException) { false }

    fun updateProduct(p: Product): Boolean = try {
        writableDatabase.update("products", p.toValues(), "id = ?", arrayOf(p.id.toString())) > 0
    } catch (e: SQLiteConstraintException) { false }

    fun deleteProduct(id: Long) {
        writableDatabase.delete("products", "id = ?", arrayOf(id.toString()))
    }

    fun getProduct(id: Long): Product? =
        readableDatabase.query("products", null, "id = ?", arrayOf(id.toString()), null, null, null)
            .use { if (it.moveToFirst()) it.toProduct() else null }

    fun getProducts(search: String = ""): List<Product> {
        val like = "%${search.trim()}%"
        val list = mutableListOf<Product>()
        readableDatabase.query(
            "products", null, "name LIKE ? OR barcode LIKE ?", arrayOf(like, like),
            null, null, "name COLLATE NOCASE"
        ).use { while (it.moveToNext()) list.add(it.toProduct()) }
        return list
    }

    // ---------------- MOVIMIENTOS ----------------

    /**
     * Registra un movimiento y actualiza el stock en una sola transacción.
     * Devuelve null si todo salió bien, o un mensaje de error.
     */
    fun registerMovement(productId: Long, qty: Int, type: String): String? {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val product = getProduct(productId) ?: return "Producto no encontrado"
            val newStock = if (type == "IN") product.stock + qty else product.stock - qty
            if (newStock < 0) return "Stock insuficiente (disponible: ${product.stock})"

            val mv = ContentValues().apply {
                put("product_id", productId)
                put("qty", qty)
                put("type", type)
                put("date", SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()))
            }
            db.insertOrThrow("movements", null, mv)
            db.update(
                "products", ContentValues().apply { put("stock", newStock) },
                "id = ?", arrayOf(productId.toString())
            )
            db.setTransactionSuccessful()
            return null
        } finally {
            db.endTransaction()
        }
    }

    fun getMovements(): List<Movement> {
        val list = mutableListOf<Movement>()
        readableDatabase.rawQuery(
            """SELECT m.id, m.product_id, p.name, m.qty, m.type, m.date
               FROM movements m JOIN products p ON p.id = m.product_id
               ORDER BY m.id DESC LIMIT 100""", null
        ).use {
            while (it.moveToNext()) list.add(
                Movement(it.getLong(0), it.getLong(1), it.getString(2), it.getInt(3), it.getString(4), it.getString(5))
            )
        }
        return list
    }

    // ---------------- RESUMEN ----------------

    fun getTotalValue(): Double =
        readableDatabase.rawQuery("SELECT COALESCE(SUM(price * stock), 0) FROM products", null)
            .use { it.moveToFirst(); it.getDouble(0) }

    fun getTopProducts(limit: Int = 5): List<Product> {
        val list = mutableListOf<Product>()
        readableDatabase.query(
            "products", null, null, null, null, null, "price * stock DESC", limit.toString()
        ).use { while (it.moveToNext()) list.add(it.toProduct()) }
        return list
    }

    fun getLowStockProducts(): List<Product> {
        val list = mutableListOf<Product>()
        readableDatabase.query(
            "products", null, "stock <= min_stock", null, null, null, "stock ASC"
        ).use { while (it.moveToNext()) list.add(it.toProduct()) }
        return list
    }
}
