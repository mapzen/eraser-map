package com.mapzen.erasermap.model

import android.os.AsyncTask
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


public class ManifestDownLoader() {
    public var s3Client: AmazonS3? = null
    public var host: String? = "android.mapzen.com"
    public var fileName: String? = "erasermap_manifest"
    public var key: String? = null
    public var secret: String? = null

    init {
        try {
            System.loadLibrary("leyndo");
            key = getDecryptedAwsKey()
            secret = getDecryptedAwsSecret()
            s3Client = AmazonS3Client(BasicAWSCredentials(key, secret))
        } catch (e: UnsatisfiedLinkError) {
            if ("Dalvik".equals(System.getProperty("java.vm.name"))) {
                throw e;
            }
        }
    }

    native fun getAwsSecret() : String
    native fun getAwsKey() : String

    public fun getDecryptedAwsKey(): String {
        var key: String = ""
        var crypt: Char = '#'
        var encrypted = getAwsKey().toCharArray()
        for(letter in encrypted) {
            key += letter.toInt().xor(crypt.toInt()).toChar().toString()
        }
        return key
    }

    public fun getDecryptedAwsSecret(): String {
        var key: String = ""
        var crypt: Char = '#'
        var encrypted = getAwsSecret().toCharArray()
        for(letter in encrypted) {
            key += letter.toInt().xor(crypt.toInt()).toChar().toString()
        }
        key = key.removeSuffix(getDecryptedAwsKey());
        return key
    }

    public fun download(manifest: ManifestModel?,  callback : () -> Unit) {
        (object : AsyncTask<Void, Void, ManifestModel>() {
            override fun doInBackground(vararg params: Void): ManifestModel? {
                try {
                    if (key != null && secret != null) {
                        var gson: Gson = Gson()
                        try {
                            var manifest = s3Client?.getObject(GetObjectRequest(
                                    host, fileName ))
                            var reader: BufferedReader = BufferedReader(
                                    InputStreamReader(manifest?.getObjectContent()));

                            var responseString: String = ""
                            while (true) {
                                var line = reader.readLine()
                                if (line == null) break
                                responseString += line
                            }

                            var model: ManifestModel = gson.fromJson(responseString,
                                    javaClass<ManifestModel>())
                            return model
                        } catch (e: Exception) {
                            return null
                        }
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

