package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.google.gson.Gson;
import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class TopicConsumer1 {

    static final Gson GSON = new Gson();

    public static void main(String[] args) throws Exception, ServiceBusException {
        String connectionString = "Endpoint=sb://rd-servicebus-sandbox.servicebus.windows.net/;SharedAccessKeyName=SendAndListenSharedAccessKey;SharedAccessKey=97E6uvE6xHcqHAVlxufN1PH75tMHoZUe78FhsCbLLLQ=";
        SubscriptionClient subscription1Client = new SubscriptionClient(new ConnectionStringBuilder(connectionString, "rd-caseworker-topic-sandbox/subscriptions/rd-caseworker-subscription-sandbox"), ReceiveMode.PEEKLOCK);
        //SubscriptionClient subscription2Client = new SubscriptionClient(new ConnectionStringBuilder(connectionString, "rd-caseworker-topic-sandbox/subscriptions/rd-caseworker-subscription-sandbox"), ReceiveMode.PEEKLOCK);
        //SubscriptionClient subscription3Client = new SubscriptionClient(new ConnectionStringBuilder(connectionString, "rd-caseworker-topic-sandbox/subscriptions/rd-caseworker-subscription-sandbox"), ReceiveMode.PEEKLOCK);

        registerMessageHandlerOnClient(subscription1Client);
        //registerMessageHandlerOnClient(subscription1Client);
        //registerMessageHandlerOnClient(subscription2Client);
        //registerMessageHandlerOnClient(subscription3Client);
        log.info("clients registered.....");
    }

 /*   static void registerMessageHandlerOnClient(SubscriptionClient receiveClient) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        log.info("registerMessageHandlerOnClient.....");
        IMessageHandler messageHandler = new IMessageHandler() {
            // callback invoked when the message handler loop has obtained a message
            public CompletableFuture<Void> onMessageAsync(IMessage message) {
                log.info("onMessageAsync.....{}", message);


                    //byte[ ] body = message.getBody();
                    List<byte[]> body = message.getMessageBody().getBinaryData();
                    log.info("body.....{}", body);
                    //UserRequest user = mapper.readValues(body, UserRequest.class);
                    //UserRequest users = GSON.fromJson(new String(body, Charset.forName("UTF-8")), UserRequest.class);
                    Integer users = null;
                    try {
                        users = mapper.readValue(body.get(0), Integer.class);
                    } catch (IOException e) {
                        try {
                            receiveClient.abandon(message.getLockToken());
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        } catch (ServiceBusException ex) {
                            ex.printStackTrace();
                        }
                        e.printStackTrace();
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

                System.out.printf("Message consumed successfully..... ");
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
                new MessageHandlerOptions(10, false, Duration.ofSeconds(5), Duration.ofSeconds(10)));

    }*/
 static void registerMessageHandlerOnClient(SubscriptionClient subscriptionClient) throws Exception {
     String connectionString = "Endpoint=sb://rd-servicebus-sandbox.servicebus.windows.net/;SharedAccessKeyName=SendAndListenSharedAccessKey;SharedAccessKey=97E6uvE6xHcqHAVlxufN1PH75tMHoZUe78FhsCbLLLQ=";
     //SubscriptionClient subscription1Client = new SubscriptionClient(new ConnectionStringBuilder(connectionString, "rd-caseworker-topic-sandbox/subscriptions/rd-caseworker-subscription-sandbox"), ReceiveMode.PEEKLOCK);

     IMessageReceiver subscriptionClient1 = ClientFactory.createMessageReceiverFromConnectionStringBuilder(new ConnectionStringBuilder(connectionString, "rd-caseworker-topic-sandbox/subscriptions/rd-caseworker-subscription-sandbox"), ReceiveMode.RECEIVEANDDELETE);

     System.out.println("Message Received 0");
     while (true) {
         IMessage receivedMessage = subscriptionClient1.receive(Duration.ofSeconds(10));
         if (receivedMessage != null) {
             System.out.println("Message Received 1");
             if (receivedMessage.getProperties() != null) {
                 for (String prop : receivedMessage.getProperties().keySet()) {
                     System.out.printf("%s=%s, ", prop, receivedMessage.getProperties().get(prop));
                 }
             }
             System.out.printf("CorrelationId=%s\n", receivedMessage.getCorrelationId());
             //receivedMessages++;
         } else {
             // No more messages to receive.
             break;
         }
     }
 }

}