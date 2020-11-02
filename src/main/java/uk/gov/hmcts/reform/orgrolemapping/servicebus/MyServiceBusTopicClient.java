package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MyServiceBusTopicClient {

    static final Gson GSON = new Gson();


    public static void main(String[] args) throws Exception, ServiceBusException {
        String connectionString = "Endpoint=sb://rd-servicebus-sandbox.servicebus.windows.net/;SharedAccessKeyName=SendAndListenSharedAccessKey;SharedAccessKey=97E6uvE6xHcqHAVlxufN1PH75tMHoZUe78FhsCbLLLQ=";
        SubscriptionClient subscription1Client = new SubscriptionClient(new ConnectionStringBuilder(connectionString, "rd-caseworker-topic-sandbox/subscriptions/rd-caseworker-subscription-sandbox"), ReceiveMode.PEEKLOCK);

        registerMessageHandlerOnClient(subscription1Client);
    }

    static void registerMessageHandlerOnClient(SubscriptionClient receiveClient) throws Exception {

        IMessageHandler messageHandler = new IMessageHandler() {
            public CompletableFuture<Void> onMessageAsync(IMessage message) {
                try {
                    UserRequest userRequest = deserialize(message, receiveClient);
                } catch (ServiceBusException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("TopicConsumer - Deserializer userRequest received from the service bus by ORM service {}",
                        message);

                return receiveClient.completeAsync(message.getLockToken());
            }

            public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
                System.out.printf(exceptionPhase + "-" + throwable.getMessage());
            }
        };
        receiveClient.registerMessageHandler(
                messageHandler,
                // callback invoked when the message handler has an exception to report
                // 1 concurrent call, messages aren't auto-completed, auto-renew duration
                new MessageHandlerOptions(1, false, Duration.ofMinutes(1)));

    }


    public static UserRequest deserialize(IMessage message, SubscriptionClient receiveClient) throws ServiceBusException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            log.info("getLabel {}", message.getLabel());
            log.info("getContentType {}", message.getContentType());
            log.info("getBody {}", message.getBody());
            byte[] body = message.getBody();

            UserRequest user = GSON.fromJson(new String(body, Charset.forName("UTF-8")), UserRequest.class);
            return user;
        } catch (Exception e) {
            receiveClient.abandon(message.getLockToken());
            log.info("------------Message abondoned.......{}", message.getLabel());
            throw new IllegalArgumentException("Could not deserialize callback", e);
        }
    }
}
