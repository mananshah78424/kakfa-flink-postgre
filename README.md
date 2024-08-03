
Steps to Understand: 

Main.Java

1. Importing all the necessary dependencies.
2. Create StreamExecutionEnvironment. The StreamExecutionEnvironment is the context in which a streaming program is executed.
3. Define Kafka Source Configuration:
     Set up the KafkaSource<Weather> to read Weather objects from a Kafka topic:
     Set Kafka bootstrap servers.
     Set properties for partition discovery and topic.
     Set consumer group ID and starting offsets.
     Define a deserializer for Weather objects.

4. Create DataStream from Kafka Source.
5. Define Data Aggregation with Windowing:
     Apply a key-by operation to group the data by city (keyBy(myEvent -> myEvent.city)).
     Define a tumbling window of 60 seconds (window(TumblingProcessingTimeWindows.of(Time.seconds(60)))).
     Use the WeatherAggregator to aggregate data within the window.

   To illustrate, consider the stream of weather data for two cities, New York and Los Angeles:

    City	Temperature	Timestamp
    New York	25.0	2024-07-30 12:00:01
    New York	27.0	2024-07-30 12:00:10
    Los Angeles	30.0	2024-07-30 12:00:20
    New York	26.0	2024-07-30 12:00:30
    Los Angeles	32.0	2024-07-30 12:00:40
    New York	28.0	2024-07-30 12:00:50
    These events will be processed as follows:
    
    Partitioning by City:
    
    New York: [25.0, 27.0, 26.0, 28.0]
    Los Angeles: [30.0, 32.0]
    Tumbling Windows:
    
    Window 1 (12:00:00 - 12:00:59):
    New York: [25.0, 27.0, 26.0, 28.0]
    Los Angeles: [30.0, 32.0]
    Aggregation:
    
    Window 1:
    New York: (25.0 + 27.0 + 26.0 + 28.0) / 4 = 26.5
    Los Angeles: (30.0 + 32.0) / 2 = 31.0
    So, the averageTemperatureStream will contain:
    
    Tuple2<MyAverage, Double> for New York: (MyAverage{city='New York', count=4, sum=106.0}, 26.5)
    Tuple2<MyAverage, Double> for Los Angeles: (MyAverage{city='Los Angeles', count=2, sum=62.0}, 31.0)

   
7. Transform Aggregated Data:
     Map the aggregated results from Tuple2<WeatherStatistics, Double> to Tuple2<String, Double>, extracting the city name and average temperature.

8. Define Aggregation Function (WeatherAggregator):

   Implement the AggregateFunction interface:
   createAccumulator(): Initialize an empty WeatherStatistics object.
   add(Weather weather, WeatherStatistics statistics): Update the accumulator with new weather data.
   getResult(WeatherStatistics statistics): Compute the final average temperature and return it as a Tuple2<WeatherStatistics, Double>.
   merge(WeatherStatistics statistics1, WeatherStatistics statistics2): Merge two accumulators to handle window merging.

   Example in Context
  Suppose we have the following weather records:
  
  {city: "San Francisco", temperature: 15.0}
  {city: "San Francisco", temperature: 17.0}
  {city: "San Francisco", temperature: 19.0}
  Here's how the WeatherAggregator would process these records:
  
  First Record:
  
  Accumulator initialized: {city: null, count: 0, sum: 0.0}
  After add: {city: "San Francisco", count: 1, sum: 15.0}
  Second Record:
  
  Accumulator before add: {city: "San Francisco", count: 1, sum: 15.0}
  After add: {city: "San Francisco", count: 2, sum: 32.0}
  Third Record:
  
  Accumulator before add: {city: "San Francisco", count: 2, sum: 32.0}
  After add: {city: "San Francisco", count: 3, sum: 51.0}
  Final Result:
  
  getResult: ("San Francisco", 51.0 / 3) => ("San Francisco", 17.0)

8. Define Weather Statistics Class (WeatherStatistics):
     Create a class to hold city, count, and sum of temperatures.
     Override toString() for debugging and logging.
