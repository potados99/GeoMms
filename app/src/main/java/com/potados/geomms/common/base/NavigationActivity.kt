package com.potados.geomms.common.base

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.potados.geomms.R
import com.potados.geomms.common.extension.addAll
import com.potados.geomms.common.extension.findFragmentByNavigationId
import com.potados.geomms.common.extension.inTransaction
import com.potados.geomms.common.extension.showOnly
import kotlinx.android.synthetic.main.navigation_activity.*
import timber.log.Timber

abstract class NavigationActivity : BaseActivity() {

    abstract val fragments: List<NavigationFragment>

    abstract fun navigationMenuId(): Int
    open fun defaultMenuItemId(): Int = -1

    private var activeFragmentId: Int = -1

    /**
     * Add fragments on-demand
     */
    private val onNavigationItemChanged = { menuItem: MenuItem ->
        addOrShowFragment(menuItem.itemId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navigation_activity)

        savedInstanceState ?: addOrShowFragment(defaultMenuItemId())
        setNavigationView()
    }

    /**
     * Show only fragment that has [id] as NavigationItemId.
     * If the destination fragment is not added to fragment manager, add it.
     *
     * @param id is navigation menu id of fragment.
     * An id and a fragment should consist a pair.
     *
     * @return false if [id] has no paired fragment
     * or Fragment Manager has fragment other than [NavigationFragment].
     */
    private fun addOrShowFragment(id: Int): Boolean {
        val transaction = supportFragmentManager.beginTransaction()

        try {
            // Ensure destination fragment is added
            if (supportFragmentManager.findFragmentByNavigationId(id) == null) {
                val fragmentToAdd = fragments.find { it.navigationItemId() == id } as? Fragment
                    ?: throw IllegalArgumentException("fragment of corresponding id $id not exist.")

                transaction.add(R.id.fragment_container, fragmentToAdd)
                Timber.i("add new fragment of id $id")
            }

            if (activeFragmentId == id) {
                return true
            }

            // Show only destination fragment
            // Do this only when destination fragment is not a
            // currently active fragment
            supportFragmentManager.fragments.forEach {
                    // ensure all fragments are NavigationFragment
                    if (it !is NavigationFragment) throw RuntimeException("only NavigationFragment is allowed in NavigationActivity.")

                    if (it.navigationItemId() == id) {
                        transaction.show(it)
                        it.onShow()
                    } else {
                        transaction.hide(it)
                        it.onHide()
                    }
                }

            activeFragmentId = id

            return true
        } catch (e: Throwable) {
            Timber.w(e)
            return false
        } finally {
            // commit() does not add fragment immediately.
            // It makes problem when calling [addOrShowFragment] rapidly
            // because it does not ensure fragment is added after the call.
            // So the addition can occur over one time, which throws exception.
            // Use commitNow instead.
            transaction.commitNow()
        }
    }

    private fun setNavigationView() {
        with(nav_view) {
            inflateMenu(navigationMenuId())

            setOnNavigationItemSelectedListener(onNavigationItemChanged)

            defaultMenuItemId().takeIf { it > 0 }?.let(::setSelectedItemId)
        }
    }
}