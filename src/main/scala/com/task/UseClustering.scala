package com.task

import com.task.Utils.{deleteDirectory, getDistanceToCentroid, transformToVecs}
import org.apache.spark.ml.clustering.KMeansModel
import org.apache.spark.ml.feature.{MinMaxScalerModel, OneHotEncoderModel, StringIndexerModel, VectorAssembler}
import org.apache.spark.sql.SparkSession

case class userInput(id: String, price: Double, reviews_per_month: Double, review_scores_rating: Double, neighbourhood_grouop_cleansed: String, property_type: String, room_type: String, beds: Double, bed_type: String, cancellation_policy: String)

object UseClustering {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[*]").getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")
    import spark.implicits._

    val userInput = spark.read.format("csv")
      .option("header", "true")
      .option("multiLine", "true")
      .option("mode", "DROPMALFORMED")
      .option("multiLine", "true")
      .option("inferSchema", "true")
      .option("wholeFile", "true")
      .option("escape", "\"")
      .load("resources/userInput.csv")
      .selectExpr("id", "cast(reviews_per_month as double) reviews_per_month", "cast(review_scores_rating as double) review_scores_rating", "neighbourhood_group_cleansed", "property_type", "room_type", "cast(beds as double) beds", "bed_type", "price", "cancellation_policy")
      .na.drop()

    val indexerModel = StringIndexerModel.load("models/indexerModel")
    val encoderModel = OneHotEncoderModel.load("models/encoderModel")
    val scalerModel = MinMaxScalerModel.load("models/scalerModel")
    val loadedModel = KMeansModel.load("models/k-means-6center")

    val strIndexed = indexerModel.transform(userInput)
    val oneHotEncoded = encoderModel.transform(strIndexed)
    val doubleFeats = oneHotEncoded.withColumn("doubleFeatures", transformToVecs('beds, 'price, 'reviews_per_month, 'review_scores_rating))
    val minMaxScaled = scalerModel.transform(doubleFeats)

    val assembler = new VectorAssembler().setInputCols(Array("room_typeVec", "cancellation_policyVec", "property_typeVec", "bed_typeVec", "doubleFeaturesStd"))
      .setOutputCol("features")
    val assembledTrainingData = assembler.transform(minMaxScaled)

    val predicted = loadedModel.transform(assembledTrainingData).withColumn("distance", getDistanceToCentroid(loadedModel.clusterCenters)('features, 'prediction))
    deleteDirectory("output")
    predicted.write.json("output")
  }

}
