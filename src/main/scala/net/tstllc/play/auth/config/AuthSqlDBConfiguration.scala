package net.tstllc.play.auth.config

import com.typesafe.config.Config
import javax.inject.Inject
import net.tstllc.datastore.config.SqlDatastoreConfig

class AuthSqlDBConfiguration @Inject() (config: Config) {

  val envConfig = config.getString("tst.services.environment") match {
    case "default"  => new SqlDatastoreConfig("db.sql", config)
    case _          => new SqlDatastoreConfig("db.sql", config)
  }
}
