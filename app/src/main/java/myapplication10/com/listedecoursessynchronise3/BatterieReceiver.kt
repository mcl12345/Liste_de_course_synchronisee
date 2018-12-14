package myapplication10.com.listedecoursessynchronise3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast

class BatterieReceiver : BroadcastReceiver() {
    var is_low :Boolean = false

    override fun onReceive(context: Context, intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        is_low = false
        if(level < 10 ) { is_low = true }
        context.toast("Level: $level, Voltage: $voltage")
    }
}