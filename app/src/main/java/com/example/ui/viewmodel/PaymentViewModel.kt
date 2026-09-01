package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    val transactions: StateFlow<List<TransactionEntity>> = paymentRepository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _paymentResult = MutableStateFlow<String?>(null)
    val paymentResult: StateFlow<String?> = _paymentResult.asStateFlow()

    fun sendMoney(receiverId: String, amount: Double, note: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = paymentRepository.sendMoney(receiverId, amount, note)
            result.onSuccess {
                _paymentResult.value = "Sent $${String.format("%.2f", amount)} successfully!"
                onSuccess()
            }.onFailure {
                _paymentResult.value = it.message ?: "Transaction failed"
            }
        }
    }

    fun addFunds(amount: Double) {
        viewModelScope.launch {
            paymentRepository.addFunds(amount)
            _paymentResult.value = "Added $${String.format("%.2f", amount)} to your balance!"
        }
    }

    fun clearResult() {
        _paymentResult.value = null
    }
}

class PaymentViewModelFactory(private val repository: PaymentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaymentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
