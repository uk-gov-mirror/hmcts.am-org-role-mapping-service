/*
package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.function.Supplier;

*/
/**
 * Deletes messages from envelopes Dead letter queue.
 *//*

@Component
//@ConditionalOnProperty("scheduling.task.delete-envelopes-dlq-messages.enabled")
public class DlqCleaner {

    private static final Logger log = LoggerFactory.getLogger(DlqCleaner.class);
    private static final String TASK_NAME = "delete-envelopes-dlq-messages";

    Supplier<IMessageReceiver> dlqReceiverProvider;

    //@Scheduled(cron = "${scheduling.task.delete-envelopes-dlq-messages.cron}")
    public void deleteMessagesInEnvelopesDlq() throws ServiceBusException, InterruptedException {
        log.info("Started {} job", TASK_NAME);
        IMessageReceiver messageReceiver = null;

        try {
            messageReceiver = dlqReceiverProvider.get();

            int completedCount = 0;
            IMessage message = messageReceiver.receive();
            while (message != null) {
                    messageReceiver.complete(message.getLockToken());
                    completedCount++;
                    log.info(
                            "Completed message from envelopes dlq. messageId: {} Current time: {}",
                            message.getMessageId(),
                            Instant.now()
                    );

                message = messageReceiver.receive();
            }

            log.info("Finished processing messages in envelopes dlq. Completed {} messages", completedCount);
        } catch (Exception e) {
            log.error("Unable to connect to envelopes dead letter queue", e);
        } finally {
            if (messageReceiver != null) {
                try {
                    messageReceiver.close();
                } catch (ServiceBusException e) {
                    log.error("Error closing dlq connection", e);
                }
            }
        }
        log.info("Finished {} job", TASK_NAME);
    }

    public IMessageReceiver get() throws ServiceBusException, InterruptedException {
        try {
            String connectionString = "Endpoint=sb://rd-servicebus-sandbox.servicebus.windows.net/;SharedAccessKeyName=SendAndListenSharedAccessKey;SharedAccessKey=97E6uvE6xHcqHAVlxufN1PH75tMHoZUe78FhsCbLLLQ=";
            SubscriptionClient subscription1Client = new SubscriptionClient(new ConnectionStringBuilder(connectionString, "rd-caseworker-topic-sandbox/subscriptions/rd-caseworker-subscription-sandbox/$deadletterqueue"), ReceiveMode.PEEKLOCK);

            return ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                    new ConnectionStringBuilder(new ConnectionStringBuilder(connectionString,
                            "rd-caseworker-topic-sandbox/subscriptions/rd-caseworker-subscription-sandbox/$deadletterqueue"),
                            ReceiveMode.PEEKLOCK)
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (ServiceBusException e) {
            throw e;
        }
    }


}
*/
