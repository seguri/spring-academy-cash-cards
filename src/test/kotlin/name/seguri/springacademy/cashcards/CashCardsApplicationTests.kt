package name.seguri.springacademy.cashcards

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import java.net.URI
import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardsApplicationTests {

  @Autowired lateinit var restTemplate: TestRestTemplate

  @Test
  fun shouldReturnACashCardWhenDataIsSaved() {
    val response =
      restTemplate
        .withBasicAuth("sarah1", "abc123")
        .getForEntity("/cashcards/99", String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

    val documentContext: DocumentContext = JsonPath.parse(response.body)
    val id: Number = documentContext.read("$.id")
    assertThat(id).isEqualTo(99)
    val amount: Double = documentContext.read("$.amount")
    assertThat(amount).isEqualTo(123.45)
  }

  @Test
  fun shouldNotReturnACashCardWithAnUnknownId() {
    val response =
      restTemplate
        .withBasicAuth("sarah1", "abc123")
        .getForEntity("/cashcards/1000", String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    assertThat(response.body).isBlank()
  }

  @Test
  @DirtiesContext
  fun shouldCreateANewCashCard() {
    val newCashCard = CashCard(null, 250.00, null)
    val createResponse =
      restTemplate
        .withBasicAuth("sarah1", "abc123")
        .postForEntity("/cashcards", newCashCard, Void::class.java)
    assertThat(createResponse.statusCode).isEqualTo(HttpStatus.CREATED)

    val locationOfNewCashCard: URI? = createResponse.headers.location
    val getResponse =
      restTemplate
        .withBasicAuth("sarah1", "abc123")
        .getForEntity(locationOfNewCashCard, String::class.java)
    assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)

    val documentContext: DocumentContext = JsonPath.parse(getResponse.body)
    val id: Number = documentContext.read("$.id")
    val amount: Double = documentContext.read("$.amount")
    assertThat(id).isNotNull
    assertThat(amount).isEqualTo(250.00)
  }

  @Test
  fun shouldReturnAllCashCardsWhenListIsRequested() {
    val response =
      restTemplate.withBasicAuth("sarah1", "abc123").getForEntity("/cashcards", String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

    val documentContext: DocumentContext = JsonPath.parse(response.body)
    val cashCardCount: Int = documentContext.read("$.length()")
    assertThat(cashCardCount).isEqualTo(3)
    val ids: JSONArray = documentContext.read("$..id")
    assertThat(ids).containsExactlyInAnyOrder(99, 100, 101)
    val amounts: JSONArray = documentContext.read("$..amount")
    assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.00, 150.00)
  }

  @Test
  fun shouldReturnAPageOfCashCards() {
    val response =
      restTemplate
        .withBasicAuth("sarah1", "abc123")
        .getForEntity("/cashcards?page=0&size=1", String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

    val documentContext: DocumentContext = JsonPath.parse(response.body)
    val page: JSONArray = documentContext.read("$[*]")
    assertThat(page.size).isEqualTo(1)
  }

  @Test
  fun shouldReturnASortedPageOfCashCards() {
    val response =
      restTemplate
        .withBasicAuth("sarah1", "abc123")
        .getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

    val documentContext: DocumentContext = JsonPath.parse(response.body)
    val read: JSONArray = documentContext.read("$[*]")
    assertThat(read.size).isEqualTo(1)
    val amount: Double = documentContext.read("$[0].amount")
    assertThat(amount).isEqualTo(150.00)
  }

  @Test
  fun shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
    val response =
      restTemplate.withBasicAuth("sarah1", "abc123").getForEntity("/cashcards", String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

    val documentContext: DocumentContext = JsonPath.parse(response.body)
    val page: JSONArray = documentContext.read("$[*]")
    assertThat(page.size).isEqualTo(3)
    val amounts: JSONArray = documentContext.read("$..amount")
    assertThat(amounts).containsExactly(1.00, 123.45, 150.00)
  }

  @Test
  fun shouldNotReturnACashCardWhenUsingBadCredentials() {
    var response =
      restTemplate
        .withBasicAuth("BAD-USER", "abc123")
        .getForEntity("/cashcards/99", String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)

    response =
      restTemplate
        .withBasicAuth("sarah1", "BAD-PASSWORD")
        .getForEntity("/cashcards/99", String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
  }

  @Test
  fun shouldRejectUsersWhoAreNotCardOwners() {
    val response =
      restTemplate
        .withBasicAuth("hank-owns-no-cards", "qrs456")
        .getForEntity("/cashcards/99", String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
  }

  @Test
  fun shouldNotAllowAccessToCashCardsTheyDoNotOwn() {
    val response =
      restTemplate
        .withBasicAuth("sarah1", "abc123")
        .getForEntity("/cashcards/102", String::class.java) // kumar2's data
    assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
  }

  @Test
  @DirtiesContext
  fun shouldUpdateAnExistingCashCard() {
    val cashCardUpdate = CashCard(null, 19.99, null)
    val request = HttpEntity(cashCardUpdate)
    val response =
      restTemplate
        .withBasicAuth("sarah1", "abc123")
        .exchange("/cashcards/99", HttpMethod.PUT, request, Void::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

    val getResponse =
      restTemplate
        .withBasicAuth("sarah1", "abc123")
        .getForEntity("/cashcards/99", String::class.java)
    assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
    val documentContext: DocumentContext = JsonPath.parse(getResponse.body)
    val id: Number = documentContext.read("$.id")
    val amount: Double = documentContext.read("$.amount")
    assertThat(id).isEqualTo(99)
    assertThat(amount).isEqualTo(19.99)
  }

  @Test
  fun shouldNotUpdateACashCardThatDoesNotExist() {
    val unknownCard = CashCard(null, 19.99, null)
    val request = HttpEntity(unknownCard)
    val response =
      restTemplate
        .withBasicAuth("sarah1", "abc123")
        .exchange("/cashcards/99999", HttpMethod.PUT, request, Void::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
  }

  @Test
  fun shouldNotUpdateACashCardThatIsOwnedBySomeoneElse() {
    val kumarsCard = CashCard(null, 333.33, null)
    val request = HttpEntity(kumarsCard)
    val response =
      restTemplate
        .withBasicAuth("sarah1", "abc123")
        .exchange("/cashcards/102", HttpMethod.PUT, request, Void::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
  }

  @Test
  @DirtiesContext
  fun shouldDeleteAnExistingCashCard() {
    val response =
      restTemplate
        .withBasicAuth("sarah1", "abc123")
        .exchange("/cashcards/99", HttpMethod.DELETE, null, Void::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

    val getResponse =
      restTemplate
        .withBasicAuth("sarah1", "abc123")
        .getForEntity("/cashcards/99", String::class.java)
    assertThat(getResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
  }

  @Test
  fun shouldNotDeleteACashCardThatDoesNotExist() {
    val deleteResponse =
      restTemplate
        .withBasicAuth("sarah1", "abc123")
        .exchange("/cashcards/99999", HttpMethod.DELETE, null, Void::class.java)
    assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
  }

  @Test
  fun shouldNotAllowDeletionOfCashCardsTheyDoNotOwn() {
    val deleteResponse =
      restTemplate
        .withBasicAuth("sarah1", "abc123")
        .exchange("/cashcards/102", HttpMethod.DELETE, null, Void::class.java)
    assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND)

    val getResponse =
      restTemplate
        .withBasicAuth("kumar2", "xyz789")
        .getForEntity("/cashcards/102", String::class.java)
    assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
  }
}
