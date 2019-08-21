package com.potados.geomms.common.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.potados.geomms.R
import com.potados.geomms.common.extension.addAll
import com.potados.geomms.common.extension.inImmediateTransaction

abstract class SingleFragmentActivity : BaseActivity() {

    abstract val fragment: Fragment

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.single_fragment_activity)

        addFragments(savedInstanceState)
    }

    private fun addFragments(savedInstanceState: Bundle?) =
        savedInstanceState ?:
        supportFragmentManager.inImmediateTransaction {
            add(R.id.fragment_container, fragment)
            this
        }
}