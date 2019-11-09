package com.task

import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.sql.functions.udf

import scala.reflect.io.File

object Utils {
  def takeHeadCharOff = udf((row: String) => {
    try {
      row.slice(1, row.length).split("\\.").head.replaceAll(",", "").toDouble
    } catch {
      case e: Exception => println(row)
        0.0
    }
  })
  def transformToVecs = udf((beds: Double, price: Double, reviews_per_month: Double, review_score: Double) => {
    Vectors.dense(beds, price, reviews_per_month, review_score)
  })

  def getDistanceToCentroid(clusterCenters: Array[Vector]) = udf((features: Vector, prediction: Int) => {
    val distance = Vectors.sqdist(features, clusterCenters(prediction))
    distance
  })

  def isDirectoryExists(directory: String): Boolean = {
    val dir = File(directory)
    if (dir.isDirectory) true else false
  }

  def isFileExists(fileName: String): Boolean = {
    val file = File(fileName)
    if (file.isFile && file.exists) true else false
  }

  def deleteFile(fileName: String): Unit = {
    val file = File(fileName)
    if (file.isFile && file.exists) {
      file.delete()
    }
  }

  def deleteDirectory(directory: String): Unit = {
    val dir = File(directory)
    if (dir.isDirectory && dir.exists) {
      dir.deleteRecursively()
    }
  }

  def deleteFileWithPath(path: String): Boolean = {
    val file = File(path)
    if (file.isDirectory) {
      file.deleteRecursively()
    } else {
      file.delete()
    }
  }
}
