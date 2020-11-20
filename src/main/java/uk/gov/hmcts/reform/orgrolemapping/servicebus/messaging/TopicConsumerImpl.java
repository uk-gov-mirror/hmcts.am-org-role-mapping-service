package uk.gov.hmcts.reform.orgrolemapping.servicebus.messaging;

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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.orgrolemapping.domain.model.UserRequest;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.BulkAssignmentOrchestrator;
import uk.gov.hmcts.reform.orgrolemapping.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.orgrolemapping.servicebus.deserializer.OrmCallbackDeserializer;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class TopicConsumerImpl {

    @Value("${amqp.host}")
    String host;
    @Value("${amqp.topic}")
    String topic;
    @Value("${amqp.sharedAccessKeyName}")
    String sharedAccessKeyName;
    @Value("${amqp.sharedAccessKeyValue}")
    String sharedAccessKeyValue;

    private BulkAssignmentOrchestrator bulkAssignmentOrchestrator;

    private final OrmCallbackDeserializer deserializer;

    final int MAX_RETRIES = 1;

    @Autowired
    private RoleAssignmentService roleAssignmentService;

    public TopicConsumerImpl(BulkAssignmentOrchestrator bulkAssignmentOrchestrator,
                         OrmCallbackDeserializer deserializer) {
        this.bulkAssignmentOrchestrator = bulkAssignmentOrchestrator;
        this.deserializer = deserializer;

    }

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
                2,
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
                log.info("token.....{}", message.getLockToken());
                    try {
                        log.info("getLockedUntilUtc" + message.getLockedUntilUtc());
                        log.info("getDeliveryCount value :" + message.getDeliveryCount());
                        if (roleAssignmentHealthCheck()) {
                            if (processMessage(message, body, mapper, receiveClient))
                                return receiveClient.completeAsync(message.getLockToken());
                        }
                        log.info("getLockToken......{}", message.getLockToken());

                    } catch (Throwable e) { // java.lang.Throwable introduces the Sonar issues
                        log.error("Unprocessable message........... ");
                        //log.info("Abandoning message after 3 retrials :" + message.getLockToken());
                        //receiveClient.abandon(message.getLockToken());
                        throw e;
                    }
                log.info("before completeAsync");
                //receiveClient.abandon(message.getLockToken());
                log.info("Finally getLockedUntilUtc" + message.getLockedUntilUtc());
                //return new CompletableFuture<Void>();
                return null;

            }

            public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
                log.error("Exception occured.");
                log.error(exceptionPhase + "-" + throwable.getMessage());
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        receiveClient.registerMessageHandler(
                messageHandler, new MessageHandlerOptions(1,
                        false, Duration.ofHours(1), Duration.ofMinutes(5)),
                executorService);
        log.info("before null");
        return null;

    }

    private boolean processMessage(IMessage message, List<byte[]> body, ObjectMapper mapper, SubscriptionClient receiveClient) throws java.io.IOException, InterruptedException, ServiceBusException {
        String users;
        log.info("Parsing the value.");
        UserRequest request = parseMessage(body, mapper);
        try {
            ResponseEntity<Object> response = bulkAssignmentOrchestrator.createBulkAssignmentsRequest(request);
            log.info("API Response {}", response.getStatusCode());

            log.info("Parsing Complete");
            return true;
        }
        catch(Exception e){
            log.error("Exception from ORM service {}", e);
            throw e;
        }
    }

    private UserRequest parseMessage(List<byte[]> body, ObjectMapper mapper) throws java.io.IOException {
        String users;
        users = mapper.readValue(body.get(0), String.class);
        String newUsers = users.replaceAll("\\s", "");
        log.info("newUsers {}", newUsers);
        List<String> profiles = Arrays.asList(newUsers.split(","));
        profiles.stream().forEach(s -> log.info("Users : {}", s));
        return UserRequest.builder().users(profiles).build();
    }

    private boolean roleAssignmentHealthCheck() throws InterruptedException {
        log.info("Calling the health check");
        try {
            String result = roleAssignmentService.getServiceStatus();
            log.info("Health end point is Up...");
        } catch (Throwable e) {
            log.error("Something is wrong with the health check...");
            throw e;
        }

        return true;
    }
}
