import sbt._
import sbt.Keys._

object SimplyScalaHttpBuild extends Build {
    // make library => 'sbt + package' & 'sbt + make-pom'

    lazy val root = Project(id = "simplyscala-http", base = file("."),
        settings = Project.defaultSettings ++ Seq(
            name := "simplyscala-http",

            version := "0.1-SNAPSHOT",

            scalaVersion := "2.10.2",

            resolvers += "SimplyScala repository" at "https://github.com/SimplyScala/repository/raw/master/release",

            libraryDependencies ++= Seq(
                "com.ning"       % "async-http-client"   % "1.7.16",

                "org.scalatest" %% "scalatest"          % "1.9.1"     % "test",
                "com.github.simplyscala"   %% "simplyscala-server" % "0.4"     % "test"
            )
        )
    )
}