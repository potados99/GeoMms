/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.potados.geomms.common.manager

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.*
import kotlinx.android.synthetic.main.bottom_sheet_container.view.*
import java.util.*

/**
 * Manages Bottom sheets like parentFragment.
 * Spawn new bottom sheet then the existing one will be hidden
 * and the new one will be shown.
 *
 * Each bottom sheet will have its parentFragment in its fragment_container.
 */
class BottomSheetManager(
    private val parentFragment: BaseFragment,
    private val rootView: ViewGroup
) {

    private val handler = Handler(Looper.getMainLooper())
    private val inflater = parentFragment.layoutInflater

    private val sheetStack = Stack<Sheet>()

    fun push(fragment: BaseFragment, cancelable: Boolean = true): Sheet {
        val sheet = createSheet(fragment, cancelable)

        // Hide currently active sheet if possible.
        if (!sheetStack.empty()) {
            val former = sheetStack.peek()

            handler.doAfter(0) {
                // Hide the former sheet before showing the new one.
                former.sheetView.apply {
                    setHidable(true)
                    hideSheet()
                }
            }
        }

        sheetStack.push(sheet)

        // Show new sheet.
        handler.doAfter(100) {
            sheet.sheetView.apply {
                halfExpandSheet()
                setHidable(false)
            }
        }

        return sheet
    }

    fun pop() {
        if (sheetStack.empty()) {
            return
        }

        // Pop only cancelable sheets.
        if (!sheetStack.peek().cancelable) {
            return
        }

        val activeSheet = sheetStack.pop() ?: return

        // Hide currently active sheet.
        handler.doAfter(0) {
            activeSheet.sheetView.apply {
                setHidable(true)
                hideSheet()
            }
        }

        // Show the last sheet.
        if (!sheetStack.empty()) {
            handler.doAfter(100) {
                // Hide the former sheet before showing the new one.
                if (!sheetStack.empty()) {
                    sheetStack.peek().sheetView.apply {
                        halfExpandSheet()
                        setHidable(false)
                    }
                }
            }
        }

        // Dispose later.
        handler.doAfter(100) {
            disposeSheet(activeSheet)
        }
    }

    fun parentFragment(): BaseFragment = parentFragment

    fun findSheetByFragment(fragment: BaseFragment): Sheet? {
        return sheetStack.find { it.childFragment == fragment }
    }

    /**
     * Collapse active sheet.
     */
    fun collapseSheet() {
        peekView()?.collapseSheet()
    }

    /**
     * Half-expand active sheet.
     */
    fun halfExpandSheet() {
        peekView()?.halfExpandSheet()
    }

    private fun createSheet(fragment: BaseFragment, cancelable: Boolean): Sheet {
        return initializeBottomSheet(cancelable).apply {

            sheetView.cancel_button.apply {
                isVisible = cancelable
                setOnClickListener { pop() }
            }

            addSheetBehavior(sheetView)
            addToRoot(sheetView)

            // Adding childFragment to a childFragment container requires
            // the container to be in the layout tree,
            // so it must be done after the sheetView(sheet) is added to the root.
            addFragment(this, fragment)
        }
    }
    private fun disposeSheet(sheet: Sheet) {
        removeFragment(sheet)
        removeFromRoot(sheet.sheetView)
    }

    private fun addFragment(sheet: Sheet, fragment: BaseFragment) {
        parentFragment.childFragmentManager.inTransaction {
            add(sheet.fragmentContainerId, fragment)
            this
        }

        fragment.onViewCreated = {
            sheet.setInitialized(true)
        }

        sheet.childFragment = fragment
    }
    private fun removeFragment(sheet: Sheet) {
        sheet.childFragment?.let {
            parentFragment.childFragmentManager.inTransaction {
                remove(it)
            }
        }
    }

    /**
     * This returns a Sheet,
     * because the returned result should include container id.
     */
    private fun initializeBottomSheet(cancelable: Boolean): Sheet {
        val containerId = generateId()

        val view = inflater.inflate(R.layout.bottom_sheet_container, rootView, false).apply {
            // When the bottom sheet is first shown, it should be hidden.
            // The sheet then will be half expanded.
            hideSheet()

            // We want to add childFragment to the fragment_container inside this sheet.
            // To achieve it we need childFragment manager.
            // One problem is, that the childFragment manager only takes a id of sheetView
            // to be a childFragment container.
            // That is a problem because we need to add childFragment for each bottom sheets.
            // In this case the id of a childFragment container is not unique.
            //
            // One solution is to change the id of the childFragment container to a random id
            // and storing it.
            //
            // The method above has a serious problem where generated id
            // collides other sheetView that has id not defined in thispackage.
            // For example, ID 2 does not exist in R.id of this project, but does in GoogleMap.
            // FUCK
            findViewById<FrameLayout>(R.id.template_fragment_container).id = containerId
        }

        return Sheet(view, containerId, cancelable = cancelable)
    }

    private fun addToRoot(view: View) {
        rootView.addView(view)
    }
    private fun removeFromRoot(view: View) {
        rootView.removeView(view)
    }

    private fun addSheetBehavior(view: View) {
        with(view) {
            /*
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
             */

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
     * So we need to check every sheetView's id in the root sheetView
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

    private fun peekView(): View? {
        return if (sheetStack.isEmpty()) null
        else sheetStack.peek().sheetView
    }

    data class Sheet(
        val sheetView: View,
        @IdRes val fragmentContainerId: Int,
        var childFragment: Fragment? = null,
        val cancelable: Boolean = true
    ) {
        /**
         * Set childFragment initialization state.
         * This is propagated to all its receivers.
         */
        fun setInitialized(initialized: Boolean) {
            _isInitialized.postValue(initialized)
        }

        private var _isInitialized = MutableLiveData<Boolean>().apply {
            postValue(false)
        }

        /**
         * Indicates if the childFragment is initialized and views are in place.
         */
        val isInitialized: LiveData<Boolean> = _isInitialized
    }
}