package net.tstllc.play.auth.providers

import javax.inject.{Inject, Singleton}
import net.tstllc.play.auth.models.User
import net.tstllc.play.auth.persistence.AuthRepo

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserProvider @Inject() (authRepo: AuthRepo){

  def provideUser(userId: Long)(implicit ec: ExecutionContext): Future[Option[User]] = authRepo.getAuthenticatedUser(userId)
}
