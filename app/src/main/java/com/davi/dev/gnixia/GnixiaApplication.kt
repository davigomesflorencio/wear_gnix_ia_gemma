package com.davi.dev.gnixia

import android.app.Application
import com.davi.dev.gnixia.di.AppContainer

class GnixiaApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
