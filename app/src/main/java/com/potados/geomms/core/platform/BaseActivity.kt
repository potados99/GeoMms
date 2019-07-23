/**
 * BaseActivity.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.core.platform

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.potados.geomms.R
import com.potados.geomms.core.extension.addAll
import com.potados.geomms.core.extension.inTransaction
import com.potados.geomms.core.platform.interfaces.HasFragments
import com.potados.geomms.core.platform.interfaces.HasToolbar

abstract class BaseActivity : AppCompatActivity(), HasToolbar, HasFragments {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.base_activity_layout)
        addFragments(savedInstanceState)
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
     * Set toolbar if exist.
     */
    private fun setToolbar() {
        toolbar()?.let { setSupportActionBar(it) }
    }

    /**
     * Add all fragments to [supportFragmentManager].
     */
    private fun addFragments(savedInstanceState: Bundle?) =
        savedInstanceState ?:
        supportFragmentManager.inTransaction {
            addAll(R.id.fragment_container, fragments())
            this
        }




}