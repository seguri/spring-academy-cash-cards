package name.seguri.springacademy.cashcards

import org.springframework.data.annotation.Id

data class CashCard(@Id val id: Long?, val amount: Double, val owner: String?)
