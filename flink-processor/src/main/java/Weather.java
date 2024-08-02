import java.util.Objects;

public class Weather{
    public String city;
    public Double temp;

    public Weather(){}

    public Weather(String city,String temp){
        this.city=city;
        this.temp=Double.valueOf(temp);;
    }

    @Override
    public String toString(){
        final StringBuilder sb=new StringBuilder("Weather{");
        sb.append("city=").append(city).append('\'');
        sb.append(", temperature=").append(String.valueOf(temp)).append('\'');
        return sb.toString();

    }
    public int hashCode() {
    return Objects.hash(super.hashCode(), city, temp);
    }

}