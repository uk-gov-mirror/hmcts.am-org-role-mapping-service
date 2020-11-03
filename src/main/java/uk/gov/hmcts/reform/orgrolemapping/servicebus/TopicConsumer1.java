package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class TopicConsumer1 {

    static final Gson GSON = new Gson();
    static final int maxRetries = 3;

    public static void main(String[] args) throws Exception {
        String connectionString = "Endpoint=sb://rd-servicebus-sandbox.servicebus.windows.net/;SharedAccessKeyName=SendAndListenSharedAccessKey;SharedAccessKey=97E6uvE6xHcqHAVlxufN1PH75tMHoZUe78FhsCbLLLQ=";
        SubscriptionClient subscription1Client = new SubscriptionClient(new ConnectionStringBuilder(connectionString, "rd-caseworker-topic-sandbox/subscriptions/temporary"), ReceiveMode.PEEKLOCK);
        //SubscriptionClient subscription2Client = new SubscriptionClient(new ConnectionStringBuilder(connectionString, "rd-caseworker-topic-sandbox/subscriptions/temporary"), ReceiveMode.PEEKLOCK);
        registerMessageHandlerOnClient(subscription1Client);
        //registerMessageHandlerOnClient(subscription2Client);

        log.info("clients registered.....");
    }

    static void registerMessageHandlerOnClient(SubscriptionClient receiveClient) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        log.info("registerMessageHandlerOnClient.....");
        IMessageHandler messageHandler = new IMessageHandler() {
            // callback invoked when the message handler loop has obtained a message
            @SneakyThrows
            public CompletableFuture<Void> onMessageAsync(IMessage message) {
                int retryCount = 0;
                log.info("onMessageAsync.....{}", message);
                List<byte[]> body = message.getMessageBody().getBinaryData();
                log.info("body.....{}", body);
                log.info("token.....{}", message.getLockToken());
                Integer users = null;
                try {

                    //int maxRetries = 3;
                    //users = waitForRas(mapper, body, maxRetries);
                    for (int i = 0; i < maxRetries; i++) {
                        log.info("Iteration number :" + i);
                        if (roleAssignmentHealthCheck()) {
                            log.info("Parsing the value.");
                            users = mapper.readValue(body.get(0), Integer.class);
                            log.info("Parsing Complete");
                            break;
                        }
                    }
                    ///receiveClient.abandon(message.getLockToken());

                } catch (IOException e) {
                    try {
                        log.info("Abandoned message:" + message.getLockToken());
                        receiveClient.abandon(message.getLockToken());
                    } catch (InterruptedException | ServiceBusException ex) {
                        ex.printStackTrace();
                    }
                    log.error("throwing exception for unprocessable message");
                    throw e;
                }
                System.out.printf(
                        "\n\t\t\t\t%s Message received: \n\t\t\t\t\t\tMessageId = %s, \n\t\t\t\t\t\tSequenceNumber = %s, \n\t\t\t\t\t\tEnqueuedTimeUtc = %s," +
                                "\n\t\t\t\t\t\tExpiresAtUtc = %s, \n\t\t\t\t\t\tContentType = \"%s\",  \n\t\t\t\t\t\tContent: [ User Id = %s]\n",
                        receiveClient.getEntityPath(),
                        message.getMessageId(),
                        message.getSequenceNumber(),
                        message.getEnqueuedTimeUtc(),
                        message.getExpiresAtUtc(),
                        message.getContentType(),
                        "",
                        "");

                //System.out.printf("Message consumed successfully..... ");
                //log.info("token before marking as complete.....{}", message.getLockToken());
                return receiveClient.completeAsync(message.getLockToken());
            }

            public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
                log.error("Exception occured.");
                System.out.printf(exceptionPhase + "-" + throwable.getMessage());
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        receiveClient.registerMessageHandler(
                messageHandler, new MessageHandlerOptions(1,
                        false, Duration.ofSeconds(30), Duration.ofMinutes(5)),
                executorService);

    }

    private static boolean roleAssignmentHealthCheck() throws InterruptedException {
        // Call Health check
        log.info("Calling the health check");
        double var = Math.random();
        if(var < 0.50) {
            log.info("Sleeping for 2 seconds");
            Thread.sleep(2000);
            return false;
        }
        return true;
    }

}