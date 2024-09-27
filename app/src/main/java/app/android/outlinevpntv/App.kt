package app.android.outlinevpntv

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory

class App : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .respectCacheHeaders(false)
            .build()
    }
}