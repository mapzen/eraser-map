package com.mapzen.erasermap.view

import android.content.Context
import android.text.Spannable
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.mapzen.erasermap.R
import com.mapzen.pelias.widget.AutoCompleteAdapter
import com.mapzen.pelias.widget.PeliasSearchView

class SearchListViewAdapter(context: Context, resource: Int, private val searchView:
    PeliasSearchView) : AutoCompleteAdapter(context, resource) {

    private var iconId: Int = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val textView = super.getView(position, convertView, parent) as TextView
        val item = getItem(position)
        textView.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0)

        textView.setText(textView.text, TextView.BufferType.SPANNABLE)
        val spannableText = textView.text as Spannable
        if( item.simpleFeature != null) {
            val nameLength = item.simpleFeature.name().length
            spannableText.setSpan(TextAppearanceSpan(this.context, R.style.searchAddr),
                    nameLength, textView.text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableText.setSpan(TextAppearanceSpan(this.context, R.style.searchName), 0,
                    nameLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val query = searchView.query.toString()
        val subStringIndex = textView.text.toString().toLowerCase().indexOf(query.toLowerCase())
        if (subStringIndex >= 0 && (query.length > 2)) {
            spannableText.setSpan(StyleSpan(android.graphics.Typeface.BOLD), subStringIndex,
                    subStringIndex + query.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        textView.text = spannableText
        return textView
    }

    override fun setIcon(resId: Int) {
        iconId = resId
    }
}
