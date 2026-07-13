package com.app.personalization.presentation.theme

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.personalization.R
import com.app.personalization.presentation.wallpaper.DIYWallpaperActivity
import com.app.personalization.presentation.main.MainActivity
import com.app.personalization.presentation.theme.MyThemeActivity
import com.app.personalization.presentation.customviews.PremiumActivity
import com.app.personalization.presentation.theme.ThemeBuilderActivity
import com.app.personalization.presentation.theme.ThemePreviewActivity
import com.app.personalization.presentation.widget.WidgetConfigActivity
import com.app.personalization.presentation.customviews.GemView
import com.app.personalization.presentation.customviews.HomeActionView

class ThemeFragment : Fragment() {

    private lateinit var viewModel: ThemeViewModel
    private lateinit var categoryAdapter: ThemeCategoryAdapter
    private lateinit var themeAdapter: ThemeAdapter
    private lateinit var pbCreate: ProgressBar
    
    private var gemView: GemView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_theme, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ThemeViewModel::class.java]

        setupToolbar(view)
        setupQuickActions(view)
        setupCategoryList(view)
        setupThemeGrid(view)
        setupFABs(view)

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadCoins()
        // Reload categories and themes to capture any new DIY themes
        viewModel.selectCategory("all")
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<View>(R.id.toolbar) ?: return
        
        val ivRefresh = toolbar.findViewById<View>(R.id.ivRefresh)
        ivRefresh?.visibility = View.VISIBLE
        ivRefresh?.setOnClickListener {
            viewModel.loadRandomThemes()
            Toast.makeText(context, "Themes shuffled", Toast.LENGTH_SHORT).show()
        }

        val llUpgrade = toolbar.findViewById<View>(R.id.llUpgrade)
        llUpgrade?.setOnClickListener {
            val intent = Intent(context, PremiumActivity::class.java)
            startActivity(intent)
        }

        gemView = toolbar.findViewById(R.id.gemView)
    }

    private fun setupQuickActions(view: View) {
        val actionView = view.findViewById<HomeActionView>(R.id.actionView) ?: return
        actionView.setListener(object : HomeActionView.OnHomeActionViewListener {
            override fun onSelect(action: HomeActionView.HomeActionType) {
                when (action) {
                    HomeActionView.HomeActionType.WALLPAPER -> {
                        startActivity(Intent(context, DIYWallpaperActivity::class.java))
                    }
                    HomeActionView.HomeActionType.THEME -> {
                        startActivity(Intent(context, ThemeBuilderActivity::class.java))
                    }
                    HomeActionView.HomeActionType.WIDGET -> {
                        startActivity(Intent(context, WidgetConfigActivity::class.java))
                    }
                    HomeActionView.HomeActionType.KEYBOARD -> {
                        showKeyboardSetupDialog()
                    }
                }
            }
        })
    }

    private fun showKeyboardSetupDialog() {
        val context = context ?: return
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Keyboard IME Setup")
        builder.setMessage("Configure 'Custom Keyboard IME' as your default system keyboard:")
        
        builder.setPositiveButton("1. Enable in Settings") { dialog, _ ->
            try {
                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to open settings", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        
        builder.setNegativeButton("2. Select as Default") { dialog, _ ->
            try {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to open input method picker", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        
        builder.setNeutralButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        
        builder.show()
    }

    private fun setupCategoryList(view: View) {
        val rvCategories = view.findViewById<RecyclerView>(R.id.categoryRecyclerView) ?: return
        rvCategories.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        
        categoryAdapter = ThemeCategoryAdapter { categoryTag ->
            viewModel.selectCategory(categoryTag.id)
        }
        rvCategories.adapter = categoryAdapter
    }

    private fun setupThemeGrid(view: View) {
        val rvThemes = view.findViewById<RecyclerView>(R.id.recyclerView) ?: return
        val context = requireContext()
        val displayMetrics = context.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        // Keyboard grid: Target ~160dp width per item
        val columns = (screenWidthDp / 160).toInt().coerceAtLeast(2)
        rvThemes.layoutManager = GridLayoutManager(context, columns)
        
        pbCreate = view.findViewById(R.id.pbCreate)
        pbCreate.visibility = View.VISIBLE

        themeAdapter = ThemeAdapter(
            onThemeClick = { theme ->
                val intent = Intent(context, ThemePreviewActivity::class.java).apply {
                    putExtra("theme_id", theme.id)
                    putExtra("theme_name", theme.name)
                    putExtra("theme_path", theme.path)
                    putExtra("theme_type", theme.rawType)
                }
                startActivity(intent)
            }
        )
        rvThemes.adapter = themeAdapter
    }

    private fun setupFABs(view: View) {
        view.findViewById<View>(R.id.tvAdd)?.setOnClickListener {
            startActivity(Intent(context, MyThemeActivity::class.java))
        }

        view.findViewById<View>(R.id.tvCreate)?.setOnClickListener {
            startActivity(Intent(context, ThemeBuilderActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }

        viewModel.themes.observe(viewLifecycleOwner) { themes ->
            pbCreate.visibility = View.GONE
            themeAdapter.submitList(themes)
        }

        viewModel.coins.observe(viewLifecycleOwner) { coins ->
            gemView?.setCoins(coins)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ThemeFragment()
    }
}
