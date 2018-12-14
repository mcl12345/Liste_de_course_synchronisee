package myapplication10.com.listedecoursessynchronise3

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import kotlinx.android.synthetic.main.activity_nouveau.*
import org.jetbrains.anko.*
import org.jetbrains.anko.db.insert
import org.json.JSONArray
import org.json.JSONObject
import org.json.simple.parser.JSONParser
import java.io.*
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class NouveauActivity : AppCompatActivity() {

    var incrementation : Int = 0
    var identifiant_bdd = 1
    var batterie_receiver = BatterieReceiver()
    var filePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nouveau)

        // TP5 Slide 37
        // registerReceiver enregistre les 2 : BatterieReceiver et l'intentFilter
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batterie_receiver, intentFilter)
    }

    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission( this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED) {
            prendre_photo.setOnClickListener {
                val intentPhoto = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
                intentPhoto.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                with(contentResolver.query(uri, arrayOf(android.provider.MediaStore.Images.ImageColumns.DATA), null, null, null)) {
                    moveToFirst()
                    filePath = getString(0)
                    close()
                }
                toast(filePath)

                if(intentPhoto.resolveActivity(packageManager) != null) {
                    startActivityForResult(intentPhoto, 42)
                }
            }
        }
        // On demande la permission
        else {
            ActivityCompat.requestPermissions( this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            42 -> { // Photo
                if (resultCode == RESULT_OK) {
                    val bitmap = BitmapFactory.decodeFile(filePath)

                    val ed_ajouter_contenu : EditText = findViewById(R.id.ajouter_contenu_et) as EditText
                    val ed_ajouter_quantite : EditText = findViewById(R.id.ajouter_quantite_et) as EditText

                    ajouter_btn.setOnClickListener {
                        // On enregistre un nouveau produit
                        val bundle=intent.extras

                        if(bundle!=null) {
                            if (bundle.getString("ajouter_produit") != null) {
                                incrementation =  Integer.parseInt(bundle.getString("ajouter_produit").toString())
                                Log.e("incrementation : " , incrementation.toString())

                                var radio_button_charcuterie : RadioButton = findViewById(R.id.radioButton_charcuterie)
                                var radio_button_boisson : RadioButton = findViewById(R.id.radioButton_boisson)
                                var radio_button_cereales : RadioButton = findViewById(R.id.radioButton_cereales)
                                var categorie = ""
                                if(buttonclicked(radio_button_charcuterie) == "") {
                                    // Il ne se passe rien
                                } else {
                                    categorie = buttonclicked(radio_button_charcuterie)
                                }
                                if(buttonclicked(radio_button_boisson) == "") {
                                    // Il ne se passe rien
                                } else {
                                    categorie = buttonclicked(radio_button_boisson)
                                }
                                if(buttonclicked(radio_button_cereales) == "") {
                                    // Il ne se passe rien
                                } else {
                                    categorie = buttonclicked(radio_button_cereales)
                                }
                                var produit = Produit_DATA_CLASS(incrementation, ed_ajouter_contenu.text.toString(), ed_ajouter_quantite.text.toString(), categorie, System.currentTimeMillis().toString(), loadBitmapFromStorage(incrementation), 0)
                                // Insertion dans la bdd et récupération de l'id
                                identifiant_bdd = (ProduitDAO(this).insert(produit))!!.toInt()

                                // Insert data into remote database : http://213.32.90.43/
                                if(batterie_receiver.is_low) {
                                    longToast("LOW BATTERY")
                                } else {
                                        // TP5 Slide 18 & 25
                                        // Récupère l'identifiant BDD -> identifiant_bdd qui est synchronisé pour chaque suppression et AJOUT
                                        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                                        var url_ : String = "http://213.32.90.43/android/insert.php?id_sqlite=" + identifiant_bdd + "&contenu="+ed_ajouter_contenu.text.toString() + "&quantite=" + ed_ajouter_quantite.text.toString() + "&categorie="+categorie + "&la_date="+System.currentTimeMillis() + "&id_image=" + identifiant_bdd
                                        Log.e("AFFICHER", url_)
                                        when (connMgr.activeNetworkInfo?.type) {
                                            ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE -> DownloadTask().execute(URL(url_))
                                            null -> { toast("Pas de réseau ") }
                                        }
                                }

                                // Enregistrement de la photo
                                saveToInternalStorage(bitmap)

                                var returnIntent = Intent()
                                returnIntent.putExtra("result", "1")
                                setResult(RESULT_OK, returnIntent)
                                finish()
                            }
                            else {
                                Log.e("test", "bundle ajouter_produit null")
                            }
                        }
                        else {
                            Log.e("test", "bundle null")
                        }
                    }

                }
            }
        }
    }

    // Code venant de ce site :
    // FROM http://tutorielandroid.francoiscolin.fr/recupjson.php
    inner class DownloadTask : AsyncTask<URL, Void, String >() {
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
            Log.e("AFFICHER", result)
        }
    }

    // Charge dans la DATA_CLASS le nouveau Bitmap
    fun loadBitmapFromStorage(identifiant : Int) : Bitmap {
        try {
            val file_ = File("/data/data/myapplication10.com.listedecoursessynchronise3/app_les_images", "profile" + identifiant + ".jpg")
            val bitmap_ = BitmapFactory.decodeStream(FileInputStream(file_))
            return bitmap_
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return (ResourcesCompat.getDrawable( getResources(), R.drawable.croix_rouge, null) as BitmapDrawable).getBitmap()
    }

    // Code venant de ce lien : https://stackoverflow.com/questions/30480906/save-bitmap-to-app-folder
    fun saveToInternalStorage( bitmapImage : Bitmap) : String {
        var contextWrapper = ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/les_images
        // path to /data/data/yourapp/app_les_images
        var directory : File = contextWrapper.getDir("les_images", Context.MODE_PRIVATE)
        // Create les_images
        var mypath = File(directory,"profile"+ identifiant_bdd + ".jpg")

        var  fos : FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage?.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch ( e : Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch ( e : IOException) {
                e.printStackTrace()
            }
        }
        return directory.getAbsolutePath()
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

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(batterie_receiver)
    }
}