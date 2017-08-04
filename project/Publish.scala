import sbt._
import Keys._
import xerial.sbt.Sonatype.sonatypeSettings

object Publish {
  lazy val settings = sonatypeSettings ++ Seq(
    scmInfo := Some(
      ScmInfo(url("https://github.com/pawelkaczor/schale"), "scm:git:git@github.com:pawelkaczor/schale.git</")
    ),
    pomExtra :=
      <developers>
        <developer>
          <id>pawelkaczor</id>
          <name>Pawel Kaczor</name>
          <url>https://github.com/pawelkaczor</url>
        </developer>
      </developers>
  )
}