package myapplication10.com.listedecoursessynchronise3

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.lang.ClassCastException

class DetailFragment : Fragment() {

    var myListener : Listener? = null
    var index = "1"

    companion object {

        /**
         * Source code from Stackoverflow
         * Create a new instance of DetailsFragment, initialized to show the text at 'index'.
         */
        fun newInstance(index: String): DetailFragment {
            val fragment = DetailFragment()
            // Supply index input as an argument.
            val args = Bundle()
            args.putString("index", index)
            fragment.arguments = args
            return fragment
        }
    }

    interface Listener {
        fun onSelectionDetail(quoi : String, fragment : DetailFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            myListener = context as Listener
        } catch (e : ClassCastException) {
            throw ClassCastException(context.toString()+ " must implement DetailFragment.Listener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_detail_linear_layout, container, false)

    }

    override fun onResume() {
        super.onResume()

        try {
            index = arguments!!.getString("index", "1")
        } catch (e : kotlin.KotlinNullPointerException) {
            index = "1"
            toast("erreur DetailFragment onResume")
        }

        myListener!!.onSelectionDetail(index, this)
    }
}