package net.tstllc.play.auth.persistence

import javax.inject.{Inject, Singleton}
import net.tstllc.datastore.SqlDatastore
import net.tstllc.datastore.api.sql.SqlCommon.SqlResponse
import net.tstllc.datastore.config.SqlDatastoreConfig
import net.tstllc.slickcodegen.schema.TSTTables
import slick.lifted.TableQuery

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthDB @Inject()(config: SqlDatastoreConfig) extends SqlDatastore(config) {

  import net.tstllc.slickcodegen.schema.TSTTables._

  case class Tables(userTable:            TableQuery[TSTTables.User],
                    userProfileTable:     TableQuery[TSTTables.UserProfile],
                    roleTable:            TableQuery[TSTTables.Role],
                    licenseeTable:        TableQuery[TSTTables.TstLicensee],
                    branchTable:          TableQuery[TSTTables.Branch],
                    parametersTable:      TableQuery[TSTTables.TstParameters],
                    rolePermissionTable:  TableQuery[TSTTables.RolePermission],
                    permissionTable:      TableQuery[TSTTables.Permission])

  private val tables = Tables(User, UserProfile, Role, TstLicensee, Branch, TstParameters, RolePermission, Permission)

  def read[T](query: Tables => SqlReadAction[T])(implicit ec: ExecutionContext): Future[SqlResponse[T]] = withRecover {
    database.run(query(tables)).map(result => Right(result))
  }

  def write[T](query: Tables => SqlWriteAction[T])(implicit ec: ExecutionContext): Future[SqlResponse[T]] = withRecover {
    database.run(query(tables)).map(result => Right(result))
  }
}
