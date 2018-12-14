package myapplication10.com.listedecoursessynchronise3

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_detail_linear_layout.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class RechercherActivity : AppCompatActivity(), ListeFragment.Listener, DetailFragment.Listener {

    var fl : FrameLayout? = null // Pour vérifier si on est en mode tablette ou non

    var detailFragment_to_delete : DetailFragment = DetailFragment()
    var listeFragment_to_delete : ListeFragment = ListeFragment()
    var maListeSearch = arrayListOf<Produit_DATA_CLASS?>()

    var query :String = ""
    var identifiant:Int = 0         // L' id du produit qui est envoyé aux fragments

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recherche)

        if(intent.action == Intent.ACTION_SEARCH) {
            query = intent.getStringExtra(SearchManager.QUERY)
            toast(query) // ce que l'utilisateur a tapé
            // calculer et afficher les résultats ( RecyclerView ? )
        }
    }

    override fun onSelectionDetail(id: String, fragment_ : DetailFragment) {
        val bundle=intent.extras

        toast("onSelectionDetail : id : " + id)

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
            } else {
                mise_a_jour_detail_panel(id, fragment_)
            }
        } else {
            mise_a_jour_detail_panel(id, fragment_)
        }
    }

    fun mise_a_jour_detail_panel(id: String, fragment_ : DetailFragment) {
        identifiant = Integer.parseInt(id)
        toast("onSelectionDetail : " + id)

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
        toast("mise_a_jour_detail_panel : " + identifiant)
        imageView_profile.setImageBitmap(loadBitmapFromStorage(identifiant))
    }

    override fun onSelectionListe(quoi: String) {
        // On clear la liste afin de la mettre à jour
        maListeSearch.clear()

        // Mise à jour de la liste dans le recyclerview
        for (i in 1..1000) {
            if (ProduitDAO(this).get(i) != null) {
                if(query == ProduitDAO(this).get(i)!!.contenu) {
                    maListeSearch.add(ProduitDAO(this).get(i))
                }
            }
        }

        var nouveau_btn_ : FloatingActionButton = findViewById(R.id.nouveau_btn)

        nouveau_btn_.setOnClickListener {
            val intent = Intent(this, NouveauActivity::class.java)
            intent.putExtra("ajouter_produit", (maListeSearch.size+1).toString())
            startActivityForResult(intent,2)
        }

        val rvListe = findViewById(R.id.recyclerview_main) as RecyclerView
        var fl : FrameLayout? = findViewById(R.id.frame_layout_detail)

        rvListe.setLayoutManager(LinearLayoutManager( this, LinearLayoutManager.VERTICAL, false))
        rvListe.setAdapter(ProduitRecyclerAdapter(maListeSearch) {
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
                supportFragmentManager.beginTransaction().replace(R.id.frame_layout_liste_search, listeFragment_to_delete).commit()
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
            R.id.action_settings -> {
                startActivity<NotificationActivity>()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        getSupportFragmentManager().beginTransaction().remove(listeFragment_to_delete).commit()
        listeFragment_to_delete = ListeFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout_liste_search, listeFragment_to_delete).commit()

        // Permet de savoir si on est avec une tablette ou un téléphone ( partie 1 )
        fl = findViewById(R.id.frame_layout_detail)
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