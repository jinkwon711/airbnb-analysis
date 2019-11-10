# airbnb-analysis
This repository analyze airbnb listing data to find outliers and etc...
## Prerequisites
 - spark 3.0.0
 - spark-ml 3.0.0
 - sbt
 - scala 2.12.10
 
## 기획 내역
 - [기획 내역 위키](https://github.com/jinkwon711/airbnb-analysis/wiki/Airbnb-%EB%A7%A4%EC%B6%9C-%EC%A6%9D%EC%95%A1%EC%9D%84-%EC%9C%84%ED%95%9C-%EB%B6%84%EC%84%9D---Grouping-&-Outlier-%ED%83%90%EC%A7%80%EB%A5%BC-%ED%86%B5%ED%95%98%EC%97%AC)


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
