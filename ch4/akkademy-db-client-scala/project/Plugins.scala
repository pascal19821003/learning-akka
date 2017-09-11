import sbt.Keys._
import sbt._


object ProjectBuild extends Build {

  val hello = TaskKey[Unit]("hello", "just say hello")

  val helloTaskSetting = hello := {
    println("hello, sbt~")
  }

  val dist = TaskKey[Unit]("dist", "distribute current project as zip or gz packages")

  val distTask = dist <<= (baseDirectory, target, fullClasspath in Compile, packageBin in Compile, resources in Compile, streams) map {
    (baseDir, targetDir, cp, jar, res, s) =>
      s.log.info("[dist] prepare distribution folders...")
      val assemblyDir = targetDir / "akka-server"
      val confDir = assemblyDir / "conf"
      val libDir = assemblyDir / "lib"
      val binDir = assemblyDir / "bin"
      Array(assemblyDir, confDir, libDir, binDir).foreach(IO.createDirectory)
      s.log.info("[dist] copy jar artifact to lib...")
      IO.copyFile(jar, libDir / jar.name)
      s.log.info("[dist] copy 3rd party dependencies to lib...")
      cp.files.foreach(f => if (f.isFile) IO.copyFile(f, libDir / f.name))
      s.log.info("[dist] copy shell scripts to bin...")
      ((baseDir / "bin") ** "*.sh").get.foreach(f => IO.copyFile(f, binDir / f.name))
      ((baseDir ) ** "*.sh").get.foreach(f => IO.copyFile(f, binDir / f.name))
      s.log.info("[dist] copy configuration templates to conf...")
      ((baseDir / "conf") * "*.xml").get.foreach(f => IO.copyFile(f, confDir / f.name))
      ((res) * "*.conf").get.foreach(f => IO.copyFile(f, confDir / f.name))
      s.log.info("[dist] copy examples chanenl deployment...")
      IO.copyDirectory(baseDir / "examples", assemblyDir / "examples")
      res.filter(_.name.startsWith("logback")).foreach(f => IO.copyFile(f, assemblyDir / f.name))
  }

  lazy val root = Project(id = "aa", base = file(".")).settings(Defaults.defaultSettings ++ Seq(distTask, helloTaskSetting): _*)
}
