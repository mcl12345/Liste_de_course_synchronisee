package myapplication10.com.listedecoursessynchronise3

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import kotlinx.android.synthetic.main.activity_nouveau.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.io.*
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class ProgrammationAjoutActivity : AppCompatActivity() {

    companion object {
        const val PREF_DATE_PRODUIT = "PREF_DATE_PRODUIT"
        const val PREF_CATEGORY_PRODUIT = "PREF_CATEGORY_PRODUIT"
        const val PREF_CONTENU_PRODUIT = "PREF_CONTENU_PRODUIT"
        const val PREF_QUANTITY_PRODUIT = "PREF_QUANTITY_PRODUIT"
        const val PREF_DATE_PROGRAMMATION = "PREF_DATE_PROGRAMMATION"
        const val PREF_SIZE_IMAGE = "PREF_SIZE_IMAGE"

        const val PREF_CONFIRMATION_PROGRAMMATION = "PREF_CONFIRMATION_PROGRAMMATION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.programmation_ajout_activity)

        val sp = getSharedPreferences("myapplication10.com.listedecoursessynchronise3", Context.MODE_PRIVATE)

        // For the onResume of MainActivity
        with(sp.edit()){
            putBoolean(PREF_CONFIRMATION_PROGRAMMATION, true)
            apply()
        }

        val ed_ajouter_contenu: EditText = findViewById(R.id.ajouter_contenu_et) as EditText
        val ed_ajouter_quantite: EditText = findViewById(R.id.ajouter_quantite_et) as EditText

        ajouter_btn.setOnClickListener {
            // On enregistre un nouveau produit

            var radio_button_charcuterie: RadioButton = findViewById(R.id.radioButton_charcuterie)
            var radio_button_boisson: RadioButton = findViewById(R.id.radioButton_boisson)
            var radio_button_cereales: RadioButton = findViewById(R.id.radioButton_cereales)
            var categorie = ""
            if (buttonclickedCategory(radio_button_charcuterie) == "") { } else {
               categorie = buttonclickedCategory(radio_button_charcuterie)
            }
            if (buttonclickedCategory(radio_button_boisson) == "") { } else {
                categorie = buttonclickedCategory(radio_button_boisson)
            }
             if (buttonclickedCategory(radio_button_cereales) == "") { } else {
                  categorie = buttonclickedCategory(radio_button_cereales)
            }

            var radio_button_une_minute : RadioButton = findViewById(R.id.radioButton_une_minute)
            var radio_button_cinq_minutes : RadioButton = findViewById(R.id.radioButton_cinq_minutes)
            var radio_button_quinze_minutes : RadioButton = findViewById(R.id.radioButton_quinze_minutes)
            var date_programmation = ""
            if(buttonclickedDate(radio_button_une_minute) == "") { } else {
                date_programmation = buttonclickedDate(radio_button_une_minute)
            }
            if(buttonclickedDate(radio_button_cinq_minutes) == "") { } else {
                date_programmation = buttonclickedDate(radio_button_cinq_minutes)
            }
            if(buttonclickedDate(radio_button_quinze_minutes) == "") { } else {
                date_programmation = buttonclickedDate(radio_button_quinze_minutes)
            }

            //val yourmilliseconds = System.currentTimeMillis()
            //val sdf = SimpleDateFormat("MMM dd,yyyy HH:mm")
            //val resultdate = Date(yourmilliseconds)
            //var la_date = sdf.format(resultdate)

            with(sp.edit()){
                putString(PREF_CONTENU_PRODUIT, ed_ajouter_contenu.text.toString())
                putString(PREF_QUANTITY_PRODUIT, ed_ajouter_quantite.text.toString())
                putString(PREF_CATEGORY_PRODUIT, categorie)
                putString(PREF_DATE_PRODUIT, System.currentTimeMillis().toString())
                putString(PREF_SIZE_IMAGE, "200x150")

                putString(PREF_DATE_PROGRAMMATION, date_programmation)

                apply()
            }
            finish()
        }
    }

    fun buttonclickedCategory(view: View): String {
        var checked: Boolean = (view as RadioButton).isChecked
        when (view.getId()) {
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

    fun buttonclickedDate(view_ : View) : String {
        var checked_ : Boolean = ( view_ as RadioButton).isChecked
        when(view_.getId()) {
            R.id.radioButton_une_minute ->
                if (checked_) {
                    return "une_minute"
                }
            R.id.radioButton_cinq_minutes ->
                if (checked_) {
                    return "cinq_minutes"
                }
            R.id.radioButton_quinze_minutes ->
                if (checked_) {
                    return "quinze_minutes"
                }
        }
        return ""
    }
}