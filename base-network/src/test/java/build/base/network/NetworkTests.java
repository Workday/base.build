package build.base.network;

import build.base.assertion.Eventually;
import build.base.foundation.Closeables;
import build.base.network.option.SocketTimeout;
import build.base.option.Timeout;
import build.base.retryable.PermanentFailureException;
import build.base.retryable.option.RetryFrequency;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Tests for {@link Network} utilities.
 *
 * @author graeme.campbell
 * @since Mar-2019
 */
class NetworkTests {

    /**
     * Ensure that {@link Network#connect} completes properly when connecting to a {@link Server} {@link Socket}.
     *
     * @throws IOException should socket connection issues occur
     */
    @Test
    void shouldCompleteWhenConnected()
        throws IOException {

        try (var server = new Server(1).start()) {

            final var inetAddress = Network.reachableLocalAddresses()
                .filter(Network.isOfClass(server.getLocalAddress()))
                .findFirst()
                .orElseThrow();

            final var address = new InetSocketAddress(inetAddress, server.getLocalPort());

            final CompletableFuture<Socket> futureSocket = Network.connect(
                address,
                SocketTimeout.ofMillis(100),
                Timeout.ofMillis(1000),
                RetryFrequency.every(Duration.ofMillis(100)));

            Eventually.assertThat(futureSocket)
                .isCompleted();

            Closeables.close(futureSocket.join());
        }
    }

    /**
     * Ensure that {@link Network#connect} completes properly when connecting to a {@link Socket} even though it starts
     * attempting to connect to the socket before the server starts.
     *
     * @throws IOException should socket connection issues occur
     */
    @Test
    void shouldEventuallyCompleteWhenConnected()
        throws IOException {

        final var port = 22222;
        final var address = new InetSocketAddress(InetAddress.getByName("localhost"), port);

        final CompletableFuture<Socket> futureSocket = Network.connect(
            address,
            SocketTimeout.ofMillis(100),
            Timeout.ofMillis(1000),
            RetryFrequency.every(Duration.ofMillis(100)));

        try (var socket = new ServerSocket(port)) {
            Eventually.assertThat(futureSocket)
                .isCompleted();

            Closeables.close(futureSocket.join());
        }
    }

    /**
     * Ensure that {@link Network#connect} fails with a concurrent {@link NetworkException} if the {@link Socket} it
     * tries to connect to is unavailable.
     *
     * @throws IOException should unexpected socket issues occur
     */
    @Test
    void shouldFailIfServerIsUnreachable()
        throws IOException {

        try (var server = new Server(1)) {
            server.close();

            final var inetAddress = Network.reachableLocalAddresses()
                .filter(Network.isOfClass(server.getLocalAddress()))
                .findFirst()
                .orElseThrow();

            final var address = new InetSocketAddress(inetAddress, server.getLocalPort());

            final CompletableFuture<Socket> futureSocket = Network.connect(
                address,
                SocketTimeout.ofMillis(100),
                Timeout.ofMillis(1000),
                RetryFrequency.every(Duration.ofMillis(100)));

            Eventually.assertThat(futureSocket)
                .isCompletedExceptionally()
                .hasCauseInstanceOf(PermanentFailureException.class);
        }
    }

    /**
     * Ensure that {@link Network#connect} cancels properly if cancelled.
     *
     * @throws IOException should socket issues occur
     */
    @Test
    void shouldStopIfCanceled()
        throws IOException {

        try (var server = new Server(1)) {
            server.close();

            final var inetAddress = Network.reachableLocalAddresses()
                .filter(Network.isOfClass(server.getLocalAddress()))
                .findFirst()
                .orElseThrow();

            final var address = new InetSocketAddress(inetAddress, server.getLocalPort());

            final CompletableFuture<Socket> futureSocket = Network.connect(
                address,
                SocketTimeout.ofMillis(100),
                Timeout.ofMillis(1000),
                RetryFrequency.every(Duration.ofMillis(100)));

            futureSocket.cancel(true);

            Eventually.assertThat(futureSocket)
                .isCancelled();
        }
    }
}
