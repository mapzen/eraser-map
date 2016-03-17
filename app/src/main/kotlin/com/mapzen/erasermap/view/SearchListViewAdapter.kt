package com.mapzen.erasermap.view

import android.content.Context
import android.graphics.Typeface
import android.preference.PreferenceManager
import android.text.Spannable
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.mapzen.erasermap.R
import com.mapzen.pelias.SavedSearch
import com.mapzen.pelias.widget.AutoCompleteAdapter
import com.mapzen.pelias.widget.PeliasSearchView

class SearchListViewAdapter(context: Context, resource: Int, private val searchView:
PeliasSearchView, private val savedSearch: SavedSearch) : AutoCompleteAdapter(context, resource) {

    private var iconId: Int = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val textView = super.getView(position, convertView, parent) as TextView
        val item = getItem(position)
        savedSearch?.deserialize(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SavedSearch.TAG, null))
        textView.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0)

        if( item.simpleFeature != null) {
            var text = item.simpleFeature.name() + '\n' + item.simpleFeature.address()
            textView.setText(text, TextView.BufferType.SPANNABLE)
            textView.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0)

            for (term in savedSearch.getTerms()) {
                if (term.equals(item.simpleFeature.label())) {
                    textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_recent, 0, 0, 0)
                }
            }

            val spannableText = textView.text as Spannable
            val nameLength = item.simpleFeature.name().length
            spannableText.setSpan(TextAppearanceSpan(this.context, R.style.searchAddr),
                    nameLength, textView.text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableText.setSpan(TextAppearanceSpan(this.context, R.style.searchName), 0,
                    nameLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            val query = searchView.query.toString()
            val subStringIndex = textView.text.toString().toLowerCase().indexOf(query.toLowerCase())
            if (subStringIndex >= 0 && (query.length > 2)) {
                spannableText.setSpan(StyleSpan(Typeface.BOLD), subStringIndex,
                        subStringIndex + query.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            textView.text = spannableText
        }
        return textView
    }

    override fun setIcon(resId: Int) {
        iconId = resId
    }
}
