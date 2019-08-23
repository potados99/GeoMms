package com.potados.geomms.common.base

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.potados.geomms.R
import com.potados.geomms.common.extension.*
import com.potados.geomms.extension.withNonNull
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.navigation_activity.*
import kotlinx.android.synthetic.main.navigation_activity.nav_view
import timber.log.Timber

/**
 * Base class for Bottom Navigation View based activity,
 * providing quick transition between fragments, without state loss.
 * Fragments are only shown or hidden when tab is switched.
 * They are lazy-added to Fragment Manager on demand.
 * It has customized Toolbar. @see [toolbar]
 *
 * Usage:
 * 1. Override [fragments], which will be used as tab contents.
 * 2. Override [navigationMenuId], the id of [BottomNavigationView].
 * 3. (Optional) Override [defaultMenuItemId] and/or [layoutId].
 */
abstract class NavigationActivity : BaseActivity() {

    abstract val fragments: List<NavigationFragment>

    abstract val navigationMenuId: Int
    open val defaultMenuItemId: Int = -1
    open val layoutId: Int = R.layout.navigation_activity

    private var activeFragmentId: Int = -1

    private val onNavigationItemChanged = { menuItem: MenuItem ->
        addOrShowFragment(menuItem.itemId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        setToolbar()
        savedInstanceState ?: addOrShowFragment(defaultMenuItemId)
        setNavigationView()
    }

    private fun setToolbar() {
        setSupportActionBar(toolbar)
        withNonNull(supportActionBar) {
            setDisplayShowTitleEnabled(false)   // use title text view instead.
            setDisplayHomeAsUpEnabled(false)    // no up button
        }
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
                val fragmentToAdd = fragments.find { it.navigationItemId == id } as? Fragment
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

                    if (it.navigationItemId == id) {
                        it.setTitle(getString(it.titleId))
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
            inflateMenu(navigationMenuId)

            setOnNavigationItemSelectedListener(onNavigationItemChanged)

            defaultMenuItemId.takeIf { it > 0 }?.let(::setSelectedItemId)
        }
    }
}