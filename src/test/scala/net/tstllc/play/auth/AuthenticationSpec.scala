//package net.tstllc.play.auth
//
//import akka.stream.Materializer
//import com.typesafe.config.ConfigFactory
//import net.tstllc.play.auth.config.AuthSqlDBConfiguration
//import net.tstllc.play.auth.persistence.AuthSqlDB
//import org.scalatestplus.play.PlaySpec
//import org.scalatestplus.play.guice.GuiceOneAppPerSuite
//import play.api.Application
//import play.api.inject.guice.GuiceApplicationBuilder
//import play.api.inject.bind
//
//class AuthenticationSpec extends PlaySpec with GuiceOneAppPerSuite {
//
//  implicit lazy val materializer: Materializer = app.materializer
//
//  val application: Application = new GuiceApplicationBuilder()
//    .configure("some.configuration" -> "value")
//    .build()
//
//  private val authSqlDBConfig = new AuthSqlDBConfiguration(ConfigFactory.load)
//  private val authDB          = new AuthSqlDB(authSqlDBConfig)
//
//
//
//  "An essential action" should {
//    "can parse a JSON body" in {
//      val action: EssentialAction = Action { request =>
//        val value = (request.body.asJson.get \ "field").as[String]
//        Ok(value)
//      }
//
//      val request = FakeRequest(POST, "/").withJsonBody(Json.parse("""{ "field": "value" }"""))
//
//      val result = call(action, request)
//
//      status(result) mustEqual OK
//      contentAsString(result) mustEqual "value"
//    }
//  }
//}
