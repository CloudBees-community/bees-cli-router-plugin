package org.cloudbees.sdk.commands.router;

import com.cloudbees.api.BeesClient;
import com.cloudbees.sdk.cli.BeesClientFactory;
import com.cloudbees.sdk.cli.CLIModule;
import com.cloudbees.sdk.extensibility.Extension;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.io.IOException;

/**
 * Defines additional bindings for the router commands.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class RouterModule extends AbstractModule implements CLIModule {
    protected void configure() {
    }

    @Provides
    public BeesClient beesClient(BeesClientFactory factory) throws IOException {
        return factory.get(BeesClient.class);
    }
}
