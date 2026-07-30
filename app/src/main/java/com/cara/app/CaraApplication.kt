package com.cara.app

import android.app.Application
import com.cara.app.data.session.UserSession

class CaraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        UserSession.init(this)
    }
}
