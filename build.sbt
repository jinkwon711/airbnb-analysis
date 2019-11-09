name := "spark-task"

version := "0.1"

scalaVersion := "2.12.10"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.4"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0-M2" % Test
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.0.0-preview"
