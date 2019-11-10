name := "spark-task"

version := "0.1"

scalaVersion := "2.12.10"

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.0.0-preview"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.0-preview"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0-M2" % Test
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.0.0-preview"
libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.12.1"
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.last
}