package myapplication10.com.listedecoursessynchronise3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.makeCall
import org.jetbrains.anko.sendSMS
import org.jetbrains.anko.toast
import android.util.Log

class AppelActivity : AppCompatActivity() {

    val phone_number = "+33686978911"
    var identifiant = 0
    var contenu : String? = ""
    var quantite  : String? = ""
    var categorie  : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.e("onCreate", "onCreate")
        
        if (ContextCompat.checkSelfPermission( this, android.Manifest.permission.CALL_PHONE ) == PackageManager.PERMISSION_GRANTED) {
            // Va chercher un contact
            Log.e("onCreate permission", "ok")
            
            val intentContact = Intent(Intent.ACTION_PICK)
            intentContact.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            if(intentContact.resolveActivity(packageManager) != null) {
                startActivityForResult(intentContact, 43)
            }
        } else {
            Log.e("onCreate permission", "nok")
            
            // On demande la permission
            ActivityCompat.requestPermissions( this, arrayOf(Manifest.permission.CALL_PHONE),1)
        }

        val bundle=intent.extras

        if(bundle!=null) {
            if (bundle.getString("partager") != null) {
                identifiant = Integer.parseInt(bundle.getString("partager").toString())
                contenu = ProduitDAO(this).get(identifiant)?.contenu
                quantite = ProduitDAO(this).get(identifiant)?.quantite
                categorie = ProduitDAO(this).get(identifiant)?.categorie
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            43 -> {
                if (resultCode == RESULT_OK) {
                    if (ContextCompat.checkSelfPermission( this, android.Manifest.permission.CALL_PHONE ) == PackageManager.PERMISSION_GRANTED) {
                        with(contentResolver.query(data?.data, arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),null,null,null)) {
                            moveToFirst()
                            val telephone = getString(0)
                            Log.e("telephone" , telephone)
                        }
                        //makeCall(phone_number)

                        // Choisir l'application Messenger car Hangouts ne permet pas de stocker le message prédéfini.
                        sendSMS(phone_number, contenu + " quantite : " + quantite + " categorie : " + categorie)
                    } else {
                        // On demande la permission
                        ActivityCompat.requestPermissions( this, arrayOf(Manifest.permission.CALL_PHONE),1)
                    }
                }
            }
        }
    }
}