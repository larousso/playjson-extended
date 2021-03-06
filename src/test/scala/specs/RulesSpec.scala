package specs

import org.scalatest._
import play.api.libs.json.Json


case class Village(name: String)

object Village {
  implicit val reads = Json.reads[Village]
}

case class Viking(name: String, surname: String, weight: Int, village: Seq[Village])

class RulesSpec extends WordSpec with MustMatchers with OptionValues {


  "Json Rules" must {
    "a rule can be added" in {
      import play.api.libs.json._
      import play.api.libs.json.Reads._
      import play.api.libs.functional.syntax._
      import shapeless._
      import syntax.singleton._
      import playjson.rules._

      implicit val reads: Reads[Viking] = jsonRead[Viking].withRules(
        'weight ->> read(min(0) keepAnd max(150))
      )

      val jsResult: JsResult[Viking] = Json.obj(
        "name" -> "Ragnar",
        "surname" -> "Lothbrok",
        "village" -> Seq(Json.obj("name" -> "Kattegat")),
        "weight" -> 90
      ).validate[Viking]

      println(s"!!! ${
        jsResult
      }")

      jsResult mustBe an[JsSuccess[_]]
    }

    "3 rules can be added" in {
      import play.api.libs.json._
      import play.api.libs.json.Reads._
      import play.api.libs.functional.syntax._
      import shapeless._
      import syntax.singleton._
      import playjson.rules._

      implicit val reads: Reads[Viking] = jsonRead[Viking].withRules(
          'weight ->> read(min(0) keepAnd max(150)) and
          'name ->> read(pattern(".*".r)) and
          'surname ->> read(pattern(".*".r))
      )

      val jsResult: JsResult[Viking] = Json.obj(
        "name" -> "Ragnar",
        "surname" -> "Lothbrok",
        "village" -> Seq(Json.obj("name" -> "Kattegat")),
        "weight" -> 2
      ).validate[Viking]

      println(s"!!! $jsResult")

      jsResult mustBe an[JsSuccess[_]]
    }

    "transformation and rules" in {

      import play.api.libs.json._
      import play.api.libs.json.Reads._
      import play.api.libs.functional.syntax._
      import shapeless._
      import syntax.singleton._
      import playjson.all._

      implicit val reads: Reads[Viking] =
        transform(
          ((__ \ 'theName) to (__ \ 'name)) and
          ((__ \ 'theSurname) to (__ \ 'surname)) and
          ((__ \ 'theVillage) to (__ \ 'village))
        ) andThen jsonRead[Viking].withRules(
          'weight ->> read(min(0) keepAnd max(150)) and
          'name ->> read(pattern(".*".r)) and
          'surname ->> read(pattern(".*".r))
        )

      val jsResult: JsResult[Viking] = Json.obj(
        "theName" -> "Ragnar",
        "theSurname" -> "Lothbrok",
        "theVillage" -> Seq(Json.obj("name" -> "Kattegat")),
        "weight" -> 2
      ).validate[Viking]

      println(s"!!! $jsResult")

      jsResult mustBe an[JsSuccess[_]]
    }


    "transformation with native reads" in {

      import play.api.libs.json._
      import playjson.transformation._

      implicit val reads: Reads[Viking] =
        transform(
          ((__ \ 'theName) to (__ \ 'name)) and
          ((__ \ 'theSurname) to (__ \ 'surname)) and
          ((__ \ 'theVillage) to (__ \ 'village))
        ) andThen Json.reads[Viking]

      val jsResult: JsResult[Viking] = Json.obj(
        "theName" -> "Ragnar",
        "theSurname" -> "Lothbrok",
        "theVillage" -> Seq(Json.obj("name" -> "Kattegat")),
        "weight" -> 2
      ).validate[Viking]

      println(s"!!! $jsResult")

      jsResult mustBe an[JsSuccess[_]]
    }


    "hReads" in {

      import play.api.libs.json._
      import playjson.reads._

      implicit val reads: Reads[Viking] = hReads[Viking]

      val jsResult: JsResult[Viking] = Json.obj(
        "name" -> "Ragnar",
        "surname" -> "Lothbrok",
        "village" -> Seq(Json.obj("name" -> "Kattegat")),
        "weight" -> 2
      ).validate[Viking]

      println(s"!!! $jsResult")

      jsResult mustBe an[JsSuccess[_]]
    }


    "transformation and hReads" in {

      import play.api.libs.json._
      import playjson.all._

      implicit val reads: Reads[Viking] =
        transform(
          ((__ \ 'theName) to (__ \ 'name)) and
          ((__ \ 'theSurname) to (__ \ 'surname)) and
          ((__ \ 'theVillage) to (__ \ 'village))
        ) andThen hReads[Viking]

      val jsResult: JsResult[Viking] = Json.obj(
        "theName" -> "Ragnar",
        "theSurname" -> "Lothbrok",
        "theVillage" -> Seq(Json.obj("name" -> "Kattegat")),
        "weight" -> 2
      ).validate[Viking]

      println(s"!!! $jsResult")

      jsResult mustBe an[JsSuccess[_]]
    }

  }

}
