package foo.bar.example.forelife.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import co.early.fore.core.logging.Logger
import co.early.fore.core.ui.SyncTrigger
import co.early.fore.core.ui.SyncableView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
import foo.bar.example.forelife.App
import foo.bar.example.forelife.R
import foo.bar.example.forelife.feature.GameModel
import kotlinx.android.synthetic.main.activity_main.view.life_next_btn
import kotlinx.android.synthetic.main.activity_main.view.life_player1cash_img
import kotlinx.android.synthetic.main.activity_main.view.life_player1icon_img
import kotlinx.android.synthetic.main.activity_main.view.life_player2cash_img
import kotlinx.android.synthetic.main.activity_main.view.life_player2icon_img
import kotlinx.android.synthetic.main.activity_main.view.life_player3cash_img
import kotlinx.android.synthetic.main.activity_main.view.life_player3icon_img
import kotlinx.android.synthetic.main.activity_main.view.life_player4cash_img
import kotlinx.android.synthetic.main.activity_main.view.life_player4icon_img
import kotlinx.android.synthetic.main.activity_main.view.life_reset_btn
import kotlinx.android.synthetic.main.activity_main.view.life_round_txt

/**
 * Copyright © 2019 early.co. All rights reserved.
 */
class GameOfLifeView @JvmOverloads constructor(
        context: Context?,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr),
    SyncableView {

    //models that we need
    private lateinit var gm: GameModel
    private lateinit var logger: Logger

    //triggers
    private lateinit var showHasBankruptciesTrigger: SyncTrigger
    private lateinit var showNoBankruptciesTrigger: SyncTrigger

    //single observer reference
    private var observer = this::syncView


    override fun onFinishInflate() {
        super.onFinishInflate()

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
            { Snackbar.make(this, context.getString(R.string.bankruptcies_true), LENGTH_SHORT).show() },
            //when this
            { gm.hasBankruptPlayers() })

        showNoBankruptciesTrigger = SyncTrigger(
            //do this
            { Snackbar.make(this, context.getString(R.string.bankruptcies_false), LENGTH_SHORT).show() },
            //when this
            { !gm.hasBankruptPlayers() })
    }

    //data binding stuff

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        gm.addObserver(observer)
        syncView() //  <- don't forget this
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        gm.removeObserver(observer)
    }

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
}
