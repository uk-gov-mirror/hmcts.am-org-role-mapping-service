/*
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;

@Slf4j
@Component
public class TopicConsumer2 {

    @Value("${amqp.host}")
    String host;
    @Value("${amqp.topic}")
    String topic;
    @Value("${amqp.sharedAccessKeyName}")
    String sharedAccessKeyName;
    @Value("${amqp.sharedAccessKeyValue}")
    String sharedAccessKeyValue;

    final int MAX_RETRIES = 3;

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
        return new RetryExponential(Duration.ofSeconds(1),
                Duration.ofSeconds(30),
                2,
                "customRetryPolicy");
    }

    @Bean
    CompletableFuture<Void> registerMessageHandlerOnClient(@Autowired SubscriptionClient receiveClient) throws Exception {


        log.info("registerMessageHandlerOnClient.....");

        IMessageHandler messageHandler = new IMessageHandler() {
            // callback invoked when the message handler loop has obtained a message
            @SneakyThrows
            public CompletableFuture<Void> onMessageAsync(IMessage message) {
                log.info("onMessageAsync.....{}", message);
                processMessageWithRetry(message, 1, receiveClient);
                return receiveClient.completeAsync(message.getLockToken());
            }

            public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
                log.error("Exception occurred.");
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

    private void processMessageWithRetry(IMessage message, int retry, SubscriptionClient receiveClient) throws ServiceBusException, InterruptedException {
        try {
            log.info("processMessageWithRetry {}", message.getLockToken());

                processMessage(message);
        } catch (Exception e) {
            log.info("retry count is {}", retry);
            log.info("getDeliveryCount {}", message.getDeliveryCount());
            log.info("getLockToken {}", message.getLockToken());
            if (retry >= MAX_RETRIES) {
                log.error(format("Caught unknown unrecoverable error %s", e.getMessage()), e);
                receiveClient.abandon(message.getLockToken());
            } else {
                log.info(String.format("Caught recoverable error %s, retrying %s out of %s",
                        e.getMessage(), retry, MAX_RETRIES));
                processMessageWithRetry(message, retry + 1, receiveClient);
            }
        }
    }

    private void processMessage(IMessage message) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        List<byte[]> body = message.getMessageBody().getBinaryData();
        log.info("token.....{}", message.getLockToken());
        Integer user = mapper.readValue(body.get(0), Integer.class);
        //parseMessage(message, user);
    }

    private boolean parseMessage(IMessage message, Integer user) {
        if (user != null) {
            log.info(String.format(
                    "\n\t\t\t\t\t\tMessageId = %s," +
                            " \n\t\t\t\t\t\tSequenceNumber = %s, \n\t\t\t\t\t\tEnqueuedTimeUtc = %s," +
                            "\n\t\t\t\t\t\tExpiresAtUtc = %s, \n\t\t\t\t\t\tContentType = \"%s\"," +
                            "  \n\t\t\t\t\t\tContent: [ User Id = %s]\n",
                    message.getMessageId(),
                    message.getSequenceNumber(),
                    message.getEnqueuedTimeUtc(),
                    message.getExpiresAtUtc(),
                    message.getContentType(),
                    user));
            return true;

        }
        log.info("Parsing Complete");
        return false;
    }

    private static boolean roleAssignmentHealthCheck() throws InterruptedException, ServiceBusException {
        // Call Health check
        log.info("Calling the health check");
        double var = Math.random();
        if (var > 0.50) {
            log.info("Sleeping for 2 seconds");
            Thread.sleep(2000);
            return false;
        }
        return false;
    }

}*/
