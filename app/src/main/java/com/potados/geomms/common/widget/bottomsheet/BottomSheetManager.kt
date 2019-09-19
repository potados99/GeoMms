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
import com.potados.geomms.common.widget.CustomBottomSheetBehavior
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

    fun push(fragment: Fragment, cancelable: Boolean = true): Sheet {
        val sheet = createSheet(fragment, cancelable)

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

        // Show new sheet.
        handler.postDelayed({
            sheet.view.apply {
                halfExpandSheet()
                setHidable(false)
            }
        }, 100)

        sheetStack.push(sheet)

        return sheet
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

            addSheetBehavior(view)
            addToRoot(view)
            addFragment(this, fragment)
        }
    }
    private fun disposeSheet(sheet: Sheet) {
        removeFragment(sheet)
        removeFromRoot(sheet.view)
    }

    private fun addFragment(sheet: Sheet, fragment: Fragment) {
        parent.childFragmentManager.inTransaction {
            add(sheet.fragmentContainerId, fragment)
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
        val containerId = generateId()

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
            //
            // The method above has a serious problem where generated id
            // collides other view that has id not defined in this package.
            // For example, ID 2 does not exist in R.id of this project, but does in GoogleMap.
            // FUCK
            findViewById<FrameLayout>(R.id.template_fragment_container).id = containerId
        }

        return Sheet(view, containerId)
    }

    private fun addToRoot(view: View) {
        rootView.addView(view)
    }
    private fun removeFromRoot(view: View) {
        rootView.removeView(view)
    }

    private fun addSheetBehavior(view: View) {
        with(view) {
       //    onSlideBottomSheet(view, 0f)

      //      collapseSheet()

            bottomSheetBehavior.setCallback(
                onStateChanged = {
                    when (it) {
                        CustomBottomSheetBehavior.STATE_EXPANDED -> view.sheet_grip.alpha = 0f
                        else -> view.sheet_grip.alpha = 1f
                    }
                },
                onSlide = {
                    onSlideBottomSheet(view, it)
                }
            )

            setOnClickListener {
                toggleSheet()
            }
        }
    }

    private fun onSlideBottomSheet(sheet: View, offset: Float) {
        with(sheet) {
            sheet_grip.setAlphaByOffset(offset)
            setBackgroundRadiusByOffset(offset)
        }
    }

    /**
     * View.generateViewId prevents collide
     * with ID values generated at build time
     * BUT the FUCK google map makes collision.
     * So we need to check every view's id in the root view
     * and avoid selecting id that already exists.
     */
    private fun generateId(): Int {
        val ids = rootView.ids()
        while (true) {
            val generated = View.generateViewId()
            if (generated !in ids) {
                return generated
            }
        }
    }

    data class Sheet(val view: View, @IdRes val fragmentContainerId: Int, var fragment: Fragment? = null)
}