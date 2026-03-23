package build.base.telemetry.ansi;

/*-
 * #%L
 * base.build Telemetry (ANSI)
 * %%
 * Copyright (C) 2025 Workday Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import build.base.foundation.UniformResource;
import build.base.telemetry.Activity;
import build.base.telemetry.Meter;
import build.base.telemetry.NamedUnit;
import build.base.telemetry.Progress;
import build.base.telemetry.Telemetry;
import build.base.telemetry.TelemetryRecorder;
import build.base.telemetry.TelemetryRecorderFactory;
import build.base.telemetry.foundation.AbstractTelemetryRecorder;

import java.io.Closeable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link TelemetryRecorder} that uses an ANSI to control and present {@link Telemetry}.
 *
 * @author brian.oliver
 * @since May-2025
 */
public class ANSIBasedTelemetryRecorder
    extends AbstractTelemetryRecorder
    implements Closeable {

    /**
     * The {@link Activity}s currently being undertaken according to the {@link TelemetryRecorder}.
     */
    private final ArrayList<Activity> activities;

    /**
     * The {@link CompletableFuture} to complete rendering.
     */
    private final CompletableFuture<Void> renderFuture;

    /**
     * Constructs a new {@link ANSIBasedTelemetryRecorder} with the specified {@link URI}.
     *
     * @param uri the {@link URI} of the {@link TelemetryRecorder}
     */
    protected ANSIBasedTelemetryRecorder(final URI uri) {
        super(uri);

        this.activities = new ArrayList<>();

        // establish an incomplete CompletableFuture to complete rendering
        this.renderFuture = new CompletableFuture<>();

        // start a VirtualThread to periodically render the activities
        Thread.startVirtualThread(() -> {
            while (!renderFuture.isDone()) {
                try {
                    Thread.sleep(1000);

                    render();
                } catch (final InterruptedException e) {
                    // ignore interrupts
                }
            }
        });
    }

    @Override
    public Meter commence(final int total, final NamedUnit unit, final String format, final Object... args) {

        // allow the super class to create the underlying Meter
        final var meter = super.commence(total, unit, format, args);

        // establish a ProgressBar based on the Meter (to track progress)
        final var progressBar = new ProgressBar(this, meter, total, unit, format, args);

        add(progressBar);

        return progressBar;
    }

    @Override
    synchronized protected <T extends Telemetry> T record(final T telemetry) {

        // ignore Progress Telemetry as it is rendered independently
        if (!(telemetry instanceof Progress)) {
            System.out.print(telemetry);
            System.out.println("\u001b[0K"); // clear the line after printing
            render();
        }

        return telemetry;
    }

    /**
     * Includes the specified {@link Activity} in the list of current activities.
     *
     * @param activity the {@link Activity} to add
     */
    synchronized void add(final Activity activity) {
        // first attempt to replace a null Activity with the new one
        for (int i = 0; i < this.activities.size(); i++) {
            if (this.activities.get(i) == null) {
                this.activities.set(i, activity);

                render();
                return;
            }
        }

        // otherwise, when no null Activity was found, add the new Activity to the list
        this.activities.add(activity);

        render();
    }

    /**
     * Removes the specified {@link Activity} from the list of current activities.
     *
     * @param activity the {@link Activity} to remove
     */
    synchronized void remove(final Activity activity) {
        // attempt to null out the Activity if it exists
        for (int i = 0; i < this.activities.size(); i++) {
            if (this.activities.get(i) == activity) {
                this.activities.set(i, null);
                break;
            }
        }

        render();

        // clear the list when it only contains null Activities
        if (this.activities.stream().allMatch(Objects::isNull)) {
            this.activities.clear();
        }
    }

    /**
     * Renders the current {@link Activity}s to the terminal.
     */
    synchronized void render() {

        // attempt to render only where there are activities
        if (!this.activities.isEmpty()) {
            // output the current non-null Activities
            for (final Activity activity : this.activities) {
                if (activity != null) {
                    // render the Activity
                    System.out.println(activity);
                } else {
                    // clear the line when the Activity is null
                    System.out.println();
                }
            }

            // move the cursor above the progress bars
            System.out.print("\u001b[" + (this.activities.size() + 1) + "A");
            System.out.flush();

            // scroll the normal text up (by outputting a form feed)
            System.out.println("\f");
            System.out.flush();

            System.out.print("\u001b[1A");
            System.out.flush();
        }
    }

    @Override
    public void close() {
        // complete the Future and thus terminate the Thread responsible for rendering
        this.renderFuture.complete(null);
    }

    @Override
    public TelemetryRecorderFactory factory() {
        return ANSIBasedTelemetryRecorder::of;
    }

    /**
     * Creates an {@link ANSIBasedTelemetryRecorder} with a default URI.
     *
     * @return a new {@link ANSIBasedTelemetryRecorder}
     */
    public static ANSIBasedTelemetryRecorder create() {
        return new ANSIBasedTelemetryRecorder(UniformResource.createURI("telemetry", "//ansi/"));
    }

    /**
     * Creates an {@link ANSIBasedTelemetryRecorder} with the specified {@link URI}.
     *
     * @param uri the {@link URI} of the {@link TelemetryRecorder}
     * @return a new {@link ANSIBasedTelemetryRecorder}
     */
    public static ANSIBasedTelemetryRecorder of(final URI uri) {
        return new ANSIBasedTelemetryRecorder(uri);
    }
}
