package name.seguri.springacademy.cashcards

import java.net.URI
import java.security.Principal
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("/cashcards")
class CashCardController(private val cashCardRepository: CashCardRepository) {

  @GetMapping("/{requestedId}")
  private fun findById(
    @PathVariable requestedId: Long,
    principal: Principal,
  ): ResponseEntity<CashCard> {
    val cashCard = findCashCard(requestedId, principal)
    return if (cashCard != null) {
      ResponseEntity.ok(cashCard)
    } else {
      ResponseEntity.notFound().build()
    }
  }

  @PostMapping
  private fun createCashCard(
    @RequestBody newCashCardRequest: CashCard,
    ucb: UriComponentsBuilder,
    principal: Principal,
  ): ResponseEntity<Void> {
    val cashCardWithOwner = CashCard(null, newCashCardRequest.amount, principal.name)
    val savedCashCard = cashCardRepository.save(cashCardWithOwner)
    val locationOfNewCashCard: URI =
      ucb.path("cashcards/{id}").buildAndExpand(savedCashCard.id).toUri()
    return ResponseEntity.created(locationOfNewCashCard).build()
  }

  @GetMapping
  private fun findAll(pageable: Pageable, principal: Principal): ResponseEntity<List<CashCard>> {
    val page: Page<CashCard> =
      cashCardRepository.findByOwner(
        principal.name,
        PageRequest.of(
          pageable.pageNumber,
          pageable.pageSize,
          pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount")),
        ),
      )
    return ResponseEntity.ok(page.content)
  }

  @PutMapping("/{requestedId}")
  private fun putCashCard(
    @PathVariable requestedId: Long,
    @RequestBody cashCardUpdate: CashCard,
    principal: Principal,
  ): ResponseEntity<Void> {
    val cashCard = findCashCard(requestedId, principal)
    return if (cashCard != null) {
      val updatedCashCard = CashCard(requestedId, cashCardUpdate.amount, principal.name)
      cashCardRepository.save(updatedCashCard)
      ResponseEntity.noContent().build()
    } else {
      ResponseEntity.notFound().build()
    }
  }

  @DeleteMapping("/{id}")
  private fun deleteCashCard(@PathVariable id: Long, principal: Principal): ResponseEntity<Void> {
    return if (cashCardRepository.existsByIdAndOwner(id, principal.name)) {
      cashCardRepository.deleteById(id)
      ResponseEntity.noContent().build()
    } else {
      ResponseEntity.notFound().build()
    }
  }

  private fun findCashCard(requestedId: Long, principal: Principal): CashCard? {
    return cashCardRepository.findByIdAndOwner(requestedId, principal.name)
  }
}
