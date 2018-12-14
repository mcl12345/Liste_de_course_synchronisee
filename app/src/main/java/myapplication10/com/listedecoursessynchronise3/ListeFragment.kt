package myapplication10.com.listedecoursessynchronise3

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.getSystemService
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_liste.*
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ClassCastException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

class ListeFragment : Fragment() {

    interface Listener {
        fun onSelectionListe(quoi : String)
    }

    var myListener : Listener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            myListener = context as Listener
        } catch (e : ClassCastException) {
            throw ClassCastException(context.toString()+ " must implement ListeFragment.Listener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_liste, container, false)
    }

    override fun onResume() {
        super.onResume()
        try {
            myListener!!.onSelectionListe("")
        } catch (e: android.database.sqlite.SQLiteCantOpenDatabaseException) {
            toast("android.database.sqlite.SQLiteCantOpenDatabaseException")
        }
    }
}