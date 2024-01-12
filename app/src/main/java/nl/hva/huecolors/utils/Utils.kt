package nl.hva.huecolors.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import kotlinx.coroutines.flow.MutableStateFlow

class Utils {

    companion object {
        /**
         * Horizontal gradient brush
         *
         * @param first Start of the gradient color
         * @param second End of the gradient color
         * @return Brush object with a horizontal gradient
         */
        fun gradient(first: Color, second: Color): Brush = Brush.horizontalGradient(
            listOf(
                first, second
            )
        )

        /**
         * Helper function to log errors in viewModels
         *
         * @param tag Tag used in logging
         * @param error Exception with information about the error
         */
        fun handleError(tag: String, error: Exception) {
            Log.e(tag, error.message ?: "An unknown error occurred.")
        }

        /**
         * Helper function to decide whether the string is a digit or a dot
         *
         * @return Boolean
         */
        fun String.isNumeric(): Boolean {
            return this.all { it.isDigit() || it == ".".single() }
        }

        /**
         * Helper function to format the bridge identifier
         *
         * @param id Unformatted id of the bridge
         */
        fun formatIdentifier(id: String) = "(.{2})".toRegex().replace(id, "$1:").removeSuffix(":")

        /**
         * Helper function to check permissions
         *
         * @param context Application context
         * @param permissions List of permissions
         * @return Boolean if permission is granted or not
         */
        fun checkPermissions(
            context: Context, permissions: Array<String>
        ): Boolean {
            return permissions.all {
                ContextCompat.checkSelfPermission(
                    context, it
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

        /**
         * Get palette by bitmap
         *
         * @param bitmap Bitmap used to extract colors
         * @param maxAmount Max amount of generated palette colors
         * @return Palette object
         */
        fun getPalette(bitmap: Bitmap, maxAmount: Int): Palette {
            return bitmap.let { image ->
                Palette.from(image).maximumColorCount(maxAmount).generate()
            }
        }

        @Composable
        fun rememberWifiEnabledState(): State<Boolean> {
            val context = LocalContext.current
            val wifiManager = ContextCompat.getSystemService(context, WifiManager::class.java)

            // Create MutableStateFlow to represent Wi-Fi state
            val isWifiEnabledFlow = remember { MutableStateFlow(getWifiEnabledState(wifiManager)) }

            DisposableEffect(context) {
                // Add observer to listen for Wi-Fi state changes
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        isWifiEnabledFlow.value = getWifiEnabledState(wifiManager)
                    }
                }

                context.registerReceiver(receiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))

                onDispose {
                    // Unregister the receiver when the composable is disposed
                    context.unregisterReceiver(receiver)
                }
            }

            // Convert MutableStateFlow to State for use in Compose
            return isWifiEnabledFlow.collectAsState()
        }

        private fun getWifiEnabledState(wifiManager: WifiManager?): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                wifiManager?.isWifiEnabled == true
            } else {
                wifiManager?.wifiState == WifiManager.WIFI_STATE_ENABLED
            }
        }
    }
}