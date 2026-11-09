
package com.example.netcheck

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var txtInternetStatus: TextView
    private lateinit var txtNetworkType: TextView
    private lateinit var txtPingResult: TextView
    private lateinit var btnPing: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtInternetStatus = findViewById(R.id.txtInternetStatus)
        txtNetworkType = findViewById(R.id.txtNetworkType)
        txtPingResult = findViewById(R.id.txtPingResult)
        btnPing = findViewById(R.id.btnPing)

        checkNetworkConnection()

        btnPing.setOnClickListener {
            runLatencyTest()
        }
    }

    override fun onResume() {
        super.onResume()

        if (::txtInternetStatus.isInitialized) {
            checkNetworkConnection()
        }
    }

    private fun checkNetworkConnection() {

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager

        val network = connectivityManager.activeNetwork

        val capabilities =
            connectivityManager.getNetworkCapabilities(network)

        if (capabilities == null) {
            txtInternetStatus.text = "Disconnected"
            txtNetworkType.text = "Network Type: No Connection"
            return
        }

        val hasInternet =
            capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            ) &&
                    capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    )

        txtInternetStatus.text =
            if (hasInternet) "Connected" else "No Internet"

        val networkType = when {

            capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            ) -> "Wi-Fi"

            capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ) -> "Mobile Data"

            capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_ETHERNET
            ) -> "Ethernet"

            else -> "Other Network"
        }

        txtNetworkType.text = "Network Type: $networkType"
    }

    private fun runLatencyTest() {

        btnPing.isEnabled = false
        txtPingResult.text = "Testing connection..."

        Thread {

            var connection: HttpURLConnection? = null

            try {
                val url = URL("https://www.google.com/generate_204")

                connection =
                    url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.useCaches = false

                val startTime = SystemClock.elapsedRealtime()

                val responseCode = connection.responseCode

                val endTime = SystemClock.elapsedRealtime()

                val responseTime = endTime - startTime

                runOnUiThread {

                    if (responseCode == 204) {
                        txtPingResult.text =
                            "Response Time: $responseTime ms"
                    } else {
                        txtPingResult.text =
                            "Test Failed: HTTP $responseCode"
                    }

                    btnPing.isEnabled = true
                }

            } catch (e: Exception) {

                runOnUiThread {
                    txtPingResult.text =
                        "Connection Failed: Check Internet"

                    btnPing.isEnabled = true
                }

            } finally {
                connection?.disconnect()
            }

        }.start()
    }
}
