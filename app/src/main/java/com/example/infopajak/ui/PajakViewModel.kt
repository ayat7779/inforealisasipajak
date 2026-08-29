package com.example.infopajak.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.infopajak.data.PajakData
import com.example.infopajak.data.PajakRequest
import com.example.infopajak.network.PajakApiService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

sealed class PajakUiState {
    object Loading : PajakUiState()
    data class Success(val data: PajakData) : PajakUiState()
    data class Error(val message: String) : PajakUiState()
}

data class TriwulanData(
    val label: String,
    val realization: Long,
    val target: String?
)

data class TrendData(
    val label: String,
    val value: Long
)

class PajakViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<PajakUiState>(PajakUiState.Loading)
    val uiState: StateFlow<PajakUiState> = _uiState.asStateFlow()

    private val _startDate = MutableStateFlow("")
    val startDate: StateFlow<String> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow("")
    val endDate: StateFlow<String> = _endDate.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _triwulanState = MutableStateFlow<List<TriwulanData>>(emptyList())
    val triwulanState: StateFlow<List<TriwulanData>> = _triwulanState.asStateFlow()

    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading.asStateFlow()

    // Analytic States
    private val _activeTab = MutableStateFlow(0) // 0: Overview, 1: Analytics
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _dailyTrend = MutableStateFlow<List<TrendData>>(emptyList())
    val dailyTrend: StateFlow<List<TrendData>> = _dailyTrend.asStateFlow()

    private val _monthlyTrend = MutableStateFlow<List<TrendData>>(emptyList())
    val monthlyTrend: StateFlow<List<TrendData>> = _monthlyTrend.asStateFlow()

    private val _isDailyLoading = MutableStateFlow(false)
    val isDailyLoading: StateFlow<Boolean> = _isDailyLoading.asStateFlow()

    private val _isMonthlyLoading = MutableStateFlow(false)
    val isMonthlyLoading: StateFlow<Boolean> = _isMonthlyLoading.asStateFlow()

    private val _analyticError = MutableStateFlow<String?>(null)
    val analyticError: StateFlow<String?> = _analyticError.asStateFlow()

    private val _selectedTrendMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedTrendMonth: StateFlow<Int> = _selectedTrendMonth.asStateFlow()

    private val _selectedTrendYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedTrendYear: StateFlow<Int> = _selectedTrendYear.asStateFlow()

    private val _selectedAnalyticCategory = MutableStateFlow<String?>(null) // null means "Total"
    val selectedAnalyticCategory: StateFlow<String?> = _selectedAnalyticCategory.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val apiService: PajakApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://apps1.bapenda.riau.go.id/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PajakApiService::class.java)
    }

    init {
        // Default to this year until today
        val calendar = Calendar.getInstance()
        val today = dateFormat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        val startOfYear = dateFormat.format(calendar.time)
        
        _startDate.value = startOfYear
        _endDate.value = today
        fetchData(startOfYear, today)
        
        // Initial analytic fetch
        fetchAnalyticTrend()
    }

    fun setActiveTab(index: Int) {
        _activeTab.value = index
    }

    fun selectTrendMonth(month: Int) {
        _selectedTrendMonth.value = month
        fetchDailyTrend(month, _selectedTrendYear.value)
    }

    fun selectTrendYear(year: Int) {
        _selectedTrendYear.value = year
        fetchMonthlyTrend(year)
        fetchDailyTrend(_selectedTrendMonth.value, year)
    }

    fun selectAnalyticCategory(category: String?) {
        _selectedAnalyticCategory.value = category
        fetchAnalyticTrend()
    }

    private fun fetchAnalyticTrend() {
        fetchDailyTrend(_selectedTrendMonth.value, _selectedTrendYear.value)
        fetchMonthlyTrend(_selectedTrendYear.value)
    }

    private fun getApiKeyForYear(year: Int): String = "bpd${year}av2#"

    private var fetchDailyTrendJob: Job? = null
    private var fetchMonthlyTrendJob: Job? = null

    private fun fetchDailyTrend(month: Int, year: Int) {
        fetchDailyTrendJob?.cancel()
        fetchDailyTrendJob = viewModelScope.launch {
            _isDailyLoading.value = true
            _analyticError.value = null
            _dailyTrend.value = emptyList()
            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                
                val results = (1..daysInMonth).map { day ->
                    async {
                        try {
                            val dateStr = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day)
                            val request = PajakRequest(getApiKeyForYear(year), dateStr, dateStr)
                            val response = apiService.getPajakData(request)
                            val total = when (_selectedAnalyticCategory.value) {
                                "PKB" -> response.data?.pkb ?: 0L
                                "BBNKB" -> response.data?.bbnkb ?: 0L
                                "PAP" -> response.data?.ap ?: 0L
                                "PBBKB" -> response.data?.pbbkb?.toLongOrNull() ?: 0L
                                "Pajak Rokok" -> response.data?.pajakRokok?.toLongOrNull() ?: 0L
                                "AB" -> response.data?.ab ?: 0L
                                else -> {
                                    (response.data?.pkb ?: 0L) +
                                    (response.data?.bbnkb ?: 0L) +
                                    (response.data?.ap ?: 0L) +
                                    (response.data?.pbbkb?.toLongOrNull() ?: 0L) +
                                    (response.data?.pajakRokok?.toLongOrNull() ?: 0L) +
                                    (response.data?.ab ?: 0L)
                                }
                            }
                            TrendData(day.toString(), total)
                        } catch (e: Exception) {
                            TrendData(day.toString(), 0L)
                        }
                    }
                }.awaitAll()
                
                if (isActive) {
                    _dailyTrend.value = results
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _analyticError.value = "Gagal memuat tren harian: ${e.message}"
                }
            } finally {
                _isDailyLoading.value = false
            }
        }
    }

    private fun fetchMonthlyTrend(year: Int) {
        fetchMonthlyTrendJob?.cancel()
        fetchMonthlyTrendJob = viewModelScope.launch {
            _isMonthlyLoading.value = true
            _analyticError.value = null
            _monthlyTrend.value = emptyList()
            try {
                val monthsList = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
                val results = (0..11).map { monthIdx ->
                    async {
                        try {
                            val calendar = Calendar.getInstance()
                            calendar.set(Calendar.YEAR, year)
                            calendar.set(Calendar.MONTH, monthIdx)
                            calendar.set(Calendar.DAY_OF_MONTH, 1)
                            val lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                            
                            val start = String.format(Locale.getDefault(), "%d-%02d-01", year, monthIdx + 1)
                            val end = String.format(Locale.getDefault(), "%d-%02d-%02d", year, monthIdx + 1, lastDay)
                            
                            val request = PajakRequest(getApiKeyForYear(year), start, end)
                            val response = apiService.getPajakData(request)
                            val total = when (_selectedAnalyticCategory.value) {
                                "PKB" -> response.data?.pkb ?: 0L
                                "BBNKB" -> response.data?.bbnkb ?: 0L
                                "PAP" -> response.data?.ap ?: 0L
                                "PBBKB" -> response.data?.pbbkb?.toLongOrNull() ?: 0L
                                "Pajak Rokok" -> response.data?.pajakRokok?.toLongOrNull() ?: 0L
                                "AB" -> response.data?.ab ?: 0L
                                else -> {
                                    (response.data?.pkb ?: 0L) +
                                    (response.data?.bbnkb ?: 0L) +
                                    (response.data?.ap ?: 0L) +
                                    (response.data?.pbbkb?.toLongOrNull() ?: 0L) +
                                    (response.data?.pajakRokok?.toLongOrNull() ?: 0L) +
                                    (response.data?.ab ?: 0L)
                                }
                            }
                            TrendData(monthsList[monthIdx], total)
                        } catch (e: Exception) {
                            TrendData(monthsList[monthIdx], 0L)
                        }
                    }
                }.awaitAll()
                
                if (isActive) {
                    _monthlyTrend.value = results
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _analyticError.value = "Gagal memuat tren bulanan: ${e.message}"
                }
            } finally {
                _isMonthlyLoading.value = false
            }
        }
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
        if (category != null) {
            fetchTriwulanData(category)
        } else {
            _triwulanState.value = emptyList()
        }
    }

    private fun fetchTriwulanData(category: String) {
        val selectedYear = try { _startDate.value.split("-")[0] } catch (e: Exception) { Calendar.getInstance().get(Calendar.YEAR).toString() }
        val yearInt = selectedYear.toInt()
        
        val quarters = listOf(
            Triple("TW-1", "$selectedYear-01-01", "$selectedYear-03-31"),
            Triple("TW-2", "$selectedYear-04-01", "$selectedYear-06-30"),
            Triple("TW-3", "$selectedYear-07-01", "$selectedYear-09-30"),
            Triple("TW-4", "$selectedYear-10-01", "$selectedYear-12-31")
        )

        viewModelScope.launch {
            _isDetailLoading.value = true
            try {
                val results = quarters.map { (label, start, end) ->
                    async {
                        try {
                            val request = PajakRequest(getApiKeyForYear(yearInt), start, end)
                            val response = apiService.getPajakData(request)
                            val realization = when (category) {
                                "PKB" -> response.data?.pkb ?: 0L
                                "BBNKB" -> response.data?.bbnkb ?: 0L
                                "PAP" -> response.data?.ap ?: 0L
                                "PBBKB" -> response.data?.pbbkb?.toLongOrNull() ?: 0L
                                "Pajak Rokok" -> response.data?.pajakRokok?.toLongOrNull() ?: 0L
                                "AB" -> response.data?.ab ?: 0L
                                else -> 0L
                            }
                            val target = when (category) {
                                "PKB" -> response.data?.targetPkb
                                "BBNKB" -> response.data?.targetBbnkb
                                "PAP" -> response.data?.targetAp
                                "PBBKB" -> response.data?.targetPbbkb
                                "Pajak Rokok" -> response.data?.targetPajakRokok
                                "AB" -> response.data?.targetAb
                                else -> null
                            }
                            TriwulanData(label, realization, target)
                        } catch (e: Exception) {
                            TriwulanData(label, 0L, null)
                        }
                    }
                }.awaitAll()
                
                _triwulanState.value = results
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isDetailLoading.value = false
            }
        }
    }

    fun setDateRangeAndFetch(start: String, end: String) {
        _startDate.value = start
        _endDate.value = end
        fetchData(start, end)
    }

    fun fetchData(start: String = _startDate.value, end: String = _endDate.value) {
        viewModelScope.launch {
            _uiState.value = PajakUiState.Loading
            try {
                val year = try { start.split("-")[0].toInt() } catch (e: Exception) { Calendar.getInstance().get(Calendar.YEAR) }
                val request = PajakRequest(
                    apiKey = getApiKeyForYear(year),
                    tglAwal = start,
                    tglAkhir = end
                )
                val response = apiService.getPajakData(request)
                if (response.status == 200 && response.data != null) {
                    _uiState.value = PajakUiState.Success(response.data)
                } else {
                    _uiState.value = PajakUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = PajakUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
    
    fun getTodayDate(): String = dateFormat.format(Date())
    
    fun getStartOfMonthDate(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return dateFormat.format(calendar.time)
    }

    fun getStartOfYearDate(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        return dateFormat.format(calendar.time)
    }
}
