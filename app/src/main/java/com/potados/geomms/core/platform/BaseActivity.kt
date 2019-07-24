/**
 * BaseActivity.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.core.platform

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.potados.geomms.R
import com.potados.geomms.core.extension.addAll
import com.potados.geomms.core.extension.inImmediateTransaction
import com.potados.geomms.core.extension.inTransaction
import com.potados.geomms.core.platform.interfaces.HasFragments
import com.potados.geomms.core.platform.interfaces.HasLayout
import com.potados.geomms.core.platform.interfaces.HasToolbar

abstract class BaseActivity : AppCompatActivity(),
    HasLayout,      /* layoutId() */
    HasToolbar,     /* toolbar(), toolbarMenuId() */
    HasFragments    /* fragments() */
{

    /**
     * Default layout for [BaseActivity].
     */
    override fun layoutId(): Int = R.layout.base_activity_layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutId())
        addFragments(savedInstanceState)

        toolbarId()?.let { setSupportActionBar(findViewById(it)) }
    }

    /**
     * Create option menu for Toolbar if it exists.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return toolbarMenuId()?.let {
            menuInflater.inflate(it, menu)
            true
        } ?: super.onCreateOptionsMenu(menu)
    }

    /**
     * Add all fragments to [supportFragmentManager].
     */
    private fun addFragments(savedInstanceState: Bundle?) =
        savedInstanceState ?:
        supportFragmentManager.inImmediateTransaction {
            addAll(R.id.fragment_container, fragments())
            this
        }

}