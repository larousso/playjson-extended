package playjson

object transformation {

  import play.api.libs.functional._
  import play.api.libs.functional.syntax._
  import play.api.libs.json.Reads._
  import play.api.libs.json._

  case class Transformation(from: JsPath, to: JsPath) {
    def reads =
      __.json.update(to.json.copyFrom(from.json.pick))

    def and(transformation: Transformation): Transformations = Transformations(Seq(this, transformation))
  }

  case class Transformations(transformations: Seq[Transformation]) {

    def and(transformation: Transformation): Transformations = this.copy(
      transformations = transformation +: transformations
    )

    def reads: Reads[JsObject] = {
      val reduce = transformations.map(_.reads).reduceLeft(combine)
      __.json.update(reduce)
    }

    private def combine(r1: Reads[JsObject], r2: Reads[JsObject]): Reads[JsObject] = {
      val b: FunctionalBuilder[Reads]#CanBuild2[JsObject, JsObject] = r1 and r2
      b.reduce
    }
  }

  implicit class TransformationOps(from: JsPath) {
    def to(t: JsPath): Transformation =
      Transformation(from, t)
  }

  def transform(transformations: Transformations): Reads[JsObject] = transformations.reads
  def transform(transformation: Transformation): Reads[JsObject] = Transformations(Seq(transformation)).reads

}


object rules {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._
  import shapeless.labelled._
  import shapeless.ops.record.{LacksKey, Selector}
  import shapeless.syntax.singleton._
  import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

  import scala.annotation.implicitNotFound

  implicit class HListOps[Repr <: HList](l: Repr) {
    def and[V](value: V): V :: Repr = value :: l
  }

  implicit class GenericOps[R](l: R) {
    def and[V](value: V): V :: R :: HNil = value :: l :: HNil
  }

  @implicitNotFound("Implicit not found: Rules type or fields are not valid")
  trait RuleValidation[Repr <: HList, Rules <: HList]

  object RuleValidation {

    implicit def validateHNil[Repr <: HList]: RuleValidation[Repr, HNil] =
      new RuleValidation[Repr, HNil] {}

    implicit def validateSingleton[Repr <: HList, K <: Symbol, V](
      implicit sel: Selector.Aux[Repr, K, V]
    ): RuleValidation[Repr, FieldType[K, Rule[V]] :: HNil] =
      new RuleValidation[Repr, FieldType[K, Rule[V]] :: HNil] {}

    implicit def validateHCons[Repr <: HList, H, R <: HList, K <: Symbol, V](
      implicit sel: Selector.Aux[Repr, K, V],
      validation: RuleValidation[Repr, R]
    ): RuleValidation[Repr, FieldType[K, Rule[V]] :: R] =
      new RuleValidation[Repr, FieldType[K, Rule[V]] :: R] {}
  }

  sealed trait Rule[T]
  case class ReadRule[T](reads: Reads[T])   extends Rule[T]
  case class OrElseRule[T](reads: Reads[T]) extends Rule[T]

  trait ReadsWithRules[T, R <: HList] {
    def withRules(rules: R): Reads[T]
  }

  trait ReadsWithRulesLowerPriority {
    implicit def readsNoRule[T](implicit reads: Reads[T]): ReadsWithRules[T, HNil] = new ReadsWithRules[T, HNil] {
      override def withRules(rules: HNil): Reads[T] = reads
    }

    implicit def readsGeneric[Repr, A, R <: HList](implicit gen: LabelledGeneric.Aux[A, Repr],
                                                   readsRepr: Lazy[ReadsWithRules[Repr, R]]): ReadsWithRules[A, R] =
      new ReadsWithRules[A, R] {
        override def withRules(rules: R): Reads[A] =
          readsRepr.value.withRules(rules).map(r => gen.from(r))
      }
  }

  object ReadsWithRules extends ReadsWithRulesLowerPriority {

    implicit def readHNil[R <: HList]: ReadsWithRules[HNil, R] = new ReadsWithRules[HNil, R] {
      override def withRules(rules: R): Reads[HNil] = Reads[HNil] { json =>
        JsSuccess(HNil)
      }
    }

