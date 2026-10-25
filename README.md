# Pulpería – Inventario (Android / Kotlin / SQLite)

1. Abrir la carpeta `Pulperia` en Android Studio (Koala o superior, JDK 17).
2. Esperar el Gradle Sync y ejecutar en un emulador o dispositivo (minSdk 24).

Tablas:
- products(id, name, price, stock, barcode UNIQUE, min_stock)
- movements(id, product_id FK, qty, type IN|OUT, date)
