package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.StoryEntity
import com.example.data.model.StoryPrivacy
import com.example.data.repository.StoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StoryViewModel(
    private val storyRepository: StoryRepository
) : ViewModel() {

    val stories: StateFlow<List<StoryEntity>> = storyRepository.getActiveStories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createTextStory(text: String, colorHex: String, privacy: StoryPrivacy = StoryPrivacy.EVERYONE, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            if (text.isNotBlank()) {
                storyRepository.createTextStory(text, colorHex, privacy)
                onDone()
            }
        }
    }

    fun createMediaStory(mediaUrl: String, caption: String = "", privacy: StoryPrivacy = StoryPrivacy.EVERYONE, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            if (mediaUrl.isNotBlank()) {
                storyRepository.createMediaStory(mediaUrl, caption, privacy)
                onDone()
            }
        }
    }

    fun markStoryViewed(storyId: String) {
        viewModelScope.launch {
            storyRepository.markStoryAsViewed(storyId)
        }
    }

    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            storyRepository.deleteStory(storyId)
        }
    }
}

class StoryViewModelFactory(private val repository: StoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
