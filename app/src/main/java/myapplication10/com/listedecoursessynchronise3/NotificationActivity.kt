package myapplication10.com.listedecoursessynchronise3

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.RadioButton
import kotlinx.android.synthetic.main.option_activity.*

class NotificationActivity : AppCompatActivity() {

    companion object {
        const val PREF_DATE = "PREF_DATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.option_activity)

        val sp = getSharedPreferences("myapplication10.com.listedecoursessynchronise3", Context.MODE_PRIVATE)

        btn_envoyer_date.setOnClickListener {
            var radio_button_une_minute : RadioButton = findViewById(R.id.radioButton_une_minute)
            var radio_button_cinq_minutes : RadioButton = findViewById(R.id.radioButton_cinq_minutes)
            var radio_button_quinze_minutes : RadioButton = findViewById(R.id.radioButton_quinze_minutes)
            var date = ""
            if(buttonclicked(radio_button_une_minute) == "") {
                // Il ne se passe rien
            } else {
                date = buttonclicked(radio_button_une_minute)
            }
            if(buttonclicked(radio_button_cinq_minutes) == "") {
                // Il ne se passe rien
            } else {
                date = buttonclicked(radio_button_cinq_minutes)
            }
            if(buttonclicked(radio_button_quinze_minutes) == "") {
                // Il ne se passe rien
            } else {
                date = buttonclicked(radio_button_quinze_minutes)
            }

            with(sp.edit()){
                putString(PREF_DATE, date)
                apply()
            }
            finish()
        }
    }

    fun buttonclicked(view : View) : String {
        var checked : Boolean = ( view as RadioButton).isChecked
        when(view.getId()) {
            R.id.radioButton_une_minute ->
                if (checked) {
                    return "une_minute"
                }
            R.id.radioButton_cinq_minutes ->
                if (checked) {
                    return "cinq_minutes"
                }
            R.id.radioButton_quinze_minutes ->
                if (checked) {
                    return "quinze_minutes"
                }
        }
        return ""
    }
}