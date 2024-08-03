import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

public class Main{
    static final String brokers="kafka:9092";
    public static void main(String[] args) throws Exception{
        StreamExecutionEnvironment env=StreamExecutionEnvironment.getExecutionEnvironment();
        System.out.println("Environment Created");

        //KafkaSource<Weather>: This sets up a Kafka source to read Weather objects.

        KafkaSource<Weather> source=KafkaSource.<Weather>builder()
        .setBootstrapServers(brokers)
        .setProperty("partition.discovery.interval.ms", "1000")
        .setTopics("weather")
        .setGroupId("groupdId-919292")
        .setStartingOffsets(OffsetsInitializer.earliest())
        .setValueOnlyDeserializer(new WeatherDeserialization())
        .build();

        //DataStreamSource<Weather>: This creates a Flink data stream from the Kafka source.

        DataStreamSource<Weather> kafka=env.fromSource(source,WatermarkStrategy.noWatermarks(), "kafka");

        DataStream<Tuple2<WeatherStatistics,Double>> averageTempStream=kafka.keyBy(myEvent->myEvent.city)
        .window(TumblingProcessingTimeWindows.of(Time.seconds(60)))
        .aggregate(new WeatherAggregator());


        //.map(...): This transforms the stream of tuples (MyAverage, Double) to tuples (String, Double), where String is the city name and Double is the average temperature.

        DataStream<Tuple2<String,Double>> cityAndValueStream=averageTempStream
        .map(new MapFunction<Tuple2<WeatherStatistics,Double>,Tuple2<String,Double>>() {
            @Override
            public Tuple2<String,Double> map(Tuple2<WeatherStatistics, Double> input) throws Exception{
                return new Tuple2<>(input.f0.city,input.f1);
            }
        });

        System.out.println("Aggregation created");
        
    }

    public static class WeatherAggregator implements AggregateFunction<Weather, WeatherStatistics, Tuple2<WeatherStatistics,Double>>{
        @Override
        public WeatherStatistics createAccumulator() {
            //Accumulator initialized: {city: null, count: 0, sum: 0.0}
            return new WeatherStatistics();
        }
        @Override
        public WeatherStatistics add(Weather weather, WeatherStatistics statistics) {
            //logger.debug("add({},{})", myAverage.city, myEvent);
            statistics.city = weather.city;
            statistics.count = statistics.count + 1;
            statistics.sum = statistics.sum + weather.temp;
            return statistics;
        }
        @Override
        public Tuple2<WeatherStatistics, Double> getResult(WeatherStatistics statistics) {
            return new Tuple2<>(statistics, statistics.sum / statistics.count);
        }
        @Override
        public WeatherStatistics merge(WeatherStatistics statistics1, WeatherStatistics statistics2) {
            statistics1.sum += statistics2.sum;
            statistics1.count += statistics2.count;
            return statistics1;
        }


    }
    public static class WeatherStatistics{
        public String city;
        public Integer count = 0;
        public Double sum = 0d;

        @Override
        public String toString(){
            return "MyAverage{" +
                    "city='" + city + '\'' +
                    ", count=" + count +
                    ", sum=" + sum +
                    '}';
        } 
    }
}