    implicit def readNoRuleForHead[K <: Symbol, H, T <: HList, R <: HList](
                                                                            implicit witness: Witness.Aux[K],
                                                                            noRule: LacksKey[R, K],
                                                                            readsH: Reads[H],
                                                                            readsT: ReadsWithRules[T, R]
                                                                          ): ReadsWithRules[FieldType[K, H] :: T, R] =
      new ReadsWithRules[FieldType[K, H] :: T, R] {
        override def withRules(rules: R): Reads[FieldType[K, H] :: T] = {
          val name = witness.value.name
          val rH   = (__ \ name).read(readsH)
          (rH and readsT.withRules(rules))((a, b) => (name ->> a :: b).asInstanceOf[FieldType[K, H] :: T])
        }
      }

    implicit def readRuleForHead[K <: Symbol, H, T <: HList, R <: HList](
      implicit witness: Witness.Aux[K],
      readsH: Reads[H],
      at: Selector.Aux[R, K, Rule[H]],
      readsT: ReadsWithRules[T, R]
    ): ReadsWithRules[FieldType[K, H] :: T, R] =
      new ReadsWithRules[FieldType[K, H] :: T, R] {
        override def withRules(rules: R): Reads[FieldType[K, H] :: T] = {
          val name                    = witness.value.name
          val additionalRule: Rule[H] = at(rules)
          val rH = additionalRule match {
            case ReadRule(reads)   => (__ \ name).read[H](reads)
            case OrElseRule(reads) => (__ \ name).read[H](readsH).orElse(reads)
          }
          (rH and readsT.withRules(rules))((a, b) => (name ->> a :: b).asInstanceOf[FieldType[K, H] :: T])
        }
      }

  }

  case class JsonRead[A]() {

    def withRules[R <: HList, ARepr <: HList](rules: R)(
      implicit readWithRule: ReadsWithRules[A, R],
      genA: LabelledGeneric.Aux[A, ARepr],
      validation: RuleValidation[ARepr, R]
    ): Reads[A] =
      readWithRule.withRules(rules)


    def withRules[R, ARepr <: HList](rules: R)(
      implicit readWithRule: ReadsWithRules[A, R :: HNil],
      genA: LabelledGeneric.Aux[A, ARepr],
      validation: RuleValidation[ARepr, R :: HNil]
    ): Reads[A] =
      readWithRule.withRules(rules :: HNil)
  }

  def jsonRead[T]: JsonRead[T] = JsonRead[T]()

  def read[T](implicit reads: Reads[T]): Rule[T] = ReadRule(reads)

  def orElse[T](or: Reads[T]): Rule[T] = OrElseRule(or)
  def orElse[T](or: T): Rule[T] = OrElseRule(Reads.pure(or))

}

object reads {
  import play.api.libs.functional.syntax._
  import play.api.libs.json._
  import shapeless.labelled._
  import shapeless.syntax.singleton._
  import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

  trait HReads[T] {
    def reads: Reads[T]
  }

  trait HReadsLowerPriority {

    implicit def readsGeneric[Repr <: HList, A](
      implicit
      gen: LabelledGeneric.Aux[A, Repr],
      readsRepr: Lazy[HReads[Repr]]
    ): HReads[A] = new HReads[A] {
      override def reads: Reads[A] = Reads[A] { json =>
        readsRepr.value.reads.reads(json).map(gen.from)
      }
    }

  }

  object HReads extends HReadsLowerPriority {

    implicit def readsHNil: HReads[HNil] = new HReads[HNil] {
      override def reads = Reads { json =>
        JsSuccess(HNil)
      }
    }


    implicit def readHCons[K <: Symbol, H, T <: HList](
      implicit
      witness: Witness.Aux[K],
      readsH: Reads[H],
      readsT: Lazy[HReads[T]]
    ): HReads[FieldType[K, H] :: T] = new HReads[FieldType[K, H] :: T] {
      override def reads: Reads[FieldType[K, H] :: T] = {
        val name = witness.value.name
        val readHead = (__ \ name).read[H](readsH)
        (readHead and readsT.value.reads) ((a, b) => (name ->> a :: b).asInstanceOf[FieldType[K, H] :: T])
      }
    }

  }

  def hReads[A](implicit readsInst: HReads[A]): Reads[A] = readsInst.reads

}