import com.customized.example.ExampleSingleConsumer;
import com.customized.example.ExampleConsumerStartListener;

public class TestSingleConsumer {

    public static void main(String[] args) {
        ExampleSingleConsumer consumer = new ExampleSingleConsumer("consumer.properties");
        consumer.start(new ExampleConsumerStartListener());
    }
}
