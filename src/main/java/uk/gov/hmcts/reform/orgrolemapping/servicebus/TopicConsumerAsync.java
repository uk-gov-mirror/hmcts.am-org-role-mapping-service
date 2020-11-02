/*

package uk.gov.hmcts.reform.orgrolemapping.servicebus;

import com.microsoft.azure.management.Azure;
import reactor.core.Disposable;

import java.util.concurrent.TimeUnit;

public class TopicConsumerAsync {

    public static void main(String[] args) throws InterruptedException {

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        String connectionString = "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
                + "SharedAccessKey={key}";

        // Create a receiver.
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<queue-name>>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .receiver()
                .queueName("<<queue-name>>")
                .buildAsyncClient();

        Disposable subscription = receiver.receiveMessages()
                .flatMap(context -> {
                    boolean messageProcessed = false;
                    // Process the context and its message here.
                    // Change the `messageProcessed` according to you business logic and if you are able to process the
                    // message successfully.
                    if (messageProcessed) {
                        return receiver.complete(context.getMessage());
                    } else {
                        return receiver.abandon(context.getMessage());
                    }
                }).subscribe();

        // Subscribe is not a blocking call so we sleep here so the program does not end.
        TimeUnit.SECONDS.sleep(60);

        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        receiver.close();
    }
}
*/
