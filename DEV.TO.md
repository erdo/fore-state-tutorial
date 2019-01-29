---
title: Tutorial: android state v. event
published: true
description: kotlin app contrasting state and events
cover_image: https://thepracticaldev.s3.amazonaws.com/i/vhxu2g9q5a6icfkvphpr.png
tags: Android, Kotlin, fore, MVO
series: android fore tutorials
---

GUIs can be driven by two different types of data: **state** or **events**. It's a universal but under-appreciated concept. Let's grok the difference (it'll come in handy whether we're using MVI, MVVM, MVO or something else)...

We’ll make a tiny game using the [**fore**](https://erdo.github.io/android-fore/) library and the [MVO](https://erdo.github.io/android-fore/00-architecture.html#shoom) architectural pattern. We'll also touch on the **SyncTrigger** class which is fore's way of bridging the two worlds of **state** and **events**, and we'll make use of some **lifecycle classes** that fore has so that we can remove even more boiler plate than usual from our view layer.

![game of life screenshot](https://thepracticaldev.s3.amazonaws.com/i/3r1vg0xi2ym572ro87sj.png)
<figcaption>game of life</figcaption>

---
## Game rules:
- Players start with an identical number of coins
- Each turn, a player gives one of their coins to another player, chosen at random

**That's it**. Of course if they have no coins to give, they skip their turn. We highlight the number of the player who is about to play (not the player who has just played).

---

## Model

Let's tackle the M in MVO first like we did in the [**last tutorial**](https://dev.to/erdo/tutorial-android-fore-basics-1155) (if you haven't read that tutorial, you should probably check it out first).

``` kotlin
class GameModel constructor(
        private val random: Random,
        workMode: WorkMode ) : ObservableImp(workMode) {

  //we need at least 3 players
  private val players = listOf(Player(), Player(), Player(), Player())
  private var currentPlayer = 0;
  private var round = 1;

  fun next() {
    giftRandomly(currentPlayer)
    currentPlayer = (currentPlayer + 1) % players.size
    if (currentPlayer == 0) {
      round++
    }
    notifyObservers()
  }

  fun clear() {
    for (player in players) {
      player.amount = startAmount
    }
    currentPlayer = 0
    round = 1
    notifyObservers()
  }

  fun getPlayerAmount(playerNumber: Int): CashAmount {
    return players[playerNumber].amount
  }

  fun isPlayersTurn(playerNumber: Int): Boolean {
    return currentPlayer == playerNumber
  }

  fun getRound(): Int {
    return round
  }

  //move a coin forward, donate it to another player at random
  private fun giftRandomly(playerIndex: Int) {

    val donatingPlayer = players[playerIndex]
    val moveCoinBy = 1 + random.nextInt(players.size - 2)
    val receivingPlayer = players[(playerIndex + moveCoinBy) % players.size]

    if (hasCoins(donatingPlayer) && !hasMaxCoins(receivingPlayer)) {
      //add coin
      receivingPlayer.amount = CashAmount.values()[receivingPlayer.amount.ordinal + 1]
      //remove coin
      donatingPlayer.amount = CashAmount.values()[donatingPlayer.amount.ordinal - 1]
    }
  }

  private fun hasCoins(player: GameModel.Player): Boolean {
    return player.amount != CashAmount.ZERO
  }

  private fun hasMaxCoins(player: GameModel.Player): Boolean {
    return player.amount.ordinal == CashAmount.values().size - 1
  }

  class Player(var amount: CashAmount = startAmount)

  companion object {
    val startAmount = CashAmount.THREE
  }
}
```
(CashAmount.kt is [here](https://github.com/erdo/fore-state-tutorial/blob/master/app/src/main/java/foo/bar/example/forelife/feature/CashAmount.kt) btw)

- The model knows nothing about android view layer classes or contexts
- We are calling **notifyObservers()** whenever any of our model’s state changes
- We assume everything is called on the **UI thread** - and in this case there is no need to do any asynchronous work at all. (If we were saving the state in a [**database**](https://erdo.github.io/android-fore/#fore-6-db-example-room) or connecting to the [**network**](https://erdo.github.io/android-fore/#fore-4-retrofit-example) it would be a different matter of course)

A checklist for writing models when you’re using fore (or indeed something like MVVM) is maintained [here](https://erdo.github.io/android-fore/02-models.html#shoom).

## View

We’ll write a custom view class to display our game, we’re going to call it **GameOfLifeView**.

We can do it in a standard XML layout, the root of this one happens to be RelativeLayout, so our GameOfLifeView class will extend that. See the [github](https://github.com/erdo/fore-state-tutorial) repo for the full file.

``` xml
<?xml version="1.0" encoding="utf-8"?>
<foo.bar.example.forelife.ui.GameOfLifeView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="@dimen/spacer">
...
</foo.bar.example.forelife.ui.GameOfLifeView>
```

As usual, our custom view is going to handle the following things:

- get a **reference to all the view elements** we need
- get a reference to the **GameModel** so we can draw our UI based on it
- hook up the **button listeners** so that they interact with the model
- **sync our view** so that it matches the state of the GameModel at all times (show the right number of coins for each user etc)

``` kotlin
class GameOfLifeView @JvmOverloads constructor(
        context: Context?,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0 )
                : RelativeLayout(context, attrs, defStyleAttr), SyncableView {

  //models that we need
  private lateinit var gm: GameModel
  private lateinit var logger: Logger

  override fun onFinishInflate() {
    super.onFinishInflate()

    //(get view references handled for us by kotlin tools)

    getModelReferences()

    setClickListeners()
  }

  private fun getModelReferences() {
    gm = App.inst.appComponent.gameModel
    logger = App.inst.appComponent.logger
  }

  private fun setClickListeners() {
    life_next_btn.setOnClickListener { gm.next() }
    life_reset_btn.setOnClickListener { gm.clear() }
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
  }
}
```

As before, there is hardly any code because we're taking full advantage
of the MVO **syncView()** convention. That's explained in detail [here](https://erdo.github.io/android-fore/03-reactive-uis.html#syncview).

## Observer

[**Last time**](https://dev.to/erdo/tutorial-android-fore-basics-1155) we hooked up our observers manually in the view class.

This time we are using one of **fore's lifecycle** classes to do that for us, ([**SyncableAppCompatActivity**](https://github.com/erdo/android-fore/blob/master/fore-lifecycle/src/main/java/co/early/fore/lifecycle/activity/SyncableAppCompatActivity.java)).

That's another reason the view code above looks so sparse. Our activity now looks like this:

``` kotlin

class MainActivity : SyncableAppCompatActivity() {

  override fun getThingsToObserve(): LifecycleSyncer.Observables {
    return LifecycleSyncer.Observables(App.inst.appComponent.gameModel)
  }

  override fun getResourceIdForSyncableView(): Int {
    return R.layout.activity_main
  }
}

```

The [fore lifecycle](https://erdo.github.io/android-fore/04-more-fore.html#lifecycle-components) classes require you to implement the **getThingsToObserve()** function (here we return the only model that we want to observe: gameModel, but we could add as many models as we want to observe here).

We also need to implement the **getResourceIdForSyncableView()** function (where we just return the xml layout for a view that implements SyncableView - as our GameOfLifeView does).

---

## Dependency Injection

Last time we rolled our own pure [DI](https://erdo.github.io/android-fore/05-extras.html#dependency-injection-basics) solution. This time we are using a bare-bones Dagger 2.0 implementation, just because.

``` kotlin
gameModel = App.inst.appComponent.gameModel
```
As before, this provides the view with the same instance of the model each time, so that everything remains consistent, even when we rotate the screen.

![gif showing the app rotating](https://thepracticaldev.s3.amazonaws.com/i/fujmzsqfvfu6y3de7af9.gif)
<figcaption>rotation support as standard</figcaption>

As always with MVO and fore: **rotation support** and **testability** come as standard.

-----

## Introducing an Event

At the moment our UI is driven by state, held in the GameModel. It's because of the "statey-ness" of MVO that we can so easily support rotation on android - all we need to do to keep our UI totally consistent is to re-sync with the model by calling syncView() whenever we want.

But let's add a new design requirement:

*When the game transitions to an economy where at least one player has no coins, we want to display a Snackbar indicating that this transition (or event) took place, and the same in reverse*.

We can easily add this piece of state to our game model and provide access to it like this: *fun hasBankruptPlayers(): Boolean*

But the Snackbar UI component is much more suited to *event* type data, and using syncView() to trigger a Snackbar would cause us [problems](https://erdo.github.io/android-fore/05-extras.html#notification-counting). A robust implementation of syncView() needs to make no assumptions about the number of times it is called, it might be called more than we expect - we definitely don't want to show 3 duplicate Snackbars by mistake.

It's an issue you find in all "statey" architectures like MVVM, MVO, and MVI (not so much in MVP as that's more "eventy"). Luckily there are some nice ways to handle it, in **fore** we can use the **SyncTrigger** class as a bridge from the state driven world to the event driven one.

## SyncTrigger

We need to specify 2 things to use a SyncTrigger:

- what we want to happen when it is triggered (typically show a snackbar or a dialog; start an activity; or run an animation)
- the threshold required to trip the trigger (network connectivity goes from connected->disconnected; an error state goes from false->true; the amount of money in an account goes below a certain limit; a timer goes beyond a certain duration)

We are going to add two SyncTriggers to the app:

``` kotlin

private fun setupTriggers() {

  showHasBankruptciesTrigger = SyncTrigger(
    //do this
    { Snackbar.make(this, context.getString(R.string.bankruptcies_true), LENGTH_SHORT).show() },
    //when this
    { gameModel.hasBankruptPlayers() })

  showNoBankruptciesTrigger = SyncTrigger(
    //do this
    { Snackbar.make(this, context.getString(R.string.bankruptcies_false), LENGTH_SHORT).show() },
    //when this
    { !gameModel.hasBankruptPlayers() })
}

```

We also need to check them each time we run through syncView():


``` kotlin
override fun syncView() {

  ...

  showHasBankruptciesTrigger.checkLazy()
  showNoBankruptciesTrigger.checkLazy()
}
```

_Because these SyncTriggers exist in our view layer, they will be destroyed and recreated by android if we rotate our view. If their threshold conditions are already breached, they will be fired immediately upon rotation (when syncView() is called). If the device is rotated again, they will be fired again etc. This is probably not what we want. Hence **checkLazy()** ignores a threshold breach if it happens the first time you check a newly constructed SyncTrigger. If you do actually want the trigger fired the first time it's checked, you can call **check()** instead of checkLazy()._


### Reset behaviour

Until it is reset, a SyncTrigger will fire only once, and it will fire the first time the trigger condition goes from being false to true (when checked).

By default, a SyncTrigger is reset according to the **ResetRule.ONLY_AFTER_REVERSION**, which means that the trigger condition needs to flip back to false, before it is eligible for re-triggering.

Alternatively you can construct a SyncTrigger with the flag: **ResetRule.IMMEDIATELY**, this means that the SyncTrigger can be continually triggered each time it is checked and the trigger condition is true.

_(There is no **ResetRule.NEVER** because SyncTriggers typically live in the view layer and in order to implement ResetRule.NEVER they would need to persist their state to survive across rotations. If you need that kind of behaviour, you'll need to implement it yourself inside the trigger threshold-check in combination with some data that you persist somewhere)._


### KISS

SyncTriggers are a great way of firing off nice animations in response to state changes happening in models. It's used pretty effectively in this [tic-tac-toe](https://erdo.github.io/android-fore/#fore-5-ui-helpers-example-tic-tac-toe) game for instance. Here is the [view code](https://github.com/erdo/android-fore/blob/master/example05ui/src/main/java/foo/bar/example/foreui/ui/tictactoe/TicTacToeView.java) (java) where you can see the SyncTriggers in action.

You won't always need them though, button click listeners are event based already for example. I recommend taking a look at the discussion about this in the [fore docs](https://erdo.github.io/android-fore/05-extras.html#state-versus-events).


-----

Thanks for reading! If you're thinking of using fore in your team, the fore docs have most of the basics covered in easy to digest sample apps, e.g. [adapters](https://erdo.github.io/android-fore/#fore-3-adapter-example), [networking](https://erdo.github.io/android-fore/#fore-4-retrofit-example) or [databases](https://erdo.github.io/android-fore/#fore-6-db-example-room-db-driven-to-do-list).

here’s the [complete code](https://github.com/erdo/fore-state-tutorial) for the tutorial
