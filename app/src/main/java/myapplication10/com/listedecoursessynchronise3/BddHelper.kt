package myapplication10.com.listedecoursessynchronise3

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.jetbrains.anko.db.ManagedSQLiteOpenHelper

/**
 * Ce code est tiré du livre : Kotlin , les fondamentaux du développement d'applications Android
 */
class BddHelper (context: Context) : ManagedSQLiteOpenHelper(context, DATABASE_NAME_, null, DATABASE_VERSION_) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(Produits._Produits_.SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Mettre à jour via des ALTER
    }

    companion object {
        const val DATABASE_VERSION_ = 1
        const val DATABASE_NAME_ = "BddExemple.db"
    }
}