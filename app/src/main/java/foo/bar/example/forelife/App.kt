package foo.bar.example.forelife

import android.app.Application

/**
 * We're using Dagger for DI here, for a pure DI solution please see the
 * sample apps in the fore repo - the repo sample apps are also likely
 * to be more up to date than this sample
 * see: https://erdo.github.io/android-fore/#sample-apps
 *
 * Copyright © 2019 early.co. All rights reserved.
 */
class App : Application(){

    lateinit var appComponent: AppComponent private set

    override fun onCreate() {
        super.onCreate()

        inst = this
        appComponent = DaggerAppComponent.builder().appModule(AppModule(this)).build()
    }

    fun injectTestAppModule(testAppModule: AppModule) {
        appComponent = DaggerAppComponent.builder().appModule(testAppModule).build()
    }

    companion object {

        lateinit var inst: App private set

        // unfortunately the android test runner calls Application.onCreate() once _before_ we get a
        // chance to call createApplication() in ApplicationTestCase (contrary to the documentation).
        // So to prevent initialisation stuff happening before we have had a chance to set our mocks
        // during tests, we need to separate out the init() stuff, which is why we put it here,
        // to be called by the base activity of the app
        // http://stackoverflow.com/questions/4969553/how-to-prevent-activityunittestcase-from-calling-application-oncreate
        fun init() {

            // run any initialisation code here

        }
    }
}
