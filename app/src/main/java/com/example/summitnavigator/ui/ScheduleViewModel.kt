// AI-assisted
package com.example.summitnavigator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.summitnavigator.data.SummitRepository
import com.example.summitnavigator.data.model.Session
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers

sealed interface ListUiState {
    object Loading : ListUiState
    object Empty : ListUiState
    data class Success(val sessions: List<Session>) : ListUiState
}

sealed interface RefreshStatus {
    object Idle : RefreshStatus
    object Loading : RefreshStatus
    object Success : RefreshStatus
    data class Error(val exception: Throwable?) : RefreshStatus
}

class ScheduleViewModel(
    private val repository: SummitRepository
) : ViewModel() {

    private val _refreshStatus = MutableStateFlow<RefreshStatus>(RefreshStatus.Idle)
    val refreshStatus: StateFlow<RefreshStatus> = _refreshStatus.asStateFlow()

    val searchQuery = MutableStateFlow("")

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val sessionsFlow = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.fullSchedule
            } else {
                repository.searchSchedule(query)
            }
        }
        .flowOn(Dispatchers.IO)

    val scheduleUiState: StateFlow<ListUiState> = combine(
        sessionsFlow,
        _refreshStatus
    ) { sessions, status ->
        when {
            status is RefreshStatus.Loading -> ListUiState.Loading
            sessions.isEmpty() -> ListUiState.Empty
            else -> ListUiState.Success(sessions)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListUiState.Loading
    )

    val agendaUiState: StateFlow<ListUiState> = combine(
        repository.bookmarkedSchedule,
        _refreshStatus
    ) { sessions, status ->
        when {
            status is RefreshStatus.Loading -> ListUiState.Loading
            sessions.isEmpty() -> ListUiState.Empty
            else -> ListUiState.Success(sessions)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListUiState.Loading
    )


    private val _userRole = MutableStateFlow<String>("attendee")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    init {
        fetchUserRoleAndBookmarks()
    }

    private fun fetchUserRoleAndBookmarks() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                _userRole.value = repository.getUserRole(user.uid)
                repository.syncUserBookmarks(user.uid)
            }
        }
    }

    fun refreshSchedule() {
        viewModelScope.launch {
            _refreshStatus.value = RefreshStatus.Loading
            // Just refresh user role and bookmarks as sync is automatic
            fetchUserRoleAndBookmarks()
            _refreshStatus.value = RefreshStatus.Success
        }
    }
    
    fun toggleBookmark(sessionId: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                repository.toggleBookmark(sessionId, user.uid, isBookmarked)
            }
        }
    }
}

class ScheduleViewModelFactory(
    private val repository: SummitRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
