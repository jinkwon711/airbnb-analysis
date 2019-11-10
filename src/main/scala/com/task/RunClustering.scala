package com.task

import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.feature.{MinMaxScaler, OneHotEncoder, StringIndexer, VectorAssembler}
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.evaluation.ClusteringEvaluator
import com.task.Utils._

import scala.reflect.io.File


object RunClustering {

  def main(args:Array[String]):Unit={
    val spark = SparkSession.builder().master("local[*]").getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    import spark.implicits._
    /**
     * Import listing csv data with options
     */

    val listings = spark.read.format("csv")
      .option("header", "true")
      .option("multiLine", "true")
      .option("mode", "DROPMALFORMED")
      .option("multiLine", "true")
      .option("inferSchema", "true")
      .option("wholeFile", "true")
      .option("escape", "\"")
      .load("resources/listings.csv")
      .selectExpr("id", "cast(reviews_per_month as double) reviews_per_month", "cast(review_scores_rating as double) review_scores_rating", "neighbourhood_group_cleansed", "property_type", "room_type", "cast(beds as double) beds", "bed_type", "price", "cancellation_policy")
      .na.drop()




    val refinedListings = listings
      .withColumn("price", takeHeadCharOff('price))
      .filter($"price" =!= 0.0)
      .withColumn("doubleFeatures", transformToVecs('beds, 'price, 'reviews_per_month, 'review_scores_rating))


    /**
     * Scalers for Double features
     */
    val scaler = new MinMaxScaler()
      .setInputCol("doubleFeatures")
      .setOutputCol("doubleFeaturesStd")


    /**
     * Indexers for stringed categorical values
     */
    val indexer = new StringIndexer().setInputCols(Array("neighbourhood_group_cleansed","property_type","room_type","bed_type","cancellation_policy"))
      .setOutputCols(Array("neighbourhoodIndex","property_typeIndex","room_typeIndex","bed_typeIndex","cancellation_policyIndex"))

    val scalerModel= scaler.fit(refinedListings)
    scalerModel.save("models/scalerModel")

    val stded = scalerModel.transform(refinedListings)
    val indexModel = indexer.fit(stded)
    indexModel.save("models/indexerModel")
    val indexed =   indexModel.transform(stded)
    //    .drop("neighbourhood_cleansed", "property_type", "room_type", "bed_type", "cancellation_policy")


    val encoder = new OneHotEncoder()
      .setInputCols(Array("property_typeIndex", "room_typeIndex", "bed_typeIndex", "cancellation_policyIndex"))
      .setOutputCols(Array("property_typeVec", "room_typeVec", "bed_typeVec", "cancellation_policyVec"))
    val encoderModel = encoder.fit(indexed)
    encoderModel.save("models/encoderModel")
    val encodedTrainingData = encoderModel.transform(indexed)
    //    .drop("neighbourhoodIndex", "property_typeIndex", "room_typeIndex", "bed_typeIndex", "cancellation_policyIndex")


    val assembler = new VectorAssembler().setInputCols(Array("room_typeVec", "cancellation_policyVec", "property_typeVec", "bed_typeVec", "doubleFeaturesStd"))
      .setOutputCol("features")
    val assembledTrainingData = assembler.transform(encodedTrainingData)

    val kmeans = new KMeans().setK(12).setSeed(1L)
    val kmodel = kmeans.fit(assembledTrainingData)

    // Make predictions
    val predictionDF = kmodel.transform(assembledTrainingData)

    // Evaluate clustering by computing Silhouette score
    val evaluator = new ClusteringEvaluator()
    val silhouette = evaluator.evaluate(predictionDF)
    println(s"Silhouette with squared euclidean distance = $silhouette")

    //  predictionDF.show(20, false)
    predictionDF.groupBy("prediction").count().show()
    predictionDF.filter($"prediction" === 0).show()
    predictionDF.filter($"prediction" === 1).show()
    predictionDF.filter($"prediction" === 2).show()
    predictionDF.filter($"prediction" === 3).show()
    predictionDF.filter($"prediction" === 4).show()


    val predictionWithDistancdDF = predictionDF.withColumn("distance", getDistanceToCentroid(kmodel.clusterCenters)('features, 'prediction))
    val top100FarIdDf = predictionWithDistancdDF.orderBy($"distance".desc).limit(100).select('id, 'distance)

    val rawTop100DF = listings.join(top100FarIdDf, Seq("id"))

    rawTop100DF.orderBy($"distance".desc).show(100, false)

    kmodel.save("models/k-means-6center")
  }
}
