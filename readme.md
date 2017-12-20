# Play extension 

[travis]:                https://travis-ci.org/larousso/playjson-extended
[travis-badge]:          https://travis-ci.org/larousso/playjson-extended.svg?branch=master

[![travis-badge][]][travis]

## Usage 


```scala 

resolvers += "playjson-repo" at "https://raw.githubusercontent.com/larousso/playjson-extended/master/repository/releases/"

libraryDependencies += "com.adelegue" %% "playjson-extended" % "0.0.1"

``` 

## Rules 

With play json you can write this : 

```scala 
    import play.api.libs.json._
    
    case class Village(name: String)
    
    object Village {
      implicit val reads = Json.reads[Village]
    }
    
    case class Viking(name: String, surname: String, weight: Int, village: Seq[Village])

    implicit val reads = Json.reads[Viking]
```
 
 And if you want to add advanced validation you have to write this 
 
```scala 
    import play.api.libs.json._
    import play.api.libs.json.Reads._
    import play.api.libs.functional.syntax._
    
    case class Village(name: String)
    
    object Village {
     implicit val reads = Json.reads[Village]
    }
    
    case class Viking(name: String, surname: String, weight: Int, village: Seq[Village])
    
    implicit val reads = (
        (__ \ 'name).read[String] and 
        (__ \ 'surname).read[String] and 
        (__ \ 'weight).read[Int](min(0) keepAnd max(150)) and 
        (__ \ 'village).read[Seq[Village]]
    )(Viking.apply _)
```
 
 Here we have to write all the parsing rules just to validate `weight`. 
 
 With this lib you can just write  
 
```scala
    import play.api.libs.json._
    import play.api.libs.json.Reads._
    import play.api.libs.functional.syntax._
    import shapeless._
    import syntax.singleton._
    import playjson.rules._
  
    case class Village(name: String)
    
    object Village {
      implicit val reads = Json.reads[Village]
    }
    
    case class Viking(name: String, surname: String, weight: Int, village: Seq[Village])
    
    implicit val reads: Reads[Viking] = jsonRead[Viking].withRules(
      'weight ->> read(min(0) keepAnd max(150))
    )
```
  
You can just generate a `Reads[T]` using shapeless instead of scala macro 

```scala
    import play.api.libs.json._
    import playjson.reads._
    
    val monMojoReads: Reads[Viking] = hReads[Viking]
```
  
## Transformations

Transform json before converting to case class : 

```scala
    import play.api.libs.json._
    import play.api.libs.json.Reads._
    import play.api.libs.functional.syntax._
    import shapeless._
    import syntax.singleton._
    import playjson.rules._
    import playjson.transformation._
    
    case class Village(name: String)
    
    object Village {
      implicit val reads = Json.reads[Village]
    }
    
    case class Viking(name: String, surname: String, weight: Int, village: Seq[Village])
        
    val reads: Reads[Viking] =
    transform(
        ((__ \ 'theName) to (__ \ 'name)) and
        ((__ \ 'theSurname) to (__ \ 'surname)) and
        ((__ \ 'theVillage) to (__ \ 'village))
    ) andThen Json.reads[Viking]
    
    val jsResult: JsResult[Viking] = reads.reads(Json.obj(
        "theName" -> "Ragnar",
        "theSurname" -> "Lothbrok",
        "theVillage" -> Seq(Json.obj("name" -> "Kattegat")),
        "weight" -> 2
    ))
```

You can combine both rules and transformations 

```scala
      val reads: Reads[Viking] =
        transform(
            ((__ \ 'theName) to (__ \ 'name)) and
            ((__ \ 'theSurname) to (__ \ 'surname)) and
            ((__ \ 'theVillage) to (__ \ 'village))
        ) andThen jsonRead[Viking].withRules(
          'weight ->> read(min(0) keepAnd max(150)) and
          'name ->> read(pattern(".*".r)) and
          'surname ->> read(pattern(".*".r))
        )
```

Or use reads generated from shapeless LabelledGeneric : 

```scala

    val reads: Reads[Viking] =
        transform(
          ((__ \ 'theName) to (__ \ 'name)) and
            ((__ \ 'theSurname) to (__ \ 'surname)) and
            ((__ \ 'theVillage) to (__ \ 'village))
        ) andThen hReads[Viking]
```


# Release 

``` 
sbt 
> + publish 
```