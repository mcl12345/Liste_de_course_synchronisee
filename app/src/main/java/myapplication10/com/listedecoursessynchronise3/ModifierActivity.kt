package myapplication10.com.listedecoursessynchronise3

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_modifier.*
import org.jetbrains.anko.toast
import android.widget.TextView
import android.content.Context
import android.widget.EditText
import java.io.FileNotFoundException
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.RadioButton
import org.jetbrains.anko.longToast
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException


class ModifierActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ISCONFIRMED = "ModifierActivity.EXTRA_ISCONFIRMED"
        const val EXTRA_UPDATE = "ModifierActivity.EXTRA_UPDATE"
        const val VAL_CONFIRMED = 1
    }

    var nouveau_contenu : String = ""
    var nouvelle_quantite : Int = 0
    var identifiant:Int = 0
    var le_produit = Produit_DATA_CLASS(0, "", "", "", "", null, 0)//loadBitmapFromStorage(0))

    var batterie_receiver = BatterieReceiver()
    var ancien_contenu = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modifier)

        try {
            val bundle=intent.extras

            // Mise à jour de la base de données
            if(bundle!=null) {
                if (bundle.getString("modifier") != null) {
                    identifiant = Integer.parseInt(bundle.getString("modifier").toString())
                    val contenu = findViewById(R.id.produit_modifier_et) as EditText
                    val quantite = findViewById(R.id.quantite_modifier_et) as EditText

                    // Check le bon radioButton
                    var radio_button_charcuterie : RadioButton = findViewById(R.id.radioButton_charcuterie)
                    var radio_button_boisson : RadioButton = findViewById(R.id.radioButton_boisson)
                    var radio_button_cereales : RadioButton = findViewById(R.id.radioButton_cereales)
                    if("charcuterie" == ProduitDAO(this).get(identifiant)!!.categorie) {
                        radio_button_charcuterie.setChecked(true)
                    }
                    if("boisson" == ProduitDAO(this).get(identifiant)!!.categorie) {
                        radio_button_boisson.setChecked(true)
                    }
                    if("cereales" == ProduitDAO(this).get(identifiant)!!.categorie) {
                        radio_button_cereales.setChecked(true)
                    }

                    // Pour mettre à jour la bdd en remote :
                    ancien_contenu = ProduitDAO(this).get(identifiant)!!.contenu;

                    contenu.setText(ProduitDAO(this).get(identifiant)!!.contenu, TextView.BufferType.EDITABLE)
                    quantite.setText(ProduitDAO(this).get(identifiant)!!.quantite, TextView.BufferType.EDITABLE)

                    le_produit = ProduitDAO(this).get(identifiant)!!
                }
            }
        } catch (e : kotlin.KotlinNullPointerException ) { }

        confirmer_btn.setOnClickListener {
            nouveau_contenu = produit_modifier_et.text.toString()
            nouvelle_quantite = Integer.parseInt(quantite_modifier_et.text.toString())

            var radio_button_charcuterie : RadioButton = findViewById(R.id.radioButton_charcuterie)
            var radio_button_boisson : RadioButton = findViewById(R.id.radioButton_boisson)
            var radio_button_cereales : RadioButton = findViewById(R.id.radioButton_cereales)
            var nouvelle_categorie = ""
            if(buttonclicked(radio_button_charcuterie) == "") {
                // Il ne se passe rien
            } else {
                nouvelle_categorie = buttonclicked(radio_button_charcuterie)
            }
            if(buttonclicked(radio_button_boisson) == "") {
                // Il ne se passe rien
            } else {
                nouvelle_categorie = buttonclicked(radio_button_boisson)
            }
            if(buttonclicked(radio_button_cereales) == "") {
                // Il ne se passe rien
            } else {
                nouvelle_categorie = buttonclicked(radio_button_cereales)
            }

            val yourmilliseconds = System.currentTimeMillis()
            le_produit = Produit_DATA_CLASS(identifiant, nouveau_contenu , nouvelle_quantite.toString(), nouvelle_categorie, yourmilliseconds.toString(), null, ProduitDAO(this).get(identifiant)!!.id_image)  //loadBitmapFromStorage(identifiant))
            ProduitDAO(this).update(le_produit)

            // ---------------------------------------------------------
            // Insert data into remote database : http://213.32.90.43/
            if(batterie_receiver.is_low) {
                longToast("LOW BATTERY")
            } else {
                // TP5 Slide 18 & 25
                // Récupère l'identifiant BDD -> identifiant qui est synchronisé pour chaque mise à jour
                val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                var url_ : String = "http://213.32.90.43/android/update.php?id_sqlite=" + identifiant + "&ancien_contenu="+ancien_contenu+"&contenu="+nouveau_contenu + "&quantite=" + nouvelle_quantite.toString() + "&categorie=" + nouvelle_categorie + "&la_date="+System.currentTimeMillis().toString()
                Log.e("AFFICHER", url_)
                when (connMgr.activeNetworkInfo?.type) {
                    ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE -> DownloadTask().execute(
                        URL(url_)
                    )
                    null -> { toast("Pas de réseau ") }
                }
            }
            // ---------------------------------------------------------

            returnResult(VAL_CONFIRMED)
        }
    }

    // Code venant de ce site :
    // FROM http://tutorielandroid.francoiscolin.fr/recupjson.php
    inner class DownloadTask : AsyncTask<URL, Void, String>() {
        override fun doInBackground(vararg params: URL): String  {
            try {
                val conn = params[0].openConnection() as HttpURLConnection
                conn.connect()
                if(conn.responseCode != HttpURLConnection.HTTP_OK) {
                    return "erreur HttpURLConnection nok"
                } else {

                    val inputStream = conn.getInputStream()

                    /*
                    * InputStreamOperations est une classe complémentaire:
                    * Elle contient une méthode InputStreamToString.
                    */
                    val result = InputStreamOperations.InputStreamToString(inputStream)

                    // On récupère le JSON complet
                    val jsonObject = JSONObject(result)
                    // On récupère l'élément qui nous concernent
                    return  jsonObject.getString("produit")
                }
            } catch (e : FileNotFoundException) {
                return "FileNotFoundException"
            } catch (e : UnknownHostException) {
                return "UnknownHostException"
            } catch (e : ConnectException) {
                return "ConnectException"
            } catch (e : IOException) {
                return "IOException"
            }
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            Log.e("AFFICHER onPostExecute", result)
        }
    }

    fun buttonclicked(view : View) : String {
        var checked : Boolean = ( view as RadioButton).isChecked
        when(view.getId()) {
            R.id.radioButton_charcuterie ->
                if (checked) {
                    return "charcuterie"
                }
            R.id.radioButton_boisson ->
                if (checked) {
                    return "boisson"
                }
            R.id.radioButton_cereales ->
                if (checked) {
                    return "cereales"
                }
        }
        return ""
    }

    private fun returnResult(res: Int) {
        val retIntent = Intent()
        val extras = Bundle()
        extras.putInt(EXTRA_ISCONFIRMED, res)
        extras.putString(EXTRA_UPDATE, nouveau_contenu)
        retIntent.putExtras(extras)
        setResult(Activity.RESULT_OK, retIntent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        nouveau_contenu = produit_modifier_et.text.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        nouveau_contenu = produit_modifier_et.text.toString()
    }
}