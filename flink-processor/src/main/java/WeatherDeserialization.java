// Takes raw object from Kafka and maps it to Weather.java class

import java.io.IOException;

import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//The deserialization schema describes how to turn the byte messages delivered by certain data sources (for example Apache Kafka) into data types (Java/Scala objects) that are processed by Flink.

public class WeatherDeserialization extends AbstractDeserializationSchema<Weather>{
    private static final long serialVersionUID=1;
    private transient ObjectMapper objectMapper;
    //Here too
    // Initialization method for the schema. It is called before the actual working methods deserialize(byte[]) and thus suitable for one time setup work.

    @Override
    public void open(InitializationContext context){
        objectMapper = JsonMapper.builder().build().registerModule(new JavaTimeModule());
    }

    //Deserializes the byte message.

    @Override
    public Weather deserialize(byte[] message) throws IOException{
        return objectMapper.readValue(message, Weather.class);
    }
}