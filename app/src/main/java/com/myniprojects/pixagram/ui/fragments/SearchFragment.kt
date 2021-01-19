package com.myniprojects.pixagram.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.myniprojects.pixagram.R
import com.myniprojects.pixagram.databinding.FragmentSearchBinding
import com.myniprojects.pixagram.utils.hideKeyboard
import com.myniprojects.pixagram.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search)
{
    private val binding by viewBinding(FragmentSearchBinding::bind)

    private lateinit var menuItemSearch: MenuItem
    private lateinit var searchView: SearchView

    private val searchTypesArray = enumValues<SearchType>()
    private var selectedSearchTypeIndex = 0

    private fun selectNextSearchType()
    {
        selectedSearchTypeIndex++
        selectedSearchTypeIndex %= searchTypesArray.size
    }

    val currentSearchType: SearchType
        get() = searchTypesArray[selectedSearchTypeIndex]

    override

    fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_toolbar_search, menu)

        menuItemSearch = menu.findItem(R.id.itemSearch)
        searchView = menuItemSearch.actionView as SearchView

        // change searchText if it has been set. TODO
        // viewModel.queryFlow.value?.let {
        //     menuItemSearch!!.expandActionView()
        //     searchView!!.setQuery(it, false)
        //     searchView!!.clearFocus()
        // }

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener
            {
                override fun onQueryTextSubmit(textInput: String?): Boolean
                {
                    hideKeyboard()
                    //viewModel.submitQuery(textInput)
                    return true
                }

                override fun onQueryTextChange(p0: String?): Boolean
                {
                    return true
                }

            }
        )

        menuItemSearch.setOnActionExpandListener(
            object : MenuItem.OnActionExpandListener
            {
                override fun onMenuItemActionExpand(p0: MenuItem?): Boolean
                {
                    return true
                }

                override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean
                {
                    //viewModel.submitQuery(null)
                    return true
                }
            }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when (item.itemId)
        {
            R.id.itemSearchType ->
            {
                selectNextSearchType()
                val d = ContextCompat.getDrawable(requireContext(), currentSearchType.icon)
                if (d != null)
                {
                    DrawableCompat.setTint(
                        d,
                        ContextCompat.getColor(requireContext(), R.color.icon_tint_toolbar)
                    )
                    item.icon = d
                }

                item.title = getString(currentSearchType.title)
                true
            }
            else ->
            {
                super.onOptionsItemSelected(item)
            }
        }
    }


    enum class SearchType(
        @DrawableRes val icon: Int,
        @StringRes val title: Int
    )
    {
        USER(
            R.drawable.ic_outline_person_outline_24,
            R.string.user
        ),
        TAG(
            R.drawable.ic_outline_tag_24,
            R.string.tag
        )
    }
}
