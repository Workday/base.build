package build.base.network;

import build.base.assertion.Eventually;
import build.base.foundation.Closeables;
import build.base.io.SerializableCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link Client} and {@link Server}.
 *
 * @author brian.oliver
 * @since Nov-2018
 */
class ClientServerTests {

    /**
     * The {@link Client} used in this test.
     */
    private Client client;

    /**
     * The {@link Server} used in this test.
     */
    private Server server;

    /**
     * The {@link CompletableFuture} of {@link Connection} used in this test.
     */
    private CompletableFuture<Connection> connection;

    /**
     * The {@link URI} for the client used in this test.
     */
    private URI uri;

    /**
     * Establishes a {@link Client} and {@link Server} connection and verifies the status of the {@link Client},
     * {@link Server} and the {@link CompletableFuture} of {@link Connection} between the {@link Client}
     * and {@link Server}.
     *
     * @throws Exception should a {@link Exception} occur
     */
    @BeforeEach
    void beforeEach()
        throws Exception {

        // establish a server
        this.server = new Server(1).start();

        Eventually.assertThat(this.server.onStarted())
            .isCompleted();

        // find an InetAddress for the local machine that's of the same class as the Server
        // (ensures that if the Server is using IPv4, the address returned is IPv4)
        final InetAddress inetAddress = Network.reachableLocalAddresses().filter(
            Network.isOfClass(this.server.getLocalAddress())).findFirst().get();

        // establish a URI for the client
        this.uri = new URI("collider", null, inetAddress.getHostAddress(), this.server.getLocalPort(), null, null,
            null);

        // ensure the server doesn't have a connection for the client
        this.connection = this.server.onConnection(2);

        assertThat(this.connection)
            .isNotDone();

        this.client = new Client(2, this.uri);

        // ensure the client was started
        Eventually.assertThat(this.client.onStarted())
            .isCompleted();

        // ensure the client connection was made to the server
        Eventually.assertThat(this.connection)
            .isCompleted();

        Eventually.assertThat(this.connection.get().onStarted())
            .isCompleted();
        ;
    }

    /**
     * Closes the @{link Client} and {@link Server} for the test.
     */
    @AfterEach
    void afterEach() {
        Closeables.close(this.server);
        Closeables.close(this.client);
    }

    /**
     * Ensure the {@link Server} can be created.
     */
    @Test
    void shouldCreateServer() {
        this.server.close();

        Eventually.assertThat(this.server.onStopped())
            .isCompleted();
    }

    /**
     * Ensure the {@link Client} can be created and closed, and that it correspondingly connects and disconnects from a
     * {@link Server}.
     *
     * @throws Exception should a {@link Exception} occur
     */
    @Test
    void shouldCreateClientServer()
        throws Exception {

        this.client.close();

        // ensure the client was stopped
        Eventually.assertThat(this.client.onStopped())
            .isCompleted();

        // ensure the server connection for the client was closed
        Eventually.assertThat(this.connection.get().onClosed())
            .isCompleted();

        this.server.close();

        // ensure the server was stopped
        Eventually.assertThat(this.server.onStopped())
            .isCompleted();
    }

    /**
     * Ensure the {@link Client} is stopped when the {@link Server} is closed.
     */
    @Test
    void shouldStopClientWhenServerClosed() {

        this.server.close();

        // ensure the client was stopped
        Eventually.assertThat(this.client.onStopped())
            .isCompleted();

        // ensure the server connection is now cancelled
        Eventually.assertThat(this.server.onConnection(2))
            .isCancelled();
    }

    /**
     * Ensure the {@link Client} can send a {@link SerializableCallable} to the {@link Server}.
     */
    @Test
    void shouldSendRequestFromClientToServer() {

        final var message = "PINGPONG";

        final var result = this.client.submit(() -> message);

        Eventually.assertThat(result)
            .isCompletedWithValue(message);
    }

    /**
     * Ensure the {@link Server} can send a {@link SerializableCallable} to the {@link Client}.
     *
     * @throws Exception should a {@link Exception} occur
     */
    @Test
    void shouldSendRequestFromServerToClient()
        throws Exception {

        final var message = "PINGPONG";

        final var result = this.connection.get().submit(() -> message);

        Eventually.assertThat(result)
            .isCompletedWithValue(message);
    }

