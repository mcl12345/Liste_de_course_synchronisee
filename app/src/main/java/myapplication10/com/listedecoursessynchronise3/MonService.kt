package myapplication10.com.listedecoursessynchronise3

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.app.JobIntentService
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.update
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class MonService : JobIntentService() {
    companion object {
        const val EXTRA_MESSAGE_DOWNLOAD_ID = "MessageService.DownloadId"
        const val size_image = "200x150"
    }

    override fun onHandleWork(intent: Intent) {
       if(intent.hasExtra(EXTRA_MESSAGE_DOWNLOAD_ID)){
            // C'est un intent de téléchargement
            traiterTelechargement(intent.getIntExtra(EXTRA_MESSAGE_DOWNLOAD_ID, 0))
        }
    }


    private fun traiterTelechargement(id: Int) {

        // Code déplacé de ListeFragment
        val url = URL("https://source.unsplash.com/random/" + size_image) // 200x200 : petites images carrées

        val conn = url.openConnection() as HttpURLConnection
        conn.connect()
        if (conn.responseCode != HttpURLConnection.HTTP_OK) {
            // on ne fait rien, on réessayera plus tard
            return
        }else {
            // J'ai donc un InputStream (conn.inputStream) et je veux l'écrire dans un fichier
            // Je cherche sur Google "android write inputstream in file"
            // Le code qui suit provient du premier résultat :
            // https://stackoverflow.com/questions/10854211/android-store-inputstream-in-file
            val mon_bitmap = BitmapFactory.decodeStream(conn.inputStream)
            saveToInternalStorage(mon_bitmap, id)
        }
    }

    fun saveToInternalStorage( bitmapImage : Bitmap, identifiant_bdd : Int) : String {
        var contextWrapper = ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/les_images
        // path to /data/data/yourapp/app_les_images
        var directory : File = contextWrapper.getDir("les_images", Context.MODE_PRIVATE)
        // Create les_images
        var mypath : File = File(directory,"profile"+ identifiant_bdd + ".jpg")

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
}