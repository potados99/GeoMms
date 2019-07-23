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
import com.potados.geomms.core.extension.inTransaction

abstract class BaseActivity : AppCompatActivity(), HasToolbar {

    abstract fun fragments(): Collection<BaseFragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.base_activity_layout)
        toolbar()?.let { setSupportActionBar(it) }
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

    private fun addFragments(savedInstanceState: Bundle?) =
        savedInstanceState ?:
        supportFragmentManager.inTransaction {
            fragments().forEach {
                add(R.id.fragment_container, it)
            }
            this
        }




}