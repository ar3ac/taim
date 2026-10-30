package io.github.ar3ac.taim

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import io.github.ar3ac.taim.util.BatteryUtils
import io.github.ar3ac.taim.widget.TaimWidgetReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val GoogleSans = FontFamily(
    Font(R.font.googlesans_regular, FontWeight.Normal),
    Font(R.font.googlesans_medium, FontWeight.Medium),
    Font(R.font.googlesans_semibold, FontWeight.SemiBold),
    Font(R.font.googlesans_bold, FontWeight.Bold)
)

class MainActivity : ComponentActivity() {

    private var isBatteryExempt by mutableStateOf(true)

    override fun onResume() {
        super.onResume()
        checkBatteryOptimization()
        val battery = BatteryUtils.getBatteryState(this)
        lifecycleScope.launch(Dispatchers.IO) {
            io.github.ar3ac.taim.receiver.BatteryReceiver.updateAllWidgets(
                this@MainActivity,
                battery.percentage,
                battery.isCharging
            )
        }
    }

    private fun checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            isBatteryExempt = powerManager?.isIgnoringBatteryOptimizations(packageName) ?: true
        } else {
            isBatteryExempt = true
        }
    }

    private fun requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(fallback)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkBatteryOptimization()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212)
                ) {
                    Scaffold(
                        containerColor = Color.Transparent
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Header
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 24.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = GoogleSans,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.widget_description),
                                    fontSize = 14.sp,
                                    fontFamily = GoogleSans,
                                    color = Color.LightGray,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Center: Widget Live Simulation Preview
                            WidgetPreviewCard()

                            // Battery Optimization Banner (if not exempt)
                            if (!isBatteryExempt) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2518))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Per aggiornamenti in tempo reale precisi al minuto in standby, imposta la batteria su 'Senza restrizioni'.",
                                            color = Color(0xFFFFD580),
                                            fontSize = 13.sp,
                                            fontFamily = GoogleSans,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedButton(
                                            onClick = { requestIgnoreBatteryOptimization() },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Consenti Senza Restrizioni", color = Color.White, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }

                            // Bottom: Pin Action & Instructions
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { pinWidget() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.add_widget_button),
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = GoogleSans,
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = stringResource(R.string.widget_instructions),
                                    fontSize = 12.sp,
                                    fontFamily = GoogleSans,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun pinWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val provider = ComponentName(this, TaimWidgetReceiver::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val successCallback = PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(this, TaimWidgetReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                appWidgetManager.requestPinAppWidget(provider, null, successCallback)
                Toast.makeText(this, getString(R.string.widget_pinned_success), Toast.LENGTH_SHORT).show()
                return
            }
        }
        Toast.makeText(this, getString(R.string.pin_not_supported), Toast.LENGTH_LONG).show()
    }

    @Composable
    private fun WidgetPreviewCard() {
        val batteryState = BatteryUtils.getBatteryState(this)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE d MMMM", Locale.getDefault())
        val now = Date()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2022))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Digital 24h Time (76sp, GoogleSans SemiBold 600)
                Text(
                    text = timeFormat.format(now),
                    fontSize = 76.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = GoogleSans,
                    color = Color.White
                )

                // Extended Date (19sp, GoogleSans Regular 400, opacity 0.9)
                Text(
                    text = dateFormat.format(now),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = GoogleSans,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 3.dp)
                )

                // Battery Row (marginTop 14dp, gap 8dp, percentage 24sp first, 24dp vertical icon second)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 14.dp)
                ) {
                    Text(
                        text = "${batteryState.percentage}%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = GoogleSans,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val iconRes = if (batteryState.isCharging) {
                        R.drawable.ic_battery_charging
                    } else {
                        R.drawable.ic_battery_std
                    }
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Battery",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
