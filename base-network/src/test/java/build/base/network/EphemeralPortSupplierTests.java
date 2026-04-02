package build.base.network;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link EphemeralPortSupplier}.
 *
 * @author graeme.campbell
 * @since Aug-2019
 */
class EphemeralPortSupplierTests {

    /**
     * Ensures that a {@link EphemeralPortSupplier} supplies a port.
     */
    @Test
    void shouldSupplyAPort() {
        final var supplier = EphemeralPortSupplier.create();

        assertThat(supplier.getAsInt())
            .isInstanceOf(Integer.class);
    }

    /**
     * Ensures that the ports supplied by a {@link EphemeralPortSupplier} are distinct.
     */
    @Test
    void shouldSupplyMultipleDistinctPorts() {
        final var supplier = EphemeralPortSupplier.create();

        final var firstPort = supplier.getAsInt();
        final var secondPort = supplier.getAsInt();

        assertThat(firstPort)
            .isNotEqualTo(secondPort);
    }

    /**
     * Ensures that the ports supplied by distinct {@link EphemeralPortSupplier}s are distinct.
     */
    @Test
    void shouldSupplyDistinctPortsBetweenSuppliers() {
        final var supplierA = EphemeralPortSupplier.create();
        final var supplierB = EphemeralPortSupplier.create();

        final var portsFromA = IntStream.generate(supplierA)
            .limit(5)
            .boxed()
            .collect(Collectors.toSet());

        final var portsFromB = IntStream.generate(supplierB)
            .limit(5)
            .boxed()
            .collect(Collectors.toSet());

        assertThat(portsFromA)
            .isNotEqualTo(portsFromB);
    }

    /**
     * Ensures that the {@link EphemeralPortSupplier} does not provide leases on a port already in use.
     */
    @Test
    void shouldNotSupplyLeasesOnAPortWhichIsAlreadyLeased()
        throws IOException {

        final var originalSupplier = EphemeralPortSupplier.create();
        final var originalPort = originalSupplier.getAsInt();

        try (var socket = new ServerSocket()) {
            socket.bind(new InetSocketAddress(Network.reachableLocalAddresses()
                .findFirst()
                .orElseThrow(),
                originalPort));

            final var supplier = EphemeralPortSupplier.create();

            final var suppliedPort = supplier.getAsInt();

            assertThat(suppliedPort)
                .isNotEqualTo(originalPort);
        }
    }

    /**
     * Ensures that the {@link EphemeralPortSupplier} does not provide leases on ports which are already reserved
     * on a variety of different network addresses by {@link ServerSocket}s.
     */
    @Test
    void shouldNotSupplyLeasesOnPortsUsedByServerSockets()
        throws IOException {

        final var originalSupplier = EphemeralPortSupplier.create();
        try (var socket1 = new ServerSocket(); var socket2 = new ServerSocket(); var socket3 = new ServerSocket()) {

            final var port1 = originalSupplier.getAsInt();
            final var port2 = originalSupplier.getAsInt();
            final var port3 = originalSupplier.getAsInt();

            socket1.bind(new InetSocketAddress(Network.localWildcardAddress(), port1));
            socket2.bind(new InetSocketAddress(Network.reachableLocalAddresses().findFirst().orElseThrow(), port2));
            socket3.bind(new InetSocketAddress(Network.reachableLocalAddresses().findAny().orElseThrow(), port3));

            final var supplier = EphemeralPortSupplier.create();

            final var suppliedPorts = IntStream.generate(supplier)
                .limit(10)
                .boxed()
                .collect(Collectors.toSet());

            assertThat(suppliedPorts.contains(port1))
                .isFalse();

            assertThat(suppliedPorts.contains(port2))
                .isFalse();

            assertThat(suppliedPorts.contains(port3))
                .isFalse();
        }
    }

    /**
     * Ensures that a {@link EphemeralPortSupplier} cannot hold a lease on a port which already has a
     * {@link DatagramSocket} established on one of its non-wildcard addresses.
     */
    @Test
    void shouldNotSupplyLeaseOnPortWithDatagramSocketsReserved()
        throws IOException {

        final var originalSupplier = EphemeralPortSupplier.create();
        final var originalPort = originalSupplier.getAsInt();

        final var localAddress = Network.reachableLocalAddresses().findAny().orElseThrow();

        try (var socket = new DatagramSocket(originalPort, localAddress)) {
            final var supplier = EphemeralPortSupplier.create();

            final var suppliedPorts = IntStream.generate(supplier)
                .limit(5)
                .boxed()
                .collect(Collectors.toSet());

            assertThat(socket.getLocalPort())
                .isEqualTo(originalPort);

            assertThat(suppliedPorts.contains(originalPort))
                .isFalse();
        }
    }
}
