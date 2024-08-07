package name.seguri.springacademy.cashcards

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface CashCardRepository :
  CrudRepository<CashCard, Long>, PagingAndSortingRepository<CashCard, Long> {
  fun findByIdAndOwner(id: Long, owner: String): CashCard?

  fun findByOwner(owner: String, pageRequest: PageRequest): Page<CashCard>

  fun existsByIdAndOwner(id: Long, owner: String): Boolean
}
