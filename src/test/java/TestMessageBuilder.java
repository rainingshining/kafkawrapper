import com.alibaba.fastjson.JSON;
import com.customized.message.MessageBuilder;
import com.customized.message.TransferMessage;

public class TestMessageBuilder {

    public static void main(String[] args) {
        String result = MessageBuilder.builder()
                .flowNumber("1334540003515")
                .businessNumber("474396998097497894")
                .requestObj("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Application name=\"rob\">I have a name<Application/>")
                .outData("tongdun_fzwl", "returned message")
                .outData("xuexin_query", "another returned message")
                .buildJsonString();

        System.out.println(result);

        TransferMessage transferMessage = JSON.parseObject(result, TransferMessage.class);
        System.out.println("RequestObjString: " + transferMessage.readableRequestObj());
        System.out.println("xuexin_query: " + transferMessage.readableOutDataElementByCode("xuexin_query"));
    }
}
