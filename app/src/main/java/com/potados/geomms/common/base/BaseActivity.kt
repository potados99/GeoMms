/**
 * BaseActivity.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.common.base

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.potados.geomms.R
import com.potados.geomms.common.extension.addAll
import com.potados.geomms.common.extension.inImmediateTransaction
import com.potados.geomms.common.base.interfaces.HasFragments
import com.potados.geomms.common.base.interfaces.HasLayout
import com.potados.geomms.common.base.interfaces.HasToolbar

abstract class BaseActivity : AppCompatActivity(),
    HasLayout,      /* layoutId() */
    HasToolbar,     /* toolbar(), toolbarMenuId() */
    HasFragments    /* fragments() */
{

    /**
     * Default layout for [BaseActivity].
     */
    override fun layoutId(): Int = R.layout.base_activity_layout
    override fun fragmentContainerId(): Int = R.id.fragment_container

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
            addAll(fragmentContainerId(), fragments())
            this
        }

}