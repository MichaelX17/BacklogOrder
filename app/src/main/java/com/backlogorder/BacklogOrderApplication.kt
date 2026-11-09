package com.backlogorder

import android.app.Application
import com.backlogorder.di.AppContainer
import com.backlogorder.di.DefaultAppContainer

class BacklogOrderApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
