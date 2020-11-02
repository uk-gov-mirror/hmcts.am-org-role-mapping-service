package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.SubscriptionClient;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class RetrieveDeadLetterTopic {

    static final Gson GSON = new Gson();

    public static void main(String[] args) throws Exception, ServiceBusException {
        String connectionString = "Endpoint=sb://rd-servicebus-sandbox.servicebus.windows.net/;SharedAccessKeyName=SendAndListenSharedAccessKey;SharedAccessKey=97E6uvE6xHcqHAVlxufN1PH75tMHoZUe78FhsCbLLLQ=";
        SubscriptionClient subscription1Client = new SubscriptionClient(new ConnectionStringBuilder(connectionString, "rd-caseworker-topic-sandbox/subscriptions/rd-caseworker-subscription-sandbox"), ReceiveMode.PEEKLOCK);
        //SubscriptionClient subscription2Client = new SubscriptionClient(new ConnectionStringBuilder(connectionString, "rd-caseworker-topic-sandbox/subscriptions/rd-caseworker-subscription-sandbox"), ReceiveMode.PEEKLOCK);
        //SubscriptionClient subscription3Client = new SubscriptionClient(new ConnectionStringBuilder(connectionString, "rd-caseworker-topic-sandbox/subscriptions/rd-caseworker-subscription-sandbox"), ReceiveMode.PEEKLOCK);

        IMessageReceiver receiver = get();
        System.out.println(receiver);
        consumeDlq(receiver);
        //registerMessageHandlerOnClient(subscription1Client);
        //registerMessageHandlerOnClient(subscription2Client);
        //registerMessageHandlerOnClient(subscription3Client);
        log.info("clients registered.....");
    }


    public static IMessageReceiver get() throws ServiceBusException, InterruptedException {
        String connectionString = "Endpoint=sb://rd-servicebus-sandbox.servicebus.windows.net/;SharedAccessKeyName=SendAndListenSharedAccessKey;SharedAccessKey=97E6uvE6xHcqHAVlxufN1PH75tMHoZUe78FhsCbLLLQ=";
        SubscriptionClient subscription1Client = new SubscriptionClient(new ConnectionStringBuilder(connectionString, "rd-caseworker-topic-sandbox/subscriptions/rd-caseworker-subscription-sandbox/$deadletterqueue"), ReceiveMode.PEEKLOCK);

        try {
            return ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                    new ConnectionStringBuilder(connectionString, "rd-caseworker-topic-sandbox/subscriptions/rd-caseworker-subscription-sandbox/$deadletterqueue"),
                    ReceiveMode.PEEKLOCK);
        } catch (Exception e) {
            throw e;
        }
    }
    public static void consumeDlq(IMessageReceiver receiver) throws ServiceBusException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        IMessage message = receiver.receive();
            log.info("onMessageAsync.....{}", message);
            if (message.getLabel() != null/* &&
                        message.getContentType() != null
                        &&
                        message.getLabel().contentEquals("UserRequest") &&
                        message.getContentType().contentEquals("application/json")*/) {

                //byte[ ] body = message.getBody();
                List<byte[]> body = message.getMessageBody().getBinaryData();
                log.info("body.....{}", body);
                //UserRequest user = mapper.readValues(body, UserRequest.class);
                //UserRequest users = GSON.fromJson(new String(body, Charset.forName("UTF-8")), UserRequest.class);
                String users = null;
                try {
                    users = mapper.readValue(body.get(0), String.class);
                    System.out.printf("Message consumed successfully..... " + users);
                    receiver.complete(message.getLockToken());
                } catch (IOException e) {
                    e.printStackTrace();

                }
                System.out.printf(
                        "\n\t\t\t\t%s Message received: \n\t\t\t\t\t\tMessageId = %s, \n\t\t\t\t\t\tSequenceNumber = %s, \n\t\t\t\t\t\tEnqueuedTimeUtc = %s," +
                                "\n\t\t\t\t\t\tExpiresAtUtc = %s, \n\t\t\t\t\t\tContentType = \"%s\",  \n\t\t\t\t\t\tContent: [ User Id = %s]\n",
                        receiver.getEntityPath(),
                        message.getMessageId(),
                        message.getSequenceNumber(),
                        message.getEnqueuedTimeUtc(),
                        message.getExpiresAtUtc(),
                        message.getContentType(),
                        "",
                        "");
            }

        }

}