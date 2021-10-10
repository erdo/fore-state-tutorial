package foo.bar.example.forelife.ui

import android.app.Activity
import com.google.android.material.snackbar.Snackbar

fun Activity.showSnackbar(message: String) {
    Snackbar.make(
        window.decorView.rootView,
        message, Snackbar.LENGTH_SHORT
    ).show()
}

