package myapplication10.com.listedecoursessynchronise3

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
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

class CategorieActivity : AppCompatActivity(), ListeFragment.Listener, DetailFragment.Listener {

    var fl : FrameLayout? = null // Pour vérifier si on est en mode tablette ou non

    var detailFragment_to_delete : DetailFragment = DetailFragment()
    var listeFragment_to_delete : ListeFragment = ListeFragment()
    var maListeCategorie = arrayListOf<Produit_DATA_CLASS?>()

    var identifiant:Int = 0         // L' id du produit qui est envoyé aux fragments
    var categorie = ""

    var batterie_receiver = BatterieReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorie)

        val bundle=intent.extras

        if(bundle!=null) {
            if (bundle.getString("selectionner_produit") != null) {
                categorie = bundle.getString("selectionner_produit")
                Log.e("categorie : " , categorie)

                maListeCategorie.clear()

                // Mise à jour de la liste dans le recyclerview
                for (i in 1..1000) {
                    if (ProduitDAO(this).get(i) != null) {
                        if(categorie == ProduitDAO(this).get(i)!!.categorie) {
                            maListeCategorie.add(ProduitDAO(this).get(i))
                        }
                    }
                }
            }
        }
    }

    override fun onSelectionDetail(id: String, fragment_ : DetailFragment) {
        val bundle=intent.extras

        Log.e("onSelectionDetail : id" , id)

        if(bundle!=null) {
            toast("bundle not null")
            if(bundle.getString("produit") != null) {
                toast("bundle produit not null")

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
                    TextView_date.text = ProduitDAO(this).get(identifiant)?.date
                } catch (e: kotlin.KotlinNullPointerException) {

                }
            } else {
                mise_a_jour_detail_panel(id, fragment_)
            }
        } else {
            mise_a_jour_detail_panel(id, fragment_)
        }
    }

    fun mise_a_jour_detail_panel(id: String, fragment_ : DetailFragment) {
        identifiant = Integer.parseInt(id)
        Log.e("onSelectionDetail : " , id)

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
            TextView_date.text = ProduitDAO(this).get(identifiant)?.date
        } catch (e: kotlin.KotlinNullPointerException) {
        }

        // Charge le Bitmap
        Log.e("test categorie", "mise_a_jour_detail_panel : " + ProduitDAO(this).get(identifiant)?.id_image)
        imageView_profile.setImageBitmap(loadBitmapFromStorage(ProduitDAO(this).get(identifiant)!!.id_image))
    }

    override fun onSelectionListe(quoi: String) {
        val rvListe = findViewById(R.id.recyclerview_main) as RecyclerView
        var fl : FrameLayout? = findViewById(R.id.frame_layout_detail)

        rvListe.setLayoutManager(LinearLayoutManager( this, LinearLayoutManager.VERTICAL, false))
        rvListe.setAdapter(ProduitRecyclerAdapter(maListeCategorie) {
            toast("Click sur : ${it?.id.toString()} = ${it?.contenu}")
            if(fl != null) {
                var fragment_detail : DetailFragment = DetailFragment.newInstance(it?.id.toString())
                toast("tablette")
                // On supprime le fragment précédent qui reste dessous le prochain
                getSupportFragmentManager().beginTransaction().remove(detailFragment_to_delete).commit()
                // Je sauvegarde le fragment pour le supprimer lors de l'appel d'un autre fragment
                detailFragment_to_delete = fragment_detail
                supportFragmentManager.beginTransaction().replace(R.id.frame_layout_detail, fragment_detail).addToBackStack(null).commit()

                // Mise à jour de la liste
                getSupportFragmentManager().beginTransaction().remove(listeFragment_to_delete).commit()
                listeFragment_to_delete = ListeFragment()
                supportFragmentManager.beginTransaction().replace(R.id.frame_layout_liste_categorie, listeFragment_to_delete).commit()
            } else {
                toast("pas tablette")
                // Code qui s'exécute quand on touche un élément
                // it = le Produit de la ligne touchée
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("produit", it?.id.toString())
                startActivityForResult(intent, 1)

                toast("Click sur : ${it?.id} = ${it?.contenu}")
            }
        })
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

    fun alertDialogDelete(){
        val builder = AlertDialog.Builder(this@CategorieActivity)
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
                    longToast("LOW BATTERY")
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
                    longToast("LOW BATTERY")
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

    override fun onResume() {
        super.onResume()
        getSupportFragmentManager().beginTransaction().remove(listeFragment_to_delete).commit()
        listeFragment_to_delete = ListeFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout_liste_categorie, listeFragment_to_delete).commit()

        // Permet de savoir si on est avec une tablette ou un téléphone ( partie 1 )
        fl = findViewById(R.id.frame_layout_detail)
    }

    // Code venant de ce site :
    // FROM http://tutorielandroid.francoiscolin.fr/recupjson.php
    inner class DownloadTask : AsyncTask<URL, Void, Boolean>() {
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