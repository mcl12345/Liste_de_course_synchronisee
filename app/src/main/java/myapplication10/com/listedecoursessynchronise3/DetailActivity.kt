package myapplication10.com.listedecoursessynchronise3

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_detail_linear_layout.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity(), DetailFragment.Listener {

    var identifiant:Int = 1
    var batterie_receiver = BatterieReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        supportFragmentManager.beginTransaction().replace(R.id.frame_layout_detail, DetailFragment()).commit()
    }

    override fun onSelectionDetail(id_ : String, fragment: DetailFragment) {
        val bundle=intent.extras

        if(bundle!=null) {
           if (bundle.getString("produit") != null) {
               identifiant = Integer.parseInt(bundle.getString("produit").toString())
               Log.e("identifiant reel : " , identifiant.toString())

                modifier_btn.setOnClickListener {
                    val intent = Intent(this, ModifierActivity::class.java)
                    intent.putExtra("modifier", identifiant.toString())
                    startActivityForResult(intent, 1)
                }

                var TextView_contenu = findViewById(R.id.contenu_detail_tv) as TextView
                var TextView_quantite = findViewById(R.id.quantite_detail_tv) as TextView
                var TextView_date = findViewById(R.id.date_detail_tv) as TextView
                var TextView_categorie = findViewById(R.id.categorie_detail_tv) as TextView
                var ImageViewProfile = findViewById(R.id.image_croix_rouge_detail) as ImageView

               // On charge les produits de la bdd et du stockage interne
                try {
                    TextView_contenu.text = ProduitDAO(this).get(identifiant)?.contenu
                    TextView_quantite.text = ProduitDAO(this).get(identifiant)?.quantite
                    Log.e("TEST", ProduitDAO(this).get(identifiant)?.date)
                    val yourmilliseconds = ProduitDAO(this).get(identifiant)?.date!!.toLong()
                    val sdf = SimpleDateFormat("MMM dd,yyyy HH:mm")
                    val resultdate = Date(yourmilliseconds)
                    var la_date = sdf.format(resultdate)
                    Log.e("TEST1", "TEST1")
                    TextView_date.text = la_date
                    TextView_categorie.text = ProduitDAO(this).get(identifiant)?.categorie
                    ImageViewProfile.setImageBitmap(loadBitmapFromStorage(ProduitDAO(this).get(identifiant)!!.id_image))
                } catch (e: kotlin.KotlinNullPointerException) {
                    Log.e("catch" , "par ici l erreur")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu : Menu) : Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {

            R.id.appel -> {
                val intent = Intent(this, AppelActivity::class.java)
                intent.putExtra("partager", identifiant.toString())
                startActivityForResult(intent, 3)
                return true
            }
            R.id.action_settings -> {
                startActivity<NotificationActivity>()
                return true
            }
            R.id.action_supprimer -> {
                alertDialogDelete()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    // Code venant de ce site :
    // FROM http://tutorielandroid.francoiscolin.fr/recupjson.php
    inner class DownloadTask : AsyncTask<URL, Void, Boolean >() {
        override fun doInBackground(vararg params: URL): Boolean?  {
            try {
                val conn = params[0].openConnection() as HttpURLConnection
                conn.connect()
                if(conn.responseCode != HttpURLConnection.HTTP_OK) {
                    return null
                } else {
                    return true
                }
            } catch (e : FileNotFoundException) {
                return null
            } catch (e : UnknownHostException) {
                return null
            } catch (e : ConnectException) {
                return null
            } catch (e : IOException) {
                return null
            }
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            Log.e("AFFICHER", result.toString())
        }
    }

    fun alertDialogDelete(){
        val builder = AlertDialog.Builder(this@DetailActivity)
        //Set the alert dialog title
        builder.setTitle("Confirmation de la suppression")
        //Display a message on altert dialog
        builder.setMessage("Etes-vous sûr de vouloir supprimer cet élément?")
        //Set a positive button and its click listener on alert dialog
        builder.setPositiveButton("Oui"){dialog, which ->
            Toast.makeText(applicationContext,"supprimer cet élément.", Toast.LENGTH_SHORT).show()
            //Delete this item
            var contenu = ProduitDAO(this).get(identifiant)!!.contenu
            ProduitDAO(this).delete(identifiant)

            if(batterie_receiver.is_low) {
                longToast("LOW BATTERY")
            } else {
                // TP5 Slide 18 & 25
                // Récupère l'identifiant BDD -> identifiant_bdd qui est synchronisé pour chaque suppression et AJOUT
                val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                when (connMgr.activeNetworkInfo?.type) {
                    ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE -> {
                        DownloadTask().execute(URL("http://213.32.90.43/android/delete.php?contenu="+contenu))
                        Log.e("alertDialogDeletedelete", contenu)
                    }
                    null -> { toast("Pas de réseau ") }
                }
            }

            finish()
        }
        //Display a negative button on alert dialog
        builder.setNegativeButton("Non"){dialog, which ->
            Toast.makeText(applicationContext, "Vous n'êtes pas d'accord.", Toast.LENGTH_SHORT).show()
        }
        //Display a neutral button on alert dialog
        builder.setNeutralButton("Annuler"){_,_ ->
            Toast.makeText(applicationContext,"Vous annulez ce dialog.", Toast.LENGTH_SHORT).show()
        }
        //Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()
        //Display the alert dialog on app interface
        dialog.show()
    }

    // Charge dans le panel détails le nouveau Bitmap
    fun loadBitmapFromStorage(identifiant : Int) : Bitmap {
        try {
            val file_ = File("/data/data/myapplication10.com.listedecoursessynchronise3/app_les_images", "profile" + identifiant + ".jpg")
            Log.e("loadBitmapFromStorage" ,"profile" + identifiant + ".jpg")
            val bitmap_ = BitmapFactory.decodeStream(FileInputStream(file_))
            return bitmap_
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return (ResourcesCompat.getDrawable( getResources(), R.drawable.croix_rouge, null) as BitmapDrawable).getBitmap()
    }
}