package com.themes.diy.widgets.keyboard.controlcenter.feature_collections

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.themes.diy.widgets.keyboard.controlcenter.feature_collections.data.CollectionItem
import com.themes.diy.widgets.keyboard.controlcenter.feature_collections.data.CollectionRepository
import com.themes.diy.widgets.keyboard.controlcenter.feature_collections.data.CollectionTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CollectionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CollectionRepository(application)

    private val _tabs = MutableLiveData<List<CollectionTab>>()
    val tabs: LiveData<List<CollectionTab>> = _tabs

    private val _selectedTab = MutableLiveData<CollectionTab>()
    val selectedTab: LiveData<CollectionTab> = _selectedTab

    private val _discoveryItems = MutableLiveData<List<CollectionItem>>()
    val discoveryItems: LiveData<List<CollectionItem>> = _discoveryItems

    private val _downloadedItems = MutableLiveData<List<CollectionItem>>()
    val downloadedItems: LiveData<List<CollectionItem>> = _downloadedItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentTabName: String = "Theme"

    init {
        initTabs()
    }

    private fun initTabs() {
        val tabList = CollectionRepository.TABS.map { name ->
            CollectionTab(
                id = name.lowercase().replace(" ", "_"),
                name = name,
                isSelected = (name.equals("Theme", ignoreCase = true))
            )
        }
        _tabs.value = tabList
        _selectedTab.value = tabList.firstOrNull()
        currentTabName = tabList.firstOrNull()?.name ?: "Theme"
        loadDataForSelectedTab()
    }

    fun selectTab(tab: CollectionTab) {
        Log.d("CollectionDebug", "ViewModel: selectTab '${tab.name}' (id=${tab.id})")
        currentTabName = tab.name
        val updated = _tabs.value?.map {
            it.copy(isSelected = (it.id == tab.id))
        } ?: emptyList()
        _tabs.value = updated
        _selectedTab.value = updated.firstOrNull { it.isSelected } ?: tab
        loadDataForSelectedTab()
    }

    fun selectTabByName(name: String) {
        val tab = _tabs.value?.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: return
        selectTab(tab)
    }

    fun loadDataForSelectedTab() {
        _isLoading.value = true
        Log.d("CollectionDebug", "ViewModel: loadDataForSelectedTab starting for '$currentTabName'")
        viewModelScope.launch {
            val discovery = repository.getDiscoveryItems(currentTabName)
            val downloadedEntities = repository.getDownloadedItems(currentTabName)
            val downloaded = downloadedEntities.map { entity ->
                CollectionItem(
                    id = entity.id,
                    name = entity.name,
                    category = entity.category,
                    targetPath = entity.targetPath,
                    previewPath = entity.previewPath,
                    downloads = entity.downloads,
                    rawType = entity.rawType,
                    extra = entity.extra
                )
            }

            withContext(Dispatchers.Main) {
                Log.d("CollectionDebug", "ViewModel: Posting discoveryItems size=${discovery.size} for '$currentTabName'")
                _discoveryItems.value = discovery
                _downloadedItems.value = downloaded
                _isLoading.value = false
            }
        }
    }
}
