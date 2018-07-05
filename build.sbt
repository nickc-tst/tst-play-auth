val projectName = "tst-play-auth"
val settings = Seq(
  organization  := "net.tstllc",
  name          := projectName,
  version       := "0.1.0-SNAPSHOT",
  description   := "TST Play auth library",
  scalaVersion  := "2.12.6",
  exportJars    := true,

  scalacOptions += "-Ypartial-unification",

  credentials += Credentials("Artifactory Realm", "artifactory.infra.tstllc.net", "travel", "travelreads123"),
  credentials += Credentials(Path.userHome / ".sbt" / "credentials"),

  resolvers ++= Resolver.jcenterRepo +: Resolver.bintrayRepo("scalaz", "releases") +: {
    //Add resolvers here
    Seq(
      "Typesafe repository" at "https://repo.typesafe.com/typesafe/maven-releases/",
      "TST Snapshots"       at "https://artifactory.infra.tstllc.net/artifactory/snapshots",
      "TST Releases"        at "https://artifactory.infra.tstllc.net/artifactory/releases"
    )
  },

  libraryDependencies ++= Seq(
    "com.typesafe.play"       %%  "play"                % "2.6.15"          % "provided",
    "net.tstllc"              %%  "tst-datastore"       % "0.2.0-SNAPSHOT",
    "net.tstllc"              %%  "tst-slick-codegen"   % "2.0.0-SNAPSHOT"  changing(),
    "org.scalatestplus.play"  %%  "scalatestplus-play"  % "3.1.2"           % Test
  ))

val testing = {
  val scalaTest = Tests.Argument(TestFrameworks.ScalaTest, "-W", "120", "60")
  val scalaCheck = Tests.Argument(TestFrameworks.ScalaCheck, "-maxSize", "500", "-minSuccessfulTests", "100", "-workers", "2", "-verbosity", "2")

  Seq(
    javaOptions += "-Dlogback.configurationFile=./test/resources/logback.xml",
    testOptions ++= Seq(scalaCheck, scalaTest)
  )
}

lazy val root = Project(id = projectName, base = file("."))
  .settings(settings)
  .settings(inConfig(Test)(javaOptions += "-Dconfig.file=conf/reference.test.conf"))
  .settings(inConfig(Compile)(inTask(doc)(sources := Seq.empty) ++
    inTask(packageDoc)(publishArtifact := false)))