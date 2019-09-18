package com.potados.geomms.common.widget.bottomsheet

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.potados.geomms.R
import com.potados.geomms.common.extension.*
import com.potados.geomms.util.Types
import kotlinx.android.synthetic.main.bottom_sheet_container.view.*
import timber.log.Timber
import java.util.*

/**
 * Manages Bottom sheets like parent.
 * Spawn new bottom sheet then the existing one will be hidden
 * and the new one will be shown.
 *
 * Each bottom sheet will have its parent in its fragment_container.
 */
class BottomSheetManager(
    private val parent: Fragment,
    private val rootView: ViewGroup
) {

    private val handler = Handler(Looper.getMainLooper())
    private val inflater = parent.layoutInflater

    private val sheetStack = Stack<Sheet>()

    fun push(fragment: Fragment, cancelable: Boolean = true) {
        val sheet = createSheet(fragment, cancelable)

        /*
        // Hide currently active sheet if possible.
        if (!sheetStack.empty()) {
            val former = sheetStack.peek()

            handler.post {
                // Hide the former sheet before showing the new one.
                former.view.apply {
                    setHidable(true)
                    hideSheet()
                    Timber.e("Hide the former.")
                }
            }
        }
        */


        // Show new sheet.
        handler.postDelayed({
            sheet.view.apply {
                halfExpandSheet()
                setHidable(false)
            }
        }, 100)

        sheetStack.push(sheet)
    }

    fun pop() {
        if (sheetStack.empty()) {
            return
        }

        val activeSheet = sheetStack.pop()

        // Hide currently active sheet.
        handler.post {
            activeSheet.view.apply {
                setHidable(true)
                hideSheet()
            }
        }

        // Show the last sheet.
        if (!sheetStack.empty()) {
            handler.postDelayed({
                // Hide the former sheet before showing the new one.
                sheetStack.peek().view.apply {
                    halfExpandSheet()
                    setHidable(false)
                }
            }, 100)
        }

        handler.postDelayed({
            disposeSheet(activeSheet)
        }, 100)

    }

    private fun createSheet(fragment: Fragment, cancelable: Boolean): Sheet {
        return inflateBottomSheet().apply {
            view.cancel_button.apply {
                isVisible = cancelable
                setOnClickListener { pop() }
            }

            addFragment(this, fragment)
            addToRoot(view)
        }
    }

    private fun disposeSheet(sheet: Sheet) {
        removeFragment(sheet)
        removeFromRoot(sheet.view)
    }

    private fun addFragment(sheet: Sheet, fragment: Fragment) {
        parent.childFragmentManager.inTransaction {
            // add(sheet.fragmentContainerId, fragment)
            this
        }

        sheet.fragment = fragment
    }

    private fun removeFragment(sheet: Sheet) {
        sheet.fragment?.let {
            parent.childFragmentManager.inTransaction {
                remove(it)
            }
        }
    }

    private fun inflateBottomSheet(): Sheet {
        val idForFragmentContainer = View.generateViewId()

        val view = inflater.inflate(R.layout.bottom_sheet_container, rootView, false).apply {
            // When the bottom sheet is first shown, it should be hidden.
            // The sheet then will be half expanded.
            hideSheet()

            // We want to add fragment to the fragment_container inside this sheet.
            // To achieve it we need fragment manager.
            // One problem is, that the fragment manager only takes a id of view
            // to be a fragment container.
            // That is a problem because we need to add fragment for each bottom sheets.
            // In this case the id of a fragment container is not unique.
            //
            // One solution is to change the id of the fragment container to a random id
            // and storing it.
            fragment_container.id = idForFragmentContainer
        }

        return Sheet(view, idForFragmentContainer)
    }

    private fun addToRoot(view: View) {
        rootView.addView(view)
    }

    private fun removeFromRoot(view: View) {
        rootView.removeView(view)
    }

    data class Sheet(val view: View, @IdRes val fragmentContainerId: Int, var fragment: Fragment? = null)
}