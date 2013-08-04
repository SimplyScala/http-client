import sbt._
import sbt.Keys._

object SimplyScalaHttpBuild extends Build {
    // make library => 'sbt + package' & 'sbt + make-pom'

    lazy val root = Project(id = "http-client", base = file("."),
        settings = Project.defaultSettings ++ Seq(
            name := "http-client",

            organization := "com.github.simplyscala",
            description := "provides a reactive (async & non-blocking) http client API (based on netty)",

            version := "0.1-SNAPSHOT",

            scalaVersion := "2.10.2",

            crossScalaVersions := Seq("2.10.0", "2.10.1", "2.10.2"),

            libraryDependencies ++= Seq(
                "com.ning"       % "async-http-client"   % "1.7.16",

                "org.scalatest" %% "scalatest"          % "1.9.1"     % "test",
                "com.github.simplyscala"   %% "simplyscala-server" % "0.4"     % "test"/*,
                "com.twitter"              %% "finagle-core"       % "6.5.2"   % "test",
                "com.twitter"              %% "finagle-http"       % "6.5.2"   % "test"*/
            ),

            publishMavenStyle := true,
            publishArtifact in Test := false,
            pomIncludeRepository := { _ => false },

            pomExtra := (
                <url>https://github.com/SimplyScala/http-client</url>
                    <licenses>
                        <license>
                            <name>GPLv3</name>
                            <url>http://www.gnu.org/licenses/gpl-3.0.html</url>
                            <distribution>repo</distribution>
                        </license>
                    </licenses>
                    <scm>
                        <url>git@github.com:SimplyScala/http-client.git</url>
                        <connection>scm:git:git@github.com:SimplyScala/http-client.git</connection>
                    </scm>
                    <developers>
                        <developer>
                            <id>ugobourdon</id>
                            <name>bourdon.ugo@gmail.com</name>
                            <url>https://github.com/ubourdon</url>
                        </developer>
                    </developers>
                ),

            publishTo <<= version { v: String =>
                val nexus = "https://oss.sonatype.org/"
                if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
                else Some("releases" at nexus + "service/local/staging/deploy/maven2")
            }
        )
    )
}