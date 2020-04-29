import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class GotChar(name: String, culture: String, aliases: Seq[String], titles: Seq[String], allegiances: Seq[String])

case class GotHouse(name: String, region: String, coatOfArms: String, currentLord: String)

object Main {
  implicit val charSerde = jsonFormat5(GotChar)
  implicit val houseSerde = jsonFormat4(GotHouse)
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val apiUri = "https://anapioficeandfire.com/api/characters"

  def main(args: Array[String]): Unit = {
    getCharacters.onComplete {
      case Success(data) =>
        data.foreach(c => {
          c.allegiances match {
            case first :: Nil =>
              println(f"${c.name} is only loyal to one house")
              getAndPrintHouse(first)
            case first :: remaining =>
              println(f"${c.name} is loyal to many houses")
              getHouseByUri(first)
              remaining.foreach(getAndPrintHouse)
            case Nil => println(f"${c.name} is loyal to no one")
          }
        })
        Http().shutdownAllConnectionPools()
        system.terminate()
      case Failure(e) => e.printStackTrace()
    }
  }

  /**
   * Get Characters from characters endpoint
   */
  def getCharacters: Future[Seq[GotChar]] = {
    for {
      response <- Http().singleRequest(HttpRequest(method = HttpMethods.GET, uri = apiUri))
      entity <- Unmarshal(response.entity).to[Seq[GotChar]]
    } yield entity
  }

  /**
   * Get house and print the house name
   */
  def getAndPrintHouse: String => Unit = (uri: String) =>
    println(f"-- ${Await.result(getHouseByUri(uri), 5.seconds).name}")

  /**
   * Get House by URI from houses endpoint
   */
  def getHouseByUri(uri: String): Future[GotHouse] = {
    for {
      response <- Http().singleRequest(HttpRequest(method = HttpMethods.GET, uri = uri))
      entity <- Unmarshal(response.entity).to[GotHouse]
    } yield entity
  }
}

