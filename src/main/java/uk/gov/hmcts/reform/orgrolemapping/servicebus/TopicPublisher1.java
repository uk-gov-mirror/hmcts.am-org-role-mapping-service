package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class TopicPublisher1 {
    static final Gson GSON = new Gson();
    static ObjectMapper mapper = new ObjectMapper();
    public static void main(String[] args) throws Exception, ServiceBusException {
        // TODO Auto-generated method stub

        TopicClient sendClient;
        String connectionString = "Endpoint=sb://rd-servicebus-sandbox.servicebus.windows.net/;SharedAccessKeyName=SendAndListenSharedAccessKey;SharedAccessKey=97E6uvE6xHcqHAVlxufN1PH75tMHoZUe78FhsCbLLLQ=";
        sendClient = new TopicClient(new ConnectionStringBuilder(connectionString, "rd-caseworker-topic-sandbox"));
        sendMessagesAsync(sendClient).thenRunAsync(() -> sendClient.closeAsync());
    }

    static CompletableFuture<Void> sendMessagesAsync(TopicClient sendClient) throws JsonProcessingException {
        String userRequest = "\"msg\"";

        String user = mapper.readValue(
                userRequest,
                new TypeReference<String>() {
                });

        log.info("user  {}", user);
        List<CompletableFuture> tasks = new ArrayList<>();
            final String messageId = Integer.toString(1);
            //Message message = new Message(GSON.toJson(user, UserRequest.class).getBytes(Charset.forName("UTF-8")));
            Message message = new Message(user);
            message.setContentType("application/json");
            message.setLabel("UserRequest");
            //message.setMessageId(messageId);
            message.setTimeToLive(Duration.ofMinutes(20));
            System.out.printf("Message sending: Id = %s\n", message.getMessageId());
            tasks.add(
                    sendClient.sendAsync(message).thenRunAsync(() -> {
                        System.out.printf("\tMessage acknowledged: Id = %s\n", message.getMessageId());
                    }));
        return CompletableFuture.allOf(tasks.toArray(new CompletableFuture<?>[tasks.size()]));
    }
}
