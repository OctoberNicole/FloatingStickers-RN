// write a native module to launch StickerActivity using react native bridge
package com.rnarcore

import android.content.Intent
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.rnarcore.StickerActivity
import android.app.Activity
import android.util.Log
import com.facebook.react.bridge.Callback

class Loader(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return "Loader"
    }

    @ReactMethod
    fun launchStickerActivity() {
        val intent = Intent(currentActivity, StickerActivity::class.java)
        currentActivity?.startActivity(intent)
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun fetchData(callback: Callback){
        val reading = StickerActivity.READING;
        callback.invoke(reading);
    }
}

