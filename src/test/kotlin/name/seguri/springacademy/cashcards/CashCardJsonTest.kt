package name.seguri.springacademy.cashcards

import java.io.IOException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester

@JsonTest
class CashCardJsonTest {

  @Autowired private lateinit var json: JacksonTester<CashCard>

  @Autowired private lateinit var jsonList: JacksonTester<Array<CashCard>>

  private lateinit var cashCards: Array<CashCard>

  @BeforeEach
  fun setUp() {
    cashCards =
      arrayOf(
        CashCard(99L, 123.45, "sarah1"),
        CashCard(100L, 1.00, "sarah1"),
        CashCard(101L, 150.00, "sarah1"),
      )
  }

  @Test
  @Throws(IOException::class)
  fun cashCardSerializationTest() {
    val cashCard = cashCards[0]
    assertThat(json.write(cashCard)).isStrictlyEqualToJson("single.json")
    assertThat(json.write(cashCard))
      .hasJsonPathNumberValue("@.id")
      .extractingJsonPathNumberValue("@.id")
      .isEqualTo(99)
    assertThat(json.write(cashCard))
      .hasJsonPathNumberValue("@.amount")
      .extractingJsonPathNumberValue("@.amount")
      .isEqualTo(123.45)
  }

  @Test
  @Throws(IOException::class)
  fun cashCardDeserializationTest() {
    val expected =
      """
      {
        "id": 99,
        "amount": 123.45, 
        "owner": "sarah1"
      }
      """
    assertThat(json.parse(expected)).isEqualTo(CashCard(99L, 123.45, "sarah1"))
    assertThat(json.parseObject(expected).id).isEqualTo(99L)
    assertThat(json.parseObject(expected).amount).isEqualTo(123.45)
  }

  @Test
  @Throws(IOException::class)
  fun cashCardListSerializationTest() {
    assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json")
  }

  @Test
  @Throws(IOException::class)
  fun cashCardListDeserializationTest() {
    val expected =
      """
      [
        {"id": 99, "amount": 123.45 , "owner": "sarah1"},
        {"id": 100, "amount": 1.00 , "owner": "sarah1"},
        {"id": 101, "amount": 150.00, "owner": "sarah1"}
      ]
      """
    assertThat(jsonList.parse(expected)).isEqualTo(cashCards)
  }
}