    /**
     * Ensure the {@link Client} can send a {@link SerializableCallable} to the {@link Server} that completes
     * exceptionally
     */
    @Test
    void shouldCompleteExceptionallyFromClientToServer() {

        final var message = "OH NO!";

        final var result = this.client.submit(() -> {
            throw new RuntimeException(message);
        });

        Eventually.assertThat(result)
            .isCompletedExceptionally()
            .hasCauseInstanceOf(RuntimeException.class);
    }

    /**
     * Ensure the {@link Server} can send a {@link SerializableCallable} to the {@link Client} that completes exceptionally.
     *
     * @throws Exception should a {@link Exception} occur
     */
    @Test
    void shouldCompleteExceptionallyFromServerToClient()
        throws Exception {

        final var message = "OH NO!";
        final var result = this.connection.get().submit(() -> {
            throw new RuntimeException(message);
        });

        Eventually.assertThat(result)
            .isCompletedExceptionally()
            .hasCauseInstanceOf(RuntimeException.class);
    }

    /**
     * Ensure a {@link Client} attempting to connect with the same identity is rejected.
     *
     * @throws Exception should a {@link Exception} occur
     */
    @Test
    void shouldRejectClientWithTheSameIdentity()
        throws Exception {

        try (var duplicate = new Client(2, this.uri)) {
            // ensure the duplicate client was started
            Eventually.assertThat(duplicate.onStarted())
                .isCompleted();

            // ensure the duplicate client can't process a request
            Eventually.assertThat(duplicate.submit(() -> "OH NO!"))
                .isCompletedExceptionally();

            // ensure the duplicate client was stopped
            Eventually.assertThat(duplicate.onStopped())
                .isCompleted();

            // ensure the client can process a request
            final var message = "PINGPONG";

            Eventually.assertThat(this.client.submit(() -> message))
                .isCompletedWithValue(message);
        }
    }

