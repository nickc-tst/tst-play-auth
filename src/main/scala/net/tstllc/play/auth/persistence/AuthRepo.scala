package net.tstllc.play.auth.persistence

import javax.inject.{Inject, Singleton}
import net.tstllc.play.auth.models.User
import net.tstllc.slickcodegen.schema._

import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.MySQLProfile.api._

@Singleton
class AuthRepo @Inject() (authSqlDB: AuthDB) {

  case class UserQueryResponse(user:      TSTTables.UserRow,
                               role:      TSTTables.RoleRow,
                               licensee:  TSTTables.TstLicenseeRow,
                               branch:    TSTTables.BranchRow,
                               profile:   TSTTables.UserProfileRow,
                               params:    TSTTables.TstParametersRow)

  def getAuthenticatedUser(userId: Long)(implicit ec: ExecutionContext): Future[Option[User]] = {
    // Getting user info
    val result = authSqlDB.read { tables =>
      val paramName = "#branches.are.agencies"
      val query = for {
        user        <- tables.userTable if user.userId === userId
        role        <- tables.roleTable if role.roleId === user.roleId
        licensee    <- tables.licenseeTable if licensee.licenseeId === user.licenseeId
        branch      <- tables.branchTable if branch.branchId === user.branchId
        profile     <- tables.userProfileTable if profile.userId === user.userId
        param       <- tables.parametersTable if (param.parameterName === paramName && (param.licenseeId === user.licenseeId))
      } yield (user, role, licensee, branch, profile, param).mapTo[UserQueryResponse]
      query.result.headOption
    }

    for {
      f0 <- result
      f1 <- f0 match {
        case Left(e)  => {
          println(s"An error occurred attempting to authenticate a user. ${e.message}")
          Future.successful(None)
        }
        case Right(u) => {
          // If we found a user then get permissions for that user and create a User object
          u.map { response =>
            val permissionResult = authSqlDB.read { tables =>
              val permissionQuery = for {
                map         <- tables.rolePermissionTable if map.roleId === response.user.roleId
                permission  <- TSTTables.Permission if permission.permissionId === map.permissionId
              } yield (permission.permissionKey)
              permissionQuery.result
            }

            val licenseeHelper    = TSTTables.getTstLicenseeRowWrapper(response.licensee)
            val branchHelper      = TSTTables.getBranchRowWrapper(response.branch)
            val userProfileHelper = TSTTables.getUserProfileRowWrapper(response.profile)

            permissionResult.map { permissions =>

              permissions match {
                case Left(e)  => None
                case Right(r) => {
                  val perms = r.flatten
                  Some(User(
                    response.user.userId,
                    perms,
                    response.user.active.getOrElse(false),
                    response.user.licenseeId,
                    licenseeName = licenseeHelper.name.getOrElse(""),
                    licenseeEmail = licenseeHelper.email.getOrElse(""),
                    licenseePhone = licenseeHelper.phone.getOrElse(""),
                    licenseeFQDN = licenseeHelper.fqdn.getOrElse("Not found"),
                    branchId = branchHelper.branchId,
                    branchName = branchHelper.branchName,
                    branchEmail = branchHelper.email.getOrElse(""),
                    branchPhone = branchHelper.phone.getOrElse(""),
                    agentName = s"${userProfileHelper.firstName} ${userProfileHelper.lastName}",
                    agentEmail = userProfileHelper.email.getOrElse(""),
                    agentPhone = userProfileHelper.phone,
                    branchesAreAgencies = response.params.dataValue.contains("true"),
                    agentAreAgencies = branchHelper.agentsAreAgencies
                  ))
                }
              }
            }
          }.getOrElse(Future.successful(None))
        }
      }
    } yield (f1)
  }
}

//class UserDao @Inject()(dbConfig: DbConfiguration) {
//
//  import dbConfig.config.profile.api._
//  private val db = dbConfig.db
//
//  /**
//    * Get common data needed for the authenticated user. All requests will have access to this User type
//    */
//  def getAuthedUserContext(userIdOpt: Option[Long])(implicit ec: ExecutionContext): Future[Option[User]] = {
//
//    userIdOpt.map(id => {
//
//      // Get a tuple of all the rows we need.
//      val getUserData = (for {
//        u <- TSTTables.User if u.userId === id
//        r <- TSTTables.Role if r.roleId === u.roleId
//        l <- TSTTables.TstLicensee if l.licenseeId === u.licenseeId
//        b <- TSTTables.Branch if b.branchId === u.branchId
//        profile <- TSTTables.UserProfile if profile.userId === u.userId
//        param <- TSTTables.TstParameters if (param.parameterName === "#branches.are.agencies" && (param.licenseeId === u.licenseeId))
//      } yield (u, r, l, b, profile, param)).result
//
//      val userDataFtr = db.run(getUserData)
//
//      // Grab a sequence of all permissions tied to that role. Go through each one and query for the permission key.
//      val result = userDataFtr.map(userDataInfoOpt => {
//        userDataInfoOpt.map(tuple => {
//          val permissionQuery = for {
//            map <- TSTTables.RolePermission if map.roleId === tuple._1.roleId
//            permission <- TSTTables.Permission if permission.permissionId === map.permissionId
//          } yield (permission.permissionKey)
//
//          val permissionResultsFtr = db.run(permissionQuery.result).map(_.flatten)
//
//          val licenseeHelper = TSTTables.getTstLicenseeRowWrapper(tuple._3)
//          val branchHelper = TSTTables.getBranchRowWrapper(tuple._4)
//          val userProfileHelper = TSTTables.getUserProfileRowWrapper(tuple._5)
//
//
//          permissionResultsFtr.map(permissionResults => {
//            println(s"-------------->> ${permissionResults} param ${tuple._6.dataValue} ${tuple._6.parameterId} ${tuple._6.licenseeId}")
//            val user = User(
//              userId = tuple._1.userId,
//              permissions = permissionResults,
//              isActive = tuple._1.active.getOrElse(false),
//              licenseeId = tuple._1.licenseeId,
//              licenseeName = licenseeHelper.name.getOrElse(""),
//              licenseeEmail = licenseeHelper.email.getOrElse(""),
//              licenseePhone = licenseeHelper.phone.getOrElse(""),
//              licenseeFQDN = licenseeHelper.fqdn.getOrElse("Not found"),
//              branchId = branchHelper.branchId,
//              branchName = branchHelper.branchName,
//              branchEmail = branchHelper.email.getOrElse(""),
//              branchPhone = branchHelper.phone.getOrElse(""),
//              agentName = s"${userProfileHelper.firstName} ${userProfileHelper.lastName}",
//              agentEmail = userProfileHelper.email.getOrElse(""),
//              agentPhone = userProfileHelper.phone,
//              branchesAreAgencies = tuple._6.dataValue.contains("true"),
//              agentAreAgencies = branchHelper.agentsAreAgencies
//            )
//            println(s"----------------->> got user: ${user}")
//            user
//          })
//        })
//      })
//
//      val resultSane = result.flatMap(Future.sequence(_))
//      resultSane.map(_.headOption)
//    }).getOrElse(Future.successful(Some(User.empty)))
//  }
//
//}
