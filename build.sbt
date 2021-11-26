name := """devote"""
organization := "org.devote"

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.13.3"

resolvers +=
  "jitpack" at "https://jitpack.io"

libraryDependencies += guice
libraryDependencies += "org.reflections" % "reflections" % "0.9.11"
libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"
libraryDependencies += "com.h2database" % "h2" % "1.4.192"
libraryDependencies ++= Seq(evolutions, jdbc)
libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.68"
libraryDependencies += "org.bouncycastle" % "bcpkix-jdk15on" % "1.68"
libraryDependencies += "com.auth0" % "java-jwt" % "3.8.2"
libraryDependencies ++= Seq(javaWs)
libraryDependencies += "io.seruco.encoding" % "base62" % "0.1.3"
libraryDependencies += "com.github.stellar" % "java-stellar-sdk" % "0.24.0"
libraryDependencies += "com.github.ipfs" % "java-ipfs-http-client" % "1.3.3"

libraryDependencies ++= Seq(javaJpa % "test", "org.hibernate" % "hibernate-core" % "5.4.2.Final" % "test")
libraryDependencies += "com.github.database-rider" % "rider-core" % "1.7.2" % "test"
libraryDependencies += "org.mockito" % "mockito-core" % "3.1.0" % "test"
libraryDependencies += "com.jayway.jsonpath" % "json-path" % "2.4.0" % "test"
libraryDependencies += "org.hamcrest" % "hamcrest-library" % "1.3" % "test"

javaOptions in Test ++= Seq("-Dconfig.file=conf/application.test.conf")

enablePlugins(JacocoCoverallsPlugin)
jacocoExcludes ++= Seq("controllers.javascript*")
jacocoExcludes ++= Seq("router.*")
jacocoExcludes ++= Seq("data.entities.*")


