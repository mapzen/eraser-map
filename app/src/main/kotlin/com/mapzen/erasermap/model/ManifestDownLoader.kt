package com.mapzen.erasermap.model

import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import java.io.IOException
import java.net.URL

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
                            .build()
                    val response: Response = client!!.newCall(request).execute()
                    val responseString: String = response.body().string()
                    var gson: Gson = Gson()
                    try {
                        var model: ManifestModel = gson.fromJson(responseString,
                                javaClass<ManifestModel>())
                        return model
                    }
                    catch (e: Exception) {
                        return null
                    }
                } catch (ioe: IOException) {
                    Log.d("Error", "Unable to get api keys")
                }
                return null
            }

            override  fun onPostExecute(model : ManifestModel?) {
                if(model != null) {
                    manifest?.setValhallaApiKey(model.getValhallaApiKey())
                    manifest?.setMinVersion(model.getMinVersion())
                    manifest?.setVectorTileApiKeyReleaseProp(model.getVectorTileApiKeyReleaseProp())
                    manifest?.setPeliasApiKey(model.getPeliasApiKey())
                }
                callback()
            }
        }).execute().get()
    }
}

