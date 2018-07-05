package net.tstllc.play.auth.models

import java.util.UUID

case class TempUser(id: String, email: String)


case class User(userId: Long,
                permissions: Seq[String],
                isActive: Boolean,
                licenseeId: Long,
                licenseeName: String,
                licenseeEmail: String,
                licenseePhone: String,
                licenseeFQDN: String,
                branchId: Long,
                branchName: String,
                branchEmail: String,
                branchPhone: String,
                agentName: String,
                agentEmail: String,
                agentPhone: Option[String],
                branchesAreAgencies: Boolean,
                agentAreAgencies: Boolean) {
  lazy val seller: SellerInfo = {
    (branchesAreAgencies, agentAreAgencies, agentPhone) match {
      case (true, true, Some(goodPhone)) => {
        //got enough info to set up agent as seller
        SellerInfo(agentName, agentEmail, goodPhone)
      }
      case (true, _, _) => {
        //use branch info
        SellerInfo(branchName, branchEmail, branchPhone)
      }
      case _ => {
        // all other fall back to licensee
        SellerInfo(licenseeName, licenseeEmail, licenseePhone)
      }
    }
  }
}

case class SellerInfo (name: String, email: String, phone: String)

object User {
  def empty = User(0L, Nil, true, 0, "TST", "tst.testuser@email.com", "", "tst", 0L, "TST", "tst.testuserbranch@email.com", "", "Test User", "testuser@email.com", None, false, false)
}