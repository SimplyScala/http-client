import sbt._
import sbt.Keys._

class SimplyScalaHttpBuild extends Build {
    // make library => 'sbt + package' & 'sbt + make-pom'

    lazy val root = Project(id = "simplyscala-http", base = file("."),
        settings = Project.defaultSettings ++ Seq(
            name := "simplyscala-http",

            version := "0.1-SNAPSHOT",

            scalaVersion := "2.9.2",

            crossScalaVersions := Seq("2.9.0", "2.9.1", "2.9.2"),

            libraryDependencies ++= Seq(
                "com.nig"       %% "async-http-client"  % "1.7.8",

                "org.scalatest" %% "scalatest"          % "1.8"     % "test",
                "simplyscala"   %% "simplyscala-server" % "0.1"     % "test"
            )
        )
    )
}
