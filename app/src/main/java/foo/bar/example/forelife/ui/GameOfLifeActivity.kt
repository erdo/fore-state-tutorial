package foo.bar.example.forelife.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import co.early.fore.core.observer.Observer
import co.early.fore.core.ui.SyncTrigger
import co.early.fore.core.ui.SyncableView
import co.early.fore.kt.core.logging.Logger
import com.google.android.material.snackbar.Snackbar
import foo.bar.example.forelife.App
import foo.bar.example.forelife.R
import foo.bar.example.forelife.feature.GameModel
import kotlinx.android.synthetic.main.activity_main.life_next_btn
import kotlinx.android.synthetic.main.activity_main.life_player1cash_img
import kotlinx.android.synthetic.main.activity_main.life_player1icon_img
import kotlinx.android.synthetic.main.activity_main.life_player2cash_img
import kotlinx.android.synthetic.main.activity_main.life_player2icon_img
import kotlinx.android.synthetic.main.activity_main.life_player3cash_img
import kotlinx.android.synthetic.main.activity_main.life_player3icon_img
import kotlinx.android.synthetic.main.activity_main.life_player4cash_img
import kotlinx.android.synthetic.main.activity_main.life_player4icon_img
import kotlinx.android.synthetic.main.activity_main.life_reset_btn
import kotlinx.android.synthetic.main.activity_main.life_round_txt

/**
 * Copyright © 2019 early.co. All rights reserved.
 */
class GameOfLifeActivity : AppCompatActivity(R.layout.activity_main), SyncableView {

    //models that we need
    private lateinit var gm: GameModel
    private lateinit var logger: Logger

    //single observer reference
    var observer = Observer { syncView() }

    //triggers
    private lateinit var showHasBankruptciesTrigger: SyncTrigger
    private lateinit var showNoBankruptciesTrigger: SyncTrigger


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //(get view references handled for us by kotlin tools)

        getModelReferences()

        setClickListeners()

        setupTriggers()
    }

    private fun getModelReferences() {
        gm = App.inst.appComponent.gameModel
        logger = App.inst.appComponent.logger
    }

    private fun setClickListeners() {
        life_next_btn.setOnClickListener { gm.next() }
        life_reset_btn.setOnClickListener { gm.clear() }
    }

    private fun setupTriggers() {

        showHasBankruptciesTrigger = SyncTrigger(
            //do this
            {
                Snackbar.make(
                    window.decorView.rootView,
                    getString(R.string.bankruptcies_true), Snackbar.LENGTH_SHORT
                ).show()
            },
            //when this
            { gm.hasBankruptPlayers() })

        showNoBankruptciesTrigger = SyncTrigger(
            //do this
            {
                Snackbar.make(
                    window.decorView.rootView,
                    getString(R.string.bankruptcies_false), Snackbar.LENGTH_SHORT
                ).show()
            },
            //when this
            { !gm.hasBankruptPlayers() })
    }


    // reactive UI implementation

    override fun syncView() {

        life_player1cash_img.setImageResource(gm.getPlayerAmount(0).resId)
        life_player2cash_img.setImageResource(gm.getPlayerAmount(1).resId)
        life_player3cash_img.setImageResource(gm.getPlayerAmount(2).resId)
        life_player4cash_img.setImageResource(gm.getPlayerAmount(3).resId)

        life_player1icon_img.setImageResource(if (gm.isPlayersTurn(0)) R.drawable.player_01_highlight else R.drawable.player_01)
        life_player2icon_img.setImageResource(if (gm.isPlayersTurn(1)) R.drawable.player_02_highlight else R.drawable.player_02)
        life_player3icon_img.setImageResource(if (gm.isPlayersTurn(2)) R.drawable.player_03_highlight else R.drawable.player_03)
        life_player4icon_img.setImageResource(if (gm.isPlayersTurn(3)) R.drawable.player_04_highlight else R.drawable.player_04)

        life_round_txt.text = resources.getString(R.string.round, gm.getRound())

        showHasBankruptciesTrigger.checkLazy()
        showNoBankruptciesTrigger.checkLazy()
    }

    override fun onStart() {
        super.onStart()
        gm.addObserver(observer)
        syncView() //  <- don't forget this
    }

    override fun onStop() {
        super.onStop()
        gm.removeObserver(observer)
    }
}
