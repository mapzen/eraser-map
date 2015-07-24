package com.mapzen.erasermap.model

import com.google.common.io.Files

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import com.mapzen.erasermap.model.ManifestModel
import com.squareup.okhttp.*

import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject

public class ManifestDownLoader() {
    private var client: OkHttpClient? = null
    public var host: String? = "http://android.mapzen.com/"
    init {
        client = OkHttpClient()
    }


    public fun download(manifest: ManifestModel?,  callback : () -> Unit) {
        (object : AsyncTask<Void, Void, ManifestModel>() {
            override fun doInBackground(vararg params: Void): ManifestModel? {
                try {
                var request: Request =  Request.Builder()
                        .url(URL(host + "erasermap_manifest"))
                        .build();

                val response: Response = client!!.newCall(request).execute()
                    var gson: Gson = Gson()
                    val responseString: String = response.body().string()
                    return gson.fromJson(responseString,
                                    javaClass<ManifestModel>())

                } catch (ioe: IOException) {
                    Log.d("Error", "Unable to get api keys")
                }
                return null
            }

            override  fun onPostExecute(model : ManifestModel?) {
                manifest?.mintApiKey = model?.mintApiKey
                manifest?.valhallaApiKey = model?.valhallaApiKey
                manifest?.minVersion = model?.minVersion
                manifest?.vectorTileApiKeyReleaseProp = model?.vectorTileApiKeyReleaseProp
                manifest?.peliasApiKey = model?.peliasApiKey
                callback()
            }
            }).execute().get()
    }


}
