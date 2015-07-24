import sbt._
import Keys._
import xerial.sbt.Sonatype.sonatypeSettings

object Publish {
  lazy val settings = sonatypeSettings :+ (pomExtra :=
    <scm>
      <url>git@github.com:pawelkaczor/schale.git</url>
      <connection>scm:git:git@github.com:pawelkaczor/schale.git</connection>
      <developerConnection>scm:git:git@github.com:pawelkaczor/schale.git</developerConnection>
    </scm>
      <developers>
        <developer>
          <id>newicom</id>
          <name>Pawel Kaczor</name>
        </developer>
      </developers>)
}