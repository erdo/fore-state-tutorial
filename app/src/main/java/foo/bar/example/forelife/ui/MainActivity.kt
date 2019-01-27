package foo.bar.example.forelife.ui

import co.early.fore.lifecycle.LifecycleSyncer
import co.early.fore.lifecycle.activity.SyncableAppCompatActivity
import foo.bar.example.forelife.App
import foo.bar.example.forelife.R

/**
 * Copyright © 2019 early.co. All rights reserved.
 */
class MainActivity : SyncableAppCompatActivity() {

    override fun getThingsToObserve(): LifecycleSyncer.Observables {
        return LifecycleSyncer.Observables(App.inst.appComponent.gameModel)
    }

    override fun getResourceIdForSyncableView(): Int {
        return R.layout.activity_main
    }
}
