package myapplication10.com.listedecoursessynchronise3

import android.provider.BaseColumns

/**
 * Ce code est tiré du livre : Kotlin , les fondamentaux du développement d'applications Android
 */
object Produits {
    object _Produits_ : BaseColumns {
        const val TABLE_NAME = "produits"
        const val COLUMN_NAME_ID = "id"
        const val COLUMN_NAME_CONTENU = "contenu"
        const val COLUMN_NAME_QUANTITE = "quantite"
        const val COLUMN_NAME_CATEGORIE = "categorie"
        const val COLUMN_NAME_DATE = "date"
        const val COLUMN_NAME_ID_IMAGE = "id_image"

        const val SQL_CREATE_TABLE = "CREATE TABLE ${TABLE_NAME} ("+
                "${COLUMN_NAME_ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${COLUMN_NAME_CONTENU} TEXT," +
                "${COLUMN_NAME_QUANTITE} TEXT," +
                "${COLUMN_NAME_CATEGORIE} TEXT," +
                "${COLUMN_NAME_DATE} INTEGER," +
                "${COLUMN_NAME_ID_IMAGE} INTEGER)"

        const val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS ${_Produits_.TABLE_NAME}"
    }
}