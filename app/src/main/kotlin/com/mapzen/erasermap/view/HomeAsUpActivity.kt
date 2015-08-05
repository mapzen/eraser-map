package com.mapzen.erasermap.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

open class HomeAsUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.getItemId() == android.R.id.home) {
            finish()
            return true
        }

        return false
    }
}
