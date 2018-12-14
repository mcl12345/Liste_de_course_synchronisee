package myapplication10.com.listedecoursessynchronise3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v4.app.JobIntentService
import org.jetbrains.anko.toast


class ConnectionReceiver : BroadcastReceiver() {
    var maListeAMettreAJour = arrayListOf<Produit_DATA_CLASS?>()

    override fun onReceive(context: Context, intent: Intent) {
        // Si ce n'est pas un évenement de connection...
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if(connMgr.activeNetworkInfo?.type == null) {
            return; // on quitte la fonction
        }

        // sinon c'est un évenement de connection
        for (i in 1..1000) {
            if (ProduitDAO(context).get(i) != null) {
                if(ProduitDAO(context).get(i)!!.image == null) {
                    maListeAMettreAJour.add(ProduitDAO(context).get(i))
                }
            }
        }

        for (row in maListeAMettreAJour) {
            val id = row!!.id
            val intSrv = Intent()
            intSrv.putExtra(MonService.EXTRA_MESSAGE_DOWNLOAD_ID, id)
            // On lance le service pour les télécharger
            context.toast("ConnectionReceiver : " + id)
            JobIntentService.enqueueWork(context, MonService::class.java, 0, intSrv)
        }
    }
}