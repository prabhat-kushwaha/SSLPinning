package com.prabhatkushwaha.sslpinning

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.CertificatePinner
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /**
         * use this website to find cert public key by domain name
         * https://approov.io/tools/static-pinning/
         * you can also find same with openssl
         */
        val certificatePin = CertificatePinner.Builder()
            .add("api.github.com", "sha256/uyPYgclc5Jt69vKu92vci6etcBDY8UNTyrHQZJpVoZY")
            .build()

        /**
         * with valid cert public key api provide valid user data
         * in case of invalid cert it show Certificate pinning failure exception
         */

        val okHttpClient = OkHttpClient.Builder()
            .certificatePinner(certificatePin)
            .build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.github.com/")
            .client(okHttpClient)
            .build().create(MyApi::class.java)

        CoroutineScope(Dispatchers.Main).launch {
            val data = retrofit.getUserData()
            data.enqueue(object : Callback<GithubUser> {
                override fun onResponse(
                    call: Call<GithubUser>,
                    response: retrofit2.Response<GithubUser>
                ) {
                    Log.d(TAG, "onResponse: ${response.body()}")
                }

                override fun onFailure(call: Call<GithubUser>, t: Throwable) {
                    Log.d(TAG, "onFailure: ${t.localizedMessage}")
                }
            })
            Log.d(TAG, "onCreate: $data")

        }
    }


    interface MyApi {
        @GET("/users/prabhat-kushwaha")
        fun getUserData():
                Call<GithubUser>
    }

}