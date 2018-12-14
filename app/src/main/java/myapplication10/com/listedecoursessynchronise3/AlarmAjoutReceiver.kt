package myapplication10.com.listedecoursessynchronise3

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

class AlarmAjoutReceiver : BroadcastReceiver() {
    var identifiant_bdd = 0

    companion object {
        const val IDENTIFIANT_BDD = "IDENTIFIANT_BDD"
        const val NOTIFICATION_MON_MESSAGE : String = "NOTIFICATION_MON_MESSAGE"
    }

    var batterie_receiver = BatterieReceiver()

    override fun onReceive(context: Context, intent: Intent) {
        context.toast("Alarm")

        // ------------------------------------------------------------
        // Configuration de la notification
        // ------------------------------------------------------------
        // Code de compatibilité avec les versions supérieures
        if (Build.VERSION.SDK_INT >= 26) {
            val mgr = context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                NOTIFICATION_MON_MESSAGE,
                context.getString(R.string.categorie_notif),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mgr.createNotificationChannel(channel)
        }

        val notificationId = 1
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notifIntent = Intent(context, ActualisationActivity::class.java)
        notifIntent.putExtra("notif_intent", "notif_intent")
        notifIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val notifPendingIntent = PendingIntent.getActivity(context, 42, notifIntent, PendingIntent.FLAG_IMMUTABLE)

        //val stackBuilder = TaskStackBuilder.create(context)
        //stackBuilder.addParentStack(ActualisationActivity::class.java)
        //stackBuilder.addNextIntent(notifIntent)

        //val notifPendingIntent = stackBuilder.getPendingIntent(42, PendingIntent.FLAG_IMMUTABLE)

        val notif = NotificationCompat.Builder(context, NOTIFICATION_MON_MESSAGE)
            .setSmallIcon(R.drawable.bosse)
            .setContentTitle(context.getString(R.string.programmation_ajout_produit_title))
            .setContentText(context.getString(R.string.programmation_ajout_produit_content))
            .setContentIntent(notifPendingIntent)
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_LIGHTS)
            .addAction(R.drawable.ic_search_white_24dp, "Action", notifPendingIntent)
            .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.bosse))

        mgr.notify(notificationId, notif.build())
        // ------------------------------------------------------------

        //-------------------------------------------------------
        // Programmer ajout un nouveau produit
        // ------------------------------------------------------
        val sp = context.getSharedPreferences("myapplication10.com.listedecoursessynchronise3", Context.MODE_PRIVATE)

        val taille_liste = sp.getInt(MainActivity.PREF_LIST_SIZE, 0)
        val date_prog_produit = sp.getString(ProgrammationAjoutActivity.PREF_DATE_PROGRAMMATION, "null")
        val date_produit = sp.getString(ProgrammationAjoutActivity.PREF_DATE_PRODUIT, "null")
        val contenu_produit = sp.getString(ProgrammationAjoutActivity.PREF_CONTENU_PRODUIT, "null")
        val quantite_produit = sp.getString(ProgrammationAjoutActivity.PREF_QUANTITY_PRODUIT, "null")
        val category_produit = sp.getString(ProgrammationAjoutActivity.PREF_CATEGORY_PRODUIT, "null")
        if (date_prog_produit != "null") {
            var produit = Produit_DATA_CLASS(
                taille_liste,
                contenu_produit,
                quantite_produit,
                category_produit,
                date_produit,
                null,
            0)

            // Insertion dans la bdd et récupération de l'id
            identifiant_bdd = (ProduitDAO(context).insert(produit))!!.toInt()
            with(sp.edit()){
                putInt(IDENTIFIANT_BDD, identifiant_bdd)
                apply()
            }

            if(batterie_receiver.is_low) {
                Log.e("LOW BATTERY" , "LOW BATTERY")
            } else {
                var identifiant_image = 0
                // TP5 Slide 18 & 25
                // Récupère l'identifiant BDD -> identifiant_bdd qui est synchronisé pour chaque suppression et AJOUT
                val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                when (connMgr.activeNetworkInfo?.type) {
                    ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE -> {
                        var url_ : String = "http://213.32.90.43/android/insert.php?id_sqlite=" + identifiant_bdd + "&contenu="+contenu_produit + "&quantite=" + quantite_produit + "&categorie="+category_produit + "&la_date="+System.currentTimeMillis() + "&id_image="+identifiant_image
                        DownloadTask().execute(URL(url_))
                    }
                    null -> { Log.e("Pas de réseau ", "pas de réseau") }
                }
            }
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
}