import com.customized.producer.KafkaCommonProducer;

import java.util.Scanner;

public class TestProducer {

    public static void main(String[] args) {
        KafkaCommonProducer producer = new KafkaCommonProducer("producer.properties");
        producer.init();
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.println("MSG:");
            String msg = scanner.nextLine();
            producer.publish(msg);
            if ("exit".equals(msg)){
                break;
            }
        }

    }
}
