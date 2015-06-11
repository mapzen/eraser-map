package util

import com.squareup.okhttp.Cache
import com.squareup.okhttp.OkHttpClient
import org.oscim.tiling.source.HttpEngine
import org.oscim.tiling.source.OkHttpEngine
import org.oscim.tiling.source.UrlTileSource

public class HttpCacheFactory : HttpEngine.Factory {
    private val mClient = OkHttpClient()


    public constructor(responseCache: Cache?) {
        this.mClient.setCache(responseCache)
    }

    override fun create(tileSource: UrlTileSource): OkHttpEngine {
        return OkHttpEngine(this.mClient, tileSource)
    }
}

