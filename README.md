# tst-play-auth

A convenience library for supporting authenticated requests.

### Configuration

```
  db {
    sql {
      driver      = com.mysql.jdbc.Driver
      database    = sql-test
      url         = "jdbc:mysql://localhost:3306/sql-test?useSSL=false"
      user        = "root"
      password    = ""
      maxThreads  = 5
    }
  }
```

### Public and Authenticated requests

```scala
import net.tstllc.play.auth.{Auth, AuthenticatedRequest, PublicRequest}

class SimpleController @Inject()(cc: ControllerComponents, appRepo: AppRepo, auth: Auth) extends AbstractController(cc) {
 
  def find(id: String): Action[AnyContent] = auth.public { request: PublicRequest[AnyContent] =>
    appRepo.findById(id).map(Ok(Json.toJson(_)))
  }

  def save(): Action[Sailing] = auth.authenticated(parse.json[Sailing]) { request: AuthenticatedRequest[Sailing] =>
    println(s"Got authenticated user ${request.user.userId}")
    val sailing: Sailing = request.body
    appRepo.save(sailing).map(Ok)
  }
}
```