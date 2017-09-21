package playjson
import org.scalatest._
import play.api.libs.json.Json

/**
  * Created by 97306p on 21/09/2017.
  */

case class Other(name: String)

object Other {
  implicit val reads = Json.reads[Other]

}

case class MonPojo(toto: String, tata: Int, other: Other)

class RulesSpec extends FlatSpec with Matchers {


  "Json rules" should "" in {
    import play.api.libs.json._
    import play.api.libs.json.Reads._
    import play.api.libs.functional.syntax._
    import shapeless._
    import syntax.singleton._
    import playjson.rules._

    val value: Reads[MonPojo] = jsonRead[MonPojo].readsWithRules(
      'tata ->> read(min(0) keepAnd max(150))
    )

    val value1: JsResult[MonPojo] = value.reads(Json.obj(
      "toto" -> "test",
      "other" -> Json.obj("name" -> "test"),
      "tata" -> 2
    ))

    println(s"!!! ${
      value1
    }")
  }

}
