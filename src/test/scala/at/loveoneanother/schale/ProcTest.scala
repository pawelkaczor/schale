package test.at.loveoneanother.schale

import java.io.IOException

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.Failure
import scala.util.Success

import org.scalatest.FunSuite

import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout
import at.loveoneanother.schale.Command
import at.loveoneanother.schale.Env
import at.loveoneanother.schale.ProcStderrReadChar
import at.loveoneanother.schale.ProcStdinClose
import at.loveoneanother.schale.ProcStdinFlush
import at.loveoneanother.schale.ProcStdoutReadLine
import at.loveoneanother.schale.Shell

class ProcTest extends FunSuite {
  test("run process and use exit status") {
    assertResult(0) { Command("echo", "a").waitFor() }
  }

  test("run process without IO") {
    Command("echo", "a").waitFor()
    intercept[IOException] {
      Command("does not exist").waitFor()
    }
  }

  test("single command and collect stdout/stderr") {
    assertResult("a") { Command("echo", "a").toString() }
  }

  test("consume stdout") {
    for (line <- Command("echo", "a").stdout)
      assertResult("a") { line }
  }

  test("consume stderr") {
    for (line <- Command("echo", "a").stderr)
      assertResult("") { line }
  }

  test("consume stdout and stderr") {
    for (line <- Command("echo", "a"))
      assertResult("a") { line }
  }

  test("feed to stdin") {
    assertResult(String.format("a%nb")) {
      (Command("cat").input(String.format("a%n"), String.format("b%n"))).toString()
    }
  }

  test("feed to stdin and consume stdout/stderr") {
    for (line <- Command("cat").input("a"))
      assertResult("a") { line }
  }

  test("interpret and collect stdout/stderr") {
    assertResult(String.format("a")) {
      Shell("echo a").toString()
    }
  }

  test("interpret using other interpreter") {
    assertResult(String.format("a")) {
      Shell("echo a", "/bin/ksh").toString()
    }
  }

  test("interpret and feed to stdin, then consume stdout/stderr") {
    for (line <- Shell("cat").input("a")) {
      assertResult("a") { line }
    }
  }

  test("interpret and use exit status") {
    val interpreter = Shell("cat").input("a")
    for (line <- interpreter) {
      assertResult("a") { line }
    }
    assertResult(0) { interpreter.waitFor() }
  }

  test("destroy process") {
    val proc = Shell("sleep 100")
    intercept[IllegalStateException] {
      proc.destroy()
    }
    proc.bg()
    assertResult(143) { proc.destroy() }
  }

  test("run in specified cwd") {
    new Env(pwd = "/") {
      assertResult("/") { Command("pwd").toString }
      assertResult("/") { Shell("pwd").toString }
    }
  }

  test("run in specified environment") {
    new Env(Map("newvar" -> "a")) {
      assertResult("a") { Shell("echo $newvar").toString }
    }
  }

  test("combine both env and pwd") {
    new Env(Map("newvar" -> "a")) {
      assertResult("a") { Shell("echo $newvar").toString }
      cd("/") {
        assertResult("a") { Shell("echo $newvar").toString }
        assertResult("/") { Command("pwd").toString }
      }
      cd("/tmp") {
        assertResult("a") { Shell("echo $newvar").toString }
        assertResult("/tmp") { Command("pwd").toString }
        env(Map("newvar2" -> "b")) {
          assertResult("/tmp") { Command("pwd").toString }
          cd("/") {
            assertResult("a") { Shell("echo $newvar").toString }
            assertResult("b") { Shell("echo $newvar2").toString }
            assertResult("/") { Command("pwd").toString }
          }
        }
      }
    }
  }

  test("interactive IO") {
    val proc = Command("grep", "a")
    proc interact { io =>
      import scala.concurrent.ExecutionContext.Implicits.global
      import at.loveoneanother.schale.actorSystem
      implicit val timeout = Timeout(2 seconds)

      io ! "a"
      io ! ProcStdinFlush
      io ! ProcStdinClose
      val future = io ? ProcStdoutReadLine
      future onComplete {
        case Success(line) => {
          assertResult("a") { line }
          assertResult(0) { proc.waitFor() }
        }
        case Failure(e) =>
          ProcTest.this.fail("cannot read proc output")
      }
      Await.result(future, 4 seconds)
    }
  }

  test("interactive IO (read character from stderr)") {
    val proc = Shell("grep a 1>&2")
    proc interact { io =>
      import scala.concurrent.ExecutionContext.Implicits.global
      import at.loveoneanother.schale.actorSystem
      implicit val timeout = Timeout(2 seconds)

      io ! "a"
      io ! ProcStdinFlush
      io ! ProcStdinClose
      val future = io ? ProcStderrReadChar
      future onComplete {
        case Success(char) => {
          assertResult('a') { char }
          assertResult(0) { proc.waitFor() }
        }
        case Failure(e) =>
          ProcTest.this.fail("cannot read proc output")
      }
      Await.result(future, 4 seconds)
    }
  }

  test("IO redirect to file using shell") {
    new java.io.File("/tmp/schale_test").delete()
    Shell("echo a | grep a > /tmp/schale_test").waitFor()
    assertResult(true) { new java.io.File("/tmp/schale_test").exists() }
  }
}
