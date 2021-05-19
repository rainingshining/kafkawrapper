import com.customized.example.ExampleBulkConsumer;
import com.customized.example.ExampleConsumerStartListener;
import com.customized.example.ExampleConsumerStopListener;

import java.util.concurrent.TimeUnit;

public class TestBulkConsumer {

    public static void main(String[] args) throws Exception{
        ExampleBulkConsumer consumer = new ExampleBulkConsumer("consumer.properties");
        consumer.start(new ExampleConsumerStartListener());
        TimeUnit.SECONDS.sleep(5);
        consumer.stop(new ExampleConsumerStopListener());
        System.out.println("done");

        TimeUnit.SECONDS.sleep(5);
        consumer.start(new ExampleConsumerStartListener());
    }
}
