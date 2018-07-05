package net.tstllc.play.auth

import javax.inject.Inject
import net.tstllc.play.auth.models.User
import net.tstllc.play.auth.providers.UserProvider
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

trait RequestParams {
  val ReferrerParam   = "ref"
  val SessionId       = "session.id"
  val AuthedUser      = "auth_principal"
  val SsoId           = "sso_id"
  val SsoProvider     = "sso_provider"
  val SessionExpires  = "session_expires"
  val Branch          = "branch"
  val ActiveCustomer  = "active_customer"
}

case class PreAuthenticatedRequest[A](user: Option[User], request: Request[A]) extends WrappedRequest[A](request)
case class AuthenticatedRequest[A](user: User, request: Request[A]) extends WrappedRequest[A](request)
case class PublicRequest[A](request: Request[A]) extends WrappedRequest[A](request)

class PreAuthenticatedAction @Inject()(val parser: BodyParsers.Default, userProvider: UserProvider)(implicit val executionContext: ExecutionContext)  extends ActionBuilder[PreAuthenticatedRequest, AnyContent] with ActionTransformer[Request, PreAuthenticatedRequest] with RequestParams {
  override protected def transform[A](request: Request[A]): Future[PreAuthenticatedRequest[A]] = {
    userProvider.provideUser(request.session.get(AuthedUser).getOrElse("0").toLong).map(optUser => PreAuthenticatedRequest(optUser, request))
  }
}

class PublicAction @Inject()(val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends ActionBuilder[PublicRequest, AnyContent] with ActionTransformer[Request, PublicRequest] {
  override def transform[A](request: Request[A]): Future[PublicRequest[A]] = Future.successful(PublicRequest[A](request))
}

class AuthenticationActionBuilder @Inject()(val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends ActionBuilder[AuthenticatedRequest, AnyContent] {
  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = block(request.asInstanceOf[AuthenticatedRequest[A]])
}

class AuthenticatedActionTransformer @Inject()(val parser: BodyParsers.Default)(implicit val executionContext: ExecutionContext) extends ActionTransformer[PreAuthenticatedRequest, AuthenticatedRequest] {
  override protected def transform[A](request: PreAuthenticatedRequest[A]): Future[AuthenticatedRequest[A]] = Future.successful(AuthenticatedRequest(request.user.get, request))
}

class AuthFlow @Inject()(preAuthenticatedAction: PreAuthenticatedAction, authenticationActionBuilder: AuthenticationActionBuilder, authenticatedActionTransformer: AuthenticatedActionTransformer)(implicit ec: ExecutionContext) extends Results {

  private def preAuthFilter(implicit ec: ExecutionContext): ActionFilter[PreAuthenticatedRequest] = new ActionFilter[PreAuthenticatedRequest] {
    override protected def filter[A](request: PreAuthenticatedRequest[A]): Future[Option[Result]] = {
      println(s"Filtering request")
      Future.successful {
        request.user match {
          case Some(u)  => { println(s"User ${u.userId} found"); None }
          case None     => { println(s"No user found"); Some(NotFound) }
        }
      }
    }

    override protected def executionContext: ExecutionContext = ec
  }

  private def flow: ActionBuilder[AuthenticatedRequest, AnyContent] = preAuthenticatedAction andThen preAuthFilter andThen authenticatedActionTransformer

  def authenticate[A](block: (AuthenticatedRequest[AnyContent]) => Future[Result]): Action[AnyContent] = {
    flow.async(block(_))
  }

  def authenticate[A](parser: BodyParser[A])(block: (AuthenticatedRequest[A]) => Future[Result]): Action[A]  = {
    flow.async(parser)(block(_))
  }
}

class Auth @Inject()(publicAction: PublicAction, authFlow: AuthFlow)(implicit ec: ExecutionContext) {
  def authenticated[A](parser: BodyParser[A])(block: (AuthenticatedRequest[A]) => Future[Result]): Action[A]  = authFlow.authenticate(parser)(block(_))
  def authenticated(block: (AuthenticatedRequest[AnyContent]) => Future[Result]): Action[AnyContent]          = authFlow.authenticate(block(_))
  def public[A](parser: BodyParser[A])(block: (PublicRequest[A]) => Future[Result]): Action[A]                = publicAction.async(parser)(block(_))
  def public(block: (PublicRequest[AnyContent]) => Future[Result]): Action[AnyContent]                        = publicAction.async(block(_))
  def public(block: => Future[Result]): Action[AnyContent]                                                    = publicAction.async(block)
}