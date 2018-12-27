package myapplication10.com.listedecoursessynchronise3


import android.app.*
import android.content.Context

import android.content.Intent
import android.content.IntentFilter

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import org.jetbrains.anko.toast
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_detail_linear_layout.*
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import kotlinx.android.synthetic.main.activity_main_with_toolbar.*
import myapplication10.com.listedecoursessynchronise3.DetailFragment.Companion.newInstance
import myapplication10.com.listedecoursessynchronise3.NotificationActivity.Companion.PREF_DATE
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

// NavigationView.OnNavigationItemSelectedListener : slide-bar
class MainActivity : AppCompatActivity(), ListeFragment.Listener, DetailFragment.Listener, NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val PREF_LIST_SIZE = "PREF_LIST_SIZE"
    }

    var identifiant:Int = 0         // L' id du produit qui est envoyé aux fragments
    var fl : FrameLayout? = null // Pour vérifier si on est en mode tablette ou non
    var maListe = arrayListOf<Produit_DATA_CLASS?>()
    var detailFragment_to_delete : DetailFragment = DetailFragment()
    var listeFragment_to_delete : ListeFragment = ListeFragment()

    var batterie_receiver = BatterieReceiver()

    override fun onSelectionDetail(id: String, fragment_ : DetailFragment) {
        val bundle=intent.extras

        Log.e("onSelectionDetail", "onSelectionDetail : id : " + id)

        if(bundle!=null) {
            Log.e("onSelectionDetail", "bundle not null")
            if(bundle.getString("produit") != null) {
                Log.e("onSelectionDetail", "bundle produit not null")

                identifiant = Integer.parseInt(bundle.getString("produit").toString())

                modifier_btn.setOnClickListener {
                    val intent = Intent(this, ModifierActivity::class.java)
                    intent.putExtra("modifier", identifiant.toString())
                    startActivityForResult(intent,1)
                }

                var TextView_produit = findViewById(R.id.contenu_detail_tv) as TextView
                var TextView_quantite = findViewById(R.id.quantite_detail_tv) as TextView
                var TextView_date = findViewById(R.id.date_detail_tv) as TextView
                // On charge les produits de la bdd
                try {
                    TextView_produit.text = ProduitDAO(this).get(identifiant)?.contenu
                    TextView_quantite.text = ProduitDAO(this).get(identifiant)?.quantite
                    val yourmilliseconds = ProduitDAO(this).get(identifiant)?.date!!.toLong()
                    val sdf = SimpleDateFormat("MMM dd,yyyy HH:mm")
                    val resultdate = Date(yourmilliseconds)
                    var la_date = sdf.format(resultdate)
                    TextView_date.text = la_date
                } catch (e: kotlin.KotlinNullPointerException) {

                }
            } else {
                mise_a_jour_detail_panel(id)
            }
        } else {
            mise_a_jour_detail_panel(id)
        }
    }

    fun mise_a_jour_detail_panel(id: String) {
        identifiant = Integer.parseInt(id)
        Log.e("maj_detail_panel" ,"onSelectionDetail : " + id)

        val btn_modifier: Button = findViewById(R.id.modifier_btn)
        val imageView_profile: ImageView = findViewById(R.id.image_croix_rouge_detail)

        btn_modifier.setOnClickListener {
            val intent = Intent(this, ModifierActivity::class.java)
            intent.putExtra("modifier", identifiant.toString())
            startActivityForResult(intent, 1)
        }

        var TextView_produit = findViewById(R.id.contenu_detail_tv) as TextView
        var TextView_quantite = findViewById(R.id.quantite_detail_tv) as TextView
        var TextView_categorie = findViewById(R.id.categorie_detail_tv) as TextView
        var TextView_date = findViewById(R.id.date_detail_tv) as TextView
        // On charge les produits de la bdd
        try {
            TextView_produit.text = ProduitDAO(this).get(identifiant)?.contenu
            TextView_quantite.text = ProduitDAO(this).get(identifiant)?.quantite
            TextView_categorie.text = ProduitDAO(this).get(identifiant)?.categorie
            val yourmilliseconds = ProduitDAO(this).get(identifiant)?.date!!.toLong()
            val sdf = SimpleDateFormat("MMM dd,yyyy HH:mm")
            val resultdate = Date(yourmilliseconds)
            var la_date = sdf.format(resultdate)
            TextView_date.text = la_date
        } catch (e: kotlin.KotlinNullPointerException) {
        }

        // Charge le Bitmap
        Log.e("maj_detail_panel" ,"mise_a_jour_detail_panel : " + ProduitDAO(this).get(identifiant)!!.id_image)
        imageView_profile.setImageBitmap(loadBitmapFromStorage(ProduitDAO(this).get(identifiant)!!.id_image))
    }

    override fun onSelectionListe(quoi: String) {
        // On clear la liste afin de la mettre à jour
        maListe.clear()

        // Mise à jour de la liste dans le recyclerview
        for (i in 1..1000) {
            if (ProduitDAO(this).get(i) != null) {
                maListe.add(ProduitDAO(this).get(i))
            }
        }

        val sp = getSharedPreferences("myapplication10.com.listedecoursessynchronise3", Context.MODE_PRIVATE)
        with(sp.edit()){
            putInt(PREF_LIST_SIZE, maListe.size)
            apply()
        }

        var nouveau_btn_ : FloatingActionButton = findViewById(R.id.nouveau_btn)

        nouveau_btn_.setOnClickListener {
            val intent = Intent(this, NouveauActivity::class.java)
            intent.putExtra("ajouter_produit", (maListe.size+1).toString())
            startActivityForResult(intent,2)
        }

        val rvListe = findViewById(R.id.recyclerview_main) as RecyclerView
        var fl : FrameLayout? = findViewById(R.id.frame_layout_detail)

        rvListe.setLayoutManager(LinearLayoutManager( this, LinearLayoutManager.VERTICAL, false))
        rvListe.setAdapter(ProduitRecyclerAdapter(maListe) {
            toast("Click sur : ${it?.id.toString()} = ${it?.contenu}")
            if(fl != null) {
                var fragment_detail : DetailFragment = newInstance(it?.id.toString())
                Log.e("tablette", "tablette")
                // On supprime le fragment précédent qui reste dessous le prochain
                getSupportFragmentManager().beginTransaction().remove(detailFragment_to_delete).commit()
                // Je sauvegarde le fragment pour le supprimer lors de l'appel d'un autre fragment
                detailFragment_to_delete = fragment_detail
                supportFragmentManager.beginTransaction().replace(R.id.frame_layout_detail, fragment_detail).addToBackStack(null).commit()

                // Mise à jour de la liste
                getSupportFragmentManager().beginTransaction().remove(listeFragment_to_delete).commit()
                listeFragment_to_delete = ListeFragment()
                supportFragmentManager.beginTransaction().replace(R.id.frame_layout_liste, listeFragment_to_delete).commit()
            } else {
                Log.e("pas tablette", "pas de tablette")
                // Code qui s'exécute quand on touche un élément
                // it = le Produit de la ligne touchée
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("produit", it?.id.toString())
                startActivityForResult(intent, 1)

                toast("Click sur : ${it?.id} = ${it?.contenu}")
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_with_toolbar)

        // Gère la slide-bar
        setSupportActionBar(toolbar)
        // Gère la slide-bar
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        // Gère la slide-bar
        nav_view.setNavigationItemSelectedListener(this)

        // Get data from the remote database : http://213.32.90.43/android
        if(batterie_receiver.is_low) {
            Log.e("LOW BATTERY", "LOW BATTERY")
        } else {
            // TP5 Slide 18 & 25
            // Récupère l'identifiant BDD -> identifiant_bdd qui est synchronisé pour chaque suppression et AJOUT
            val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            when (connMgr.activeNetworkInfo?.type) {
                ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE -> {
                    // Delete all the list :
                    for (i in 1..1000) {
                        if (ProduitDAO(this).get(i) != null) {
                            Log.e("DELETE MainActivity", i.toString())
                            ProduitDAO(this).delete(i)
                        }
                    }
                    DownloadTask().execute(URL("http://213.32.90.43/android/select_all.php"))
                }
                null -> { toast("Pas de réseau ") }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item : MenuItem) : Boolean {
        when(item.itemId) {
            R.id.nav_charcuterie -> {
                val intent = Intent(this, CategorieActivity::class.java)
                intent.putExtra("selectionner_produit", "charcuterie")
                startActivityForResult(intent,5)
            }
            R.id.nav_boisson -> {
                val intent = Intent(this, CategorieActivity::class.java)
                intent.putExtra("selectionner_produit", "boisson")
                startActivityForResult(intent,6)
            }
            R.id.nav_cereales -> {
                val intent = Intent(this, CategorieActivity::class.java)
                intent.putExtra("selectionner_produit", "cereales")
                startActivityForResult(intent,7)
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onResume() {
        super.onResume()

        getSupportFragmentManager().beginTransaction().remove(listeFragment_to_delete).commit()
        listeFragment_to_delete = ListeFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout_liste, listeFragment_to_delete).commit()

        // Permet de savoir si on est avec une tablette ou un téléphone ( partie 1 )
        fl = findViewById(R.id.frame_layout_detail)

        // Ne fonctionne pas
        //val myFrag = supportFragmentManager.findFragmentById(R.id.ici_fragment) as MyFragment

        // Configuration de l' AlarmReceiver
        // ------------------------------------------------------------
        val mgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val alarmIntent = PendingIntent.getBroadcast(this, 42, intent, 0)

        // Récupération de la date donnée par l'utilisateur dans Programmmer aller au marché
        val sp = getSharedPreferences("myapplication10.com.listedecoursessynchronise3", Context.MODE_PRIVATE)
        val date = sp.getString(PREF_DATE, "null")
        if(date == "une_minute") {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, 0)
            calendar.add(Calendar.HOUR_OF_DAY, 0)
            calendar.add(Calendar.MINUTE, 1)
            mgr.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)
            //mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1 * 60 * 1000, alarmIntent)
            //mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, 1000, alarmIntent)
        } else if (date == "cinq_minutes") {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, 0)
            calendar.add(Calendar.HOUR_OF_DAY, 0)
            calendar.add(Calendar.MINUTE, 5)
            mgr.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)
        } else if (date == "quinze_minutes") {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, 0)
            calendar.add(Calendar.HOUR_OF_DAY, 0)
            calendar.add(Calendar.MINUTE, 15)
            mgr.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent)
        }


        // Configuration de l' AlarmAjoutReceiver
        // ------------------------------------------------------------
        val confirmation = sp.getBoolean(ProgrammationAjoutActivity.PREF_CONFIRMATION_PROGRAMMATION, false)
        if(confirmation == true ) {
            val mgr_ajout = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent_ajout = Intent(this, AlarmAjoutReceiver::class.java)
            val alarmIntent_ajout = PendingIntent.getBroadcast(this, 43, intent_ajout, 0)
            val date_programmation = sp.getString(ProgrammationAjoutActivity.PREF_DATE_PROGRAMMATION, "null")

            if (date_programmation != "null") {
                // TP5 Slide 37
                // registerReceiver enregistre les 2 : BatterieReceiver et l'intentFilter
                val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                registerReceiver(batterie_receiver, intentFilter)
            }
            if (date_programmation == "une_minute") {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, 0)
                calendar.add(Calendar.HOUR_OF_DAY, 0)
                calendar.add(Calendar.MINUTE, 1)
                mgr_ajout.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent_ajout)
            } else if (date_programmation == "cinq_minutes") {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, 0)
                calendar.add(Calendar.HOUR_OF_DAY, 0)
                calendar.add(Calendar.MINUTE, 5)
                mgr_ajout.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent_ajout)
            } else if (date_programmation == "quinze_minutes") {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, 0)
                calendar.add(Calendar.HOUR_OF_DAY, 0)
                calendar.add(Calendar.MINUTE, 15)
                mgr_ajout.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmIntent_ajout)
            }
            // For the onResume of MainActivity
            with(sp.edit()){
                putBoolean(ProgrammationAjoutActivity.PREF_CONFIRMATION_PROGRAMMATION, false)
                apply()
            }
        }

    }

    override fun onCreateOptionsMenu(menu : Menu) : Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.actualisation -> {
                startActivity<ActualisationActivity>()
                return true
            }
            /*R.id.localisation -> {
                startActivity<LocalisationActivity>()
                return true
            }*/
            R.id.appel -> {
                val intent = Intent(this, AppelActivity::class.java)
                intent.putExtra("partager", identifiant.toString())
                startActivityForResult(intent, 3)
                return true
            }
            R.id.programmer_aller_marche -> {
                startActivity<NotificationActivity>()
                return true
            }
            R.id.programmer_ajout_produit -> {
                startActivity<ProgrammationAjoutActivity>()
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
    inner class DownloadTask : AsyncTask<URL, Void, JSONArray >() {
        override fun doInBackground(vararg params: URL): JSONArray?  {
            try {
                val conn = params[0].openConnection() as HttpURLConnection
                conn.connect()
                if(conn.responseCode != HttpURLConnection.HTTP_OK) {
                    return null
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
                    return JSONArray(jsonObject.getString("produits"))
                }
            } catch (e : FileNotFoundException) {
                return null
            } catch (e : UnknownHostException) {
                return null
            } catch (e : ConnectException) {
                return null
            } catch (e : IOException) {
                return null
            } catch (e : org.json.JSONException) {
                return null
            }
        }

        override fun onPostExecute(result: JSONArray?) {
            super.onPostExecute(result)
            // Pour tous les objets on récupère les infos
            if (result == null ) {
                Log.e("AFFICHER", "null")
            } else {
                Log.e("AFFICHER", result.length().toString())

                for(i in 0..(result.length() - 1)) {
                    // On récupère un objet JSON du tableau
                    var obj = JSONObject(result.getString(i))
                    Log.e("AFFICHER", obj.getString("contenu"))
                    var contenu = obj.getString("contenu")
                    var quantite = obj.getString("quantite")
                    var categorie = obj.getString("categorie")
                    var la_date = obj.getString("la_date")
                    var id_image = obj.getInt("id_image")

                    savetoBDD(contenu, quantite, categorie, la_date, id_image)
                }
            }
        }
    }

    // Contenu venant du DownloadTask inséré en base de données
    fun savetoBDD(contenu: String, quantite: String, categorie: String, la_date: String, id_image: Int) {
        // Introduit tous les éléments du serveur à distance
        var produit = Produit_DATA_CLASS(
            identifiant,
            contenu,
            quantite,
            categorie,
            la_date,
            loadBitmapFromStorage(id_image),
            id_image)
        // Insertion dans la bdd
        identifiant = ProduitDAO(this).insert(produit)!!.toInt() + 1
    }

    fun alertDialogDelete(){
        val builder = AlertDialog.Builder(this@MainActivity)
        //Set the alert dialog title
        builder.setTitle("Confirmation de la suppression")
        //Display a message on altert dialog
        builder.setMessage("Etes-vous sûr de vouloir supprimer cet élément?")
        //Set a positive button and its click listener on alert dialog
        builder.setPositiveButton("Oui"){dialog, which ->
            Toast.makeText(applicationContext,"supprimer cet élément.", Toast.LENGTH_SHORT).show()
            //Delete this item
            if(fl != null) {
                var contenu = ProduitDAO(this).get(identifiant)!!.contenu
                ProduitDAO(this).delete(identifiant)

                // Delete en remote 213.32.90.43
                if(batterie_receiver.is_low) {
                    Log.e("LOW BATTERY", "LOW_BATTERY")
                } else {
                    // TP5 Slide 18 & 25
                    // Récupère l'identifiant BDD -> identifiant_bdd qui est synchronisé pour chaque suppression et AJOUT
                    val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    when (connMgr.activeNetworkInfo?.type) {
                        ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE -> {
                            DownloadTask().execute(URL("http://213.32.90.43/android/delete.php?contenu="+contenu))
                            Log.e("id delete DetailActivi", identifiant.toString())
                        }
                        null -> { toast("Pas de réseau ") }
                    }
                }

                // Je quitte le DetailFragment
                getSupportFragmentManager().beginTransaction().remove(detailFragment_to_delete).commit()
                supportFragmentManager.beginTransaction().replace(R.id.frame_layout_liste, ListeFragment()).commit()
            }
            else {
                var contenu = ProduitDAO(this).get(identifiant)!!.contenu
                ProduitDAO(this).delete(identifiant)

                // Delete en remote 213.32.90.43
                if(batterie_receiver.is_low) {
                    Log.e("LOW BATTERY", "LOW_BATTERY")
                } else {
                    // TP5 Slide 18 & 25
                    // Récupère l'identifiant BDD -> identifiant_bdd qui est synchronisé pour chaque suppression et AJOUT
                    val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    when (connMgr.activeNetworkInfo?.type) {
                        ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE -> {
                            DownloadTask().execute(URL("http://213.32.90.43/android/delete.php?contenu="+contenu))
                            Log.e("id delete DetailActivi", identifiant.toString())
                        }
                        null -> { toast("Pas de réseau ") }
                    }
                }
                finish()
            }
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
}