package myapplication10.com.listedecoursessynchronise3


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

    class ProduitRecyclerAdapter(val list : List<Produit_DATA_CLASS?>, val listener: (Produit_DATA_CLASS?) -> Unit) : RecyclerView.Adapter<ProduitRecyclerAdapter.ProduitViewHolder>() {

    inner class ProduitViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        var contenu : TextView = view.findViewById(R.id.tv_contenu_list)
        var quantite : TextView = view.findViewById(R.id.tv_quantite_list)
        var categorie : TextView = view.findViewById(R.id.tv_categorie_list)
        var image_profile : ImageView = view.findViewById(R.id.image_croix_rouge_liste)
    }

    override fun onBindViewHolder(viewHolder : ProduitViewHolder, position : Int) {
        viewHolder.contenu.text                 = list[position]?.contenu
        //holder.date.text                  = list[position].date.toLocaleString()
        viewHolder.quantite.text                = list[position]?.quantite
        viewHolder.categorie.text                = list[position]?.categorie
        viewHolder.image_profile.setImageBitmap(list[position]?.image)
        viewHolder.itemView.setOnClickListener{listener(list[position])}
    }

    override fun onCreateViewHolder(parent:  ViewGroup, viewType: Int) = ProduitViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_produit, parent, false))

    override fun getItemCount() = list.size

}