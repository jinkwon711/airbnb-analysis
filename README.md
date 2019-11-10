# airbnb-analysis
This repository analyze airbnb listing data to find outliers and etc...

## 사용법
```
$ cd airbnb-analysis
$ sbt assembly


# 다시 학습하려면 모델폴더를 삭제 후 listings데이터를 넣어주어야함.
$ rm -rf ./models/*
# put listings.csv in resources directory where listings_10000.csv exists
spark-submit --master local --class com.task.RunClustering target/scala-2.12/spark-task-assembly-0.1.jar


# 모델 사용을 위하여
# put userInput.csv to resources directory

$ spark-submit --master local --class com.task.UseClustering target/scala-2.12/spark-task-assembly-0.1.jar
$ cd output
$ vim part*
```