    /**
     * Ensure that the {@link Connection} on the {@link Server} is closed when {@link Server} is closed completely.
     *
     * @throws Exception should a {@link Exception} occur
     */
    @Test
    void shouldCloseAllConnectionsWhenServerClosed()
        throws Exception {
        this.server.close();

        // ensure the server was stopped
        Eventually.assertThat(this.server.onStopped())
            .isCompleted();

        // ensure the connection is closed at this point without waiting
        Eventually.assertThat(this.connection.get().onClosed())
            .isCompleted();
    }
    //
    //    /**
    //     * Ensure that a {@link Server} can publish and {@link Client} can subscribe for the published items.
    //     *
    //     * @throws Exception should a {@link Exception} occur
    //     */
    //    @Test
    //    void shouldServerPublishClientSubscribe()
    //        throws Exception {
    //
    //        final String stringPublisher = "serverPublisher";
    //
    //        // create the publisher on the server
    //        this.connection.get().createPublisher(stringPublisher, String.class)
    //            .ifPresent(publisher -> {
    //                try {
    //                    // create a RecordingObserver
    //                    final RecordingSubscriber<String> subscriber = new RecordingSubscriber<>();
    //
    //                    // subscribe the observer on the client
    //                    this.client.subscribe(stringPublisher, String.class, subscriber);
    //
    //                    // publish from the server and verify on the client
    //                    publishAndVerify(publisher, subscriber);
    //                }
    //                catch (final Exception e) {
    //                    throw new RuntimeException(e);
    //                }
    //            });
    //    }
    //
    // /**
    //     * Ensure that a {@link Client} can publish and {@link Server} can subscribe for the published items.
    //     */
    //    @Test
    //    void shouldClientPublishServerSubscribe() {
    //
    //        final String stringPublisher = "clientPublisher";
    //
    //        // create the publisher on the client
    //        this.client.createPublisher(stringPublisher, String.class)
    //            .ifPresent(publisher -> {
    //                try {
    //                    // create a RecordingObserver
    //                    final RecordingObserver<String> observer = new RecordingObserver<>();
    //
    //                    // subscribe the observer on the server
    //                    this.connection.get().subscribe(stringPublisher, String.class, observer);
    //
    //                    // publish from the client and verify on the server
    //                    publishAndVerify(publisher, observer);
    //                }
    //                catch (final Exception e) {
    //                    throw new RuntimeException(e);
    //                }
    //            });
    //    }
    //
    //    /**
    //     * Ensure that after {@link Publisher} closes, publishing will stop.
    //     */
    //    @Test
    //    void shouldPublisherClose() {
    //
    //        final String stringPublisher = "clientPublisher";
    //        final String stringMessage = " published string";
    //
    //        // create the publisher on the client
    //        this.client.createPublisher(stringPublisher, String.class)
    //            .ifPresent(publisher -> {
    //                try {
    //                    // create a RecordingObserver
    //                    final RecordingObserver<String> observer = new RecordingObserver<>();
    //
    //                    // subscribe the observer on the server
    //                    this.connection.get().subscribe(stringPublisher, String.class, observer);
    //
    //                    // publish two items
    //                    publisher.publish("First" + stringMessage);
    //                    publisher.publish("Second" + stringMessage);
    //
    //                    // close the publisher
    //                    publisher.close();
    //
    //                    // publish again and this item will not be published
    //                    publisher.publish("Third" + stringMessage);
    //
    //                    Eventually.assertThat(observer, IteratorPatternMatchers.<String>starts()
    //                        .then().matches("First" + stringMessage)
    //                        .then().matches("Second" + stringMessage)
    //                        .then().ends());
    //                }
    //                catch (final Exception e) {
    //                    throw new RuntimeException(e);
    //                }
    //            });
    //    }
    //
    //    /**
    //     * Ensure that after {@link Client} closes, publishing from the {@link Client} side will stop.
    //     */
    //    @Test
    //    void shouldClientCloseInPublishSubscribe() {
    //
    //        final String stringPublisher = "clientPublisher";
    //        final String stringMessage = " published string";
    //
    //        // create the publisher on the client
    //        this.client.createPublisher(stringPublisher, String.class)
    //            .ifPresent(publisher -> {
    //                try {
    //                    // create a RecordingObserver
    //                    final RecordingObserver<String> observer = new RecordingObserver<>();
    //
    //                    // subscribe the observer on the server
    //                    this.connection.get().subscribe(stringPublisher, String.class, observer);
    //
    //                    // publish two items
    //                    publisher.publish("First" + stringMessage);
    //                    publisher.publish("Second" + stringMessage);
    //
    //                    // close the client
    //                    this.client.close();
    //                    Eventually.assertThat(this.client.onStopped(), completed());
    //
    //                    // publish again and this item will not be published
    //                    publisher.publish("Third" + stringMessage);
    //
    //                    Eventually.assertThat(observer, IteratorPatternMatchers.<String>starts()
    //                        .then().matches("First" + stringMessage)
    //                        .then().matches("Second" + stringMessage)
    //                        .then().ends());
    //                }
    //                catch (final Exception e) {
    //                    throw new RuntimeException(e);
    //                }
    //            });
    //    }
    //
    //    /**
    //     * Ensure that after {@link Client} closes, publishing from the {@link Client} side will stop.
    //     *
    //     * @throws Exception should a {@link Exception} occur
    //     */
    //    @Test
    //    void shouldServerCloseInPublishSubscribe()
    //        throws Exception {
    //
    //        final String stringPublisher = "serverPublisher";
    //        final String stringMessage = " published string";
    //
    //        // create the publisher on the server
    //        this.connection.get().createPublisher(stringPublisher, String.class)
    //            .ifPresent(publisher -> {
    //                try {
    //                    // create a RecordingObserver
    //                    final RecordingObserver<String> observer = new RecordingObserver<>();
    //
    //                    // subscribe the observer on the client
    //                    this.client.subscribe(stringPublisher, String.class, observer);
    //
    //                    // publish two items
    //                    publisher.publish("First" + stringMessage);
    //                    publisher.publish("Second" + stringMessage);
    //
    //                    // close the server
    //                    this.server.close();
    //
    //                    // wait until the connection is closed
    //                    Eventually.assertThat(this.server.onStopped(), completed());
    //
    //                    // publish again and this item will not be published
    //                    publisher.publish("Third" + stringMessage);
    //
    //                    Eventually.assertThat(observer, IteratorPatternMatchers.<String>starts()
    //                        .then().matches("First" + stringMessage)
    //                        .then().matches("Second" + stringMessage)
    //                        .then().ends());
    //                }
    //                catch (final Exception e) {
    //                    throw new RuntimeException(e);
    //                }
    //            });
    //    }
    //
    //    /**
    //     * Ensure that multiple publishing and subscription works correctly with the {@link Serializable} {@link TestItem}.
    //     *
    //     * @throws Exception should a {@link Exception} occur
    //     */
    //    @Test
    //    void shouldMultiplePublishersMultipleSubscribers()
    //        throws Exception {
    //
    //        final String itemPublisher = "itemPublisher";
    //        final List<Publisher<TestItem>> publishers = new ArrayList<>();
    //        final List<RecordingObserver<TestItem>> observers = new ArrayList<>();
    //
    //        // create two unique publishers and verify the publishing and subscription works
    //        for (int i = 0; i < 2; i++) {
    //            final String publisherName = itemPublisher + i;
    //            this.client.createPublisher(publisherName, TestItem.class)
    //                .ifPresent(publisher -> {
    //                    try {
    //                        publishers.add(publisher);
    //                        final RecordingObserver<TestItem> observer = new RecordingObserver<>();
    //                        observers.add(observer);
    //                        this.connection.get().subscribe(publisherName, TestItem.class, observer);
    //                        publisher.publish(new TestItem("GOT", "episode1"));
    //                        publisher.publish(new TestItem("GOT", "episode2"));
    //                        publisher.publish(new TestItem("WestWorld", "episode1"));
    //
    //                        Eventually.assertThat(observer.filter(testItem -> testItem.getName().equals("GOT")),
    //                            IteratorPatternMatchers.<TestItem>starts()
    //                                .then().matches(item -> item.getMessage().equals("episode1"))
    //                                .then().matches(item -> item.getMessage().equals("episode2"))
    //                                .then().ends());
    //                    }
    //                    catch (final Exception e) {
    //                        throw new RuntimeException(e);
    //                    }
    //                });
    //        }
    //
    //        if (publishers.size() == 2 && observers.size() == 2) {
    //            // close the first publisher
    //            publishers.get(0).close();
    //
    //            // publish again
    //            publishers.forEach(publish -> publish.publish(new TestItem("WestWorld", "episode2")));
    //
    //            // ensure the first publisher stops publishing, and the second publisher can still publish
    //            Eventually.assertThat(observers.get(0).filter(item -> item.getName().equals("WestWorld")),
    //                IteratorPatternMatchers.<TestItem>starts()
    //                    .thenLater().matches(item -> item.getMessage().equals("episode1"))
    //                    .then().ends());
    //
    //            Eventually.assertThat(observers.get(1).filter(item -> item.getName().equals("WestWorld")),
    //                IteratorPatternMatchers.<TestItem>starts()
    //                    .thenLater().matches(item -> item.getMessage().equals("episode2"))
    //                    .then().ends());
    //        }
    //    }
    //
    //        /**
    //         * Publish some {@link String}s using the provided {@link Publisher} and verify using the provided
    //         * {@link RecordingSubscriber}.
    //         *
    //         * @param publisher the {@link Publisher}
    //         * @param subscriber  the {@link RecordingSubscriber}
    //         */
    //        private void publishAndVerify(final Publisher<String> publisher,
    //                                      final RecordingSubscriber<String> subscriber) {
    //
    //            final String stringMessage = " published string";
    //
    //            publisher.publish("First" + stringMessage);
    //            publisher.publish("Second" + stringMessage);
    //            publisher.publish("Third" + stringMessage);
    //
    //            // ensure the observers observe the items
    //            Eventually.assertThat(subscriber, IteratorPatternMatchers.<String>starts()
    //                .then().matches("First" + stringMessage)
    //                .then().matches("Second" + stringMessage)
    //                .then().matches("Third" + stringMessage)
    //                .then().ends());
    //        }

    //    /**
    //     * A {@link Serializable} item that can be published and subscribed.
    //     */
    //    private static class TestItem
    //        implements Serializable {
    //
    //        /**
    //         * The name of the {@link TestItem}.
    //         */
    //        private final String name;
    //
    //        /**
    //         * The message related to the {@link TestItem}.
    //         */
    //        private final String message;
    //
    //        /**
    //         * Constructs a {@link TestItem}.
    //         *
    //         * @param name    the name
    //         * @param message the message
    //         */
    //        private TestItem(final String name, final String message) {
    //            this.name = name;
    //            this.message = message;
    //        }
    //
    //        /**
    //         * Obtains the name of the {@link TestItem}.
    //         *
    //         * @return the name
    //         */
    //        public String getName() {
    //            return this.name;
    //        }
    //
    //        /**
    //         * Obtains the message of the {@link TestItem}.
    //         *
    //         * @return the message
    //         */
    //        public String getMessage() {
    //            return this.message;
    //        }
    //    }
}
