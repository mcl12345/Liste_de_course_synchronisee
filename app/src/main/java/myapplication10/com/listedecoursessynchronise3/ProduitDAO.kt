package myapplication10.com.listedecoursessynchronise3

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

/**
 * Ce code est tiré du livre : Kotlin , les fondamentaux du développement d'applications Android
 */
class ProduitDAO(context: Context) : AppCompatActivity() {
    // Récupération d'un objet BddHelper permettant de récupérer des connexions à la base de données
    private val dbHelper = BddHelper(context)

    /**
     * Fonction permettant d'enregistrer un produit en base de données
     * Une requête INSERT sera envoyée à la bdd
     */
    fun insert(produit: Produit_DATA_CLASS) : Long? {
        // Récupération d'un objet représentant une connexion à la base de données
        // Si la bdd n'existe pas alors elle est créée
        var return_resultat: Long? = 0

        // val db = dbHelper.writableDatabase
        dbHelper.use {

            // Objet permettant d'enregistrer des données sous forme de clé / valeur
            // La clé est le nom d'une colonne
            // La valeur est la valeur à insérer dans la colonne de l'enregistrement
            val values = ContentValues().apply {
                put(Produits._Produits_.COLUMN_NAME_CONTENU, produit.contenu)
                put(Produits._Produits_.COLUMN_NAME_QUANTITE, produit.quantite)
                put(Produits._Produits_.COLUMN_NAME_CATEGORIE, produit.categorie)
                put(Produits._Produits_.COLUMN_NAME_DATE, produit.date)
                put(Produits._Produits_.COLUMN_NAME_ID_IMAGE, produit.id_image)
            }

            return_resultat = insert(Produits._Produits_.TABLE_NAME, null, values)
        }
        return return_resultat
    }

    /**
     * Fonction permettant de récupérer un objet de type Produit_DATA_CLASS grâce à son identifiant
     * Une requête SELECT sera envoyée à la bdd
     */
    fun get(id : Int) : Produit_DATA_CLASS? {
        // Objet de type Produit_DATA_CLASS permettant d'enregistrer les informations à retourner
        var resultat:Produit_DATA_CLASS? = null

        // Récupération d'un objet représentant une connexion en lecture sur la base de données
        // Si la base de données n'existe pas alors elle est créée.
        //val it = dbHelper.readableDatabase
        dbHelper.use {
            // Définition de la projection de la requête SELECT
            val projection = arrayOf(Produits._Produits_.COLUMN_NAME_ID, Produits._Produits_.COLUMN_NAME_CONTENU, Produits._Produits_.COLUMN_NAME_QUANTITE, Produits._Produits_.COLUMN_NAME_CATEGORIE, Produits._Produits_.COLUMN_NAME_DATE, Produits._Produits_.COLUMN_NAME_ID_IMAGE)

            // Définition du WHERE de la requête SELECT
            // Le WHERE contient un paramètre représenté par le "?"
            val selection = "${Produits._Produits_.COLUMN_NAME_ID} = ?"

            // Valeur qui sera fournie au paramètre de la restriction WHERE
            val selectionArgs = arrayOf("$id")

            // Appel de la fonction query qui éxécute le SELECT et retourne le résultat de la requête sous forme de cursor
            try {
                val cursor = query(
                    Produits._Produits_.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
                )

                // Travail sur le curseur
                with(cursor) {
                    // fonction moveToNext de l'objet cursor
                    if (moveToNext()) {
                        // Récupération de l'identifiant dans le résultat de la requête
                        val itemId = getInt(getColumnIndexOrThrow(Produits._Produits_.COLUMN_NAME_ID))

                        // Récupération du nom dans le résultat de la requête
                        val itemContenu = getString(getColumnIndexOrThrow(Produits._Produits_.COLUMN_NAME_CONTENU))

                        val itemQuantite = getString(getColumnIndexOrThrow(Produits._Produits_.COLUMN_NAME_QUANTITE))

                        val itemCategorie = getString(getColumnIndexOrThrow(Produits._Produits_.COLUMN_NAME_CATEGORIE))

                        val itemDate = getString(getColumnIndexOrThrow(Produits._Produits_.COLUMN_NAME_DATE))

                        val itemIdImage = getInt(getColumnIndexOrThrow(Produits._Produits_.COLUMN_NAME_ID_IMAGE))

                        // Construction de l'objet de type Produit_DATA_CLASS qui sera la valeur retournée
                        resultat = Produit_DATA_CLASS(itemId, itemContenu, itemQuantite, itemCategorie, itemDate, loadBitmapFromStorage(itemIdImage), itemIdImage)

                    }
                }
            } catch (e : android.database.sqlite.SQLiteCantOpenDatabaseException) {
                e.printStackTrace()
            }
        }
        // Retourne la data_class
        return resultat
    }

    /**
     * Fonction permettant de mettre à jour un enregistrement dans la table Produits2
     * Une requête update sera envoyée à la bdd
     */
    fun update(item:Produit_DATA_CLASS) {
        // Récupération d'un objet représentant une connexion en écriture sur la bdd
        // Si la base de données n'existe pas alors elle est créée.
        //val db = dbHelper.writableDatabase
        dbHelper.use {

            // Objet permettant d'enregistrer des données sous forme de clé/valeur
            // La clé est le nom d'une colonne

            val values = ContentValues().apply {
                put(Produits._Produits_.COLUMN_NAME_ID, item.id)
                put(Produits._Produits_.COLUMN_NAME_CONTENU, item.contenu)
                put(Produits._Produits_.COLUMN_NAME_QUANTITE, item.quantite)
                put(Produits._Produits_.COLUMN_NAME_CATEGORIE, item.categorie)
                put(Produits._Produits_.COLUMN_NAME_DATE, item.date)
                put(Produits._Produits_.COLUMN_NAME_ID_IMAGE, item.id_image)
            }

            // Définition du WHERE de la requête UPDATE
            // Le Where contient un paramètre representé par le "?"
            val selection = "${Produits._Produits_.COLUMN_NAME_ID} = ?"

            val selectionArgs = arrayOf("${item.id}")

            // Appel de la fonction query qui exécute la requête UPDATE et retourne le nombre de lignes mises à jour
            val count = update(
                Produits._Produits_.TABLE_NAME, // Nom de la table à mettre à jour
                values, // Valeurs à mettre à jour
                selection, // Définition du WHERE
                selectionArgs) // Valeurs à mettre dans le WHERE
        }
    }

    /**
     * Fonction permettant de supprimer un enregistrement dans la table Produits2
     * Une requête DELETE sera envoyée à la bdd
     */
    fun delete(id: Int) {
        // Récupération d'un objet représentant une connexion en écriture sur la base de données
        // Si la base de données n'existe pas alors elle est créée
        //val db = dbHelper.writableDatabase
        dbHelper.use {

            // Définition du WHERE de la requête DELETE
            // Le WHERE contient un paramètre représenté par le "?"
            val selection = "${Produits._Produits_.COLUMN_NAME_ID} = ?"

            // Valeur qui sera fournie au paramètre de la restriction WHERE
            val selectionArgs = arrayOf("$id")

            // Appel de la fonction query qui exécute la requête WHERE
            delete(Produits._Produits_.TABLE_NAME, selection, selectionArgs)
        }
    }

    fun loadBitmapFromStorage(identifiant : Int) : Bitmap? {
        try {
            val file_ = File("/data/data/myapplication10.com.listedecoursessynchronise3/app_les_images", "profile" + identifiant + ".jpg")
            val bitmap_ = BitmapFactory.decodeStream(FileInputStream(file_))
            return bitmap_
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }
}