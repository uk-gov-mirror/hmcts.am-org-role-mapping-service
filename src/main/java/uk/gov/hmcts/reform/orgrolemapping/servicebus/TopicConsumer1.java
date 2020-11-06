package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.RetryExponential;
import com.microsoft.azure.servicebus.primitives.RetryPolicy;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class TopicConsumer1 {

    @Value("${amqp.host}")
    String host;
    @Value("${amqp.topic}")
    String topic;
    @Value("${amqp.sharedAccessKeyName}")
    String sharedAccessKeyName;
    @Value("${amqp.sharedAccessKeyValue}")
    String sharedAccessKeyValue;

    final int MAX_RETRIES = 1;

    @Bean
    public SubscriptionClient getSubscriptionClient(@Autowired RetryPolicy retryPolicy) throws URISyntaxException, ServiceBusException, InterruptedException {
        URI endpoint = new URI(host);

        ConnectionStringBuilder connectionStringBuilder = new ConnectionStringBuilder(
                endpoint,
                topic,
                sharedAccessKeyName,
                sharedAccessKeyValue);

        connectionStringBuilder.setOperationTimeout(Duration.ofMinutes(10));
        connectionStringBuilder.setRetryPolicy(retryPolicy);

        return new SubscriptionClient(connectionStringBuilder, ReceiveMode.PEEKLOCK);
    }

    @Bean
    RetryPolicy getRetryPolicy() {
        return new RetryExponential(Duration.ofSeconds(10),
                Duration.ofMinutes(1),
                50,
                "customRetryPolicy");
    }

    @Bean
    CompletableFuture<Void> registerMessageHandlerOnClient(@Autowired SubscriptionClient receiveClient) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        log.info("registerMessageHandlerOnClient.....");

        IMessageHandler messageHandler = new IMessageHandler() {
            // callback invoked when the message handler loop has obtained a message
            @SneakyThrows
            public CompletableFuture<Void> onMessageAsync(IMessage message) {
                log.info("onMessageAsync.....{}", message);
                List<byte[]> body = message.getMessageBody().getBinaryData();
                log.info("body.....{}", body);
                log.info("token.....{}", message.getLockToken());
                Integer users = null;
                try {
                    for (int i = 0; i < MAX_RETRIES; i++) {
                        log.info("Iteration number :" + i);
                        //throw new ServiceBusException(true);
                        if (roleAssignmentHealthCheck()) {
                            log.info("Parsing the value.");
                            users = mapper.readValue(body.get(0), Integer.class);
                            // Process the message in a separate method
                            log.info("Parsing Complete");
                            break;
                        }
                    }
                } catch (Exception e) { // java.lang.Throwable introduces the Sonar issues
                    try {
                        log.info("Abandoned message:" + message.getLockToken());
                        receiveClient.abandon(message.getLockToken());
                    } catch (InterruptedException | ServiceBusException ex) {
                        ex.printStackTrace();
                        throw ex;
                    }
                    log.error("throwing exception for unprocessable message");
                    throw e;
                }
                if (users != null) {
                    //Actual processing
                    log.info(
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

                    // Success only if Users != null
                } else {
                    log.info("Users is NULL");
                }
                return receiveClient.completeAsync(message.getLockToken());
            }

            public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
                log.error("Exception occured.");
                log.error(exceptionPhase + "-" + throwable.getMessage());
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        receiveClient.registerMessageHandler(
                messageHandler, new MessageHandlerOptions(1,
                        false, Duration.ofMinutes(5), Duration.ofMinutes(5)),
                executorService);
        return null;

    }

    private static boolean roleAssignmentHealthCheck() throws InterruptedException {
        // Call Health check
        log.info("Calling the health check");
        double var = Math.random();
        if (var > 0.50) {
            log.info("Sleeping for 2 seconds");
            Thread.sleep(2000);
            return false;
        }
        return true;
    }

}