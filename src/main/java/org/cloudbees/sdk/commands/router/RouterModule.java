/*
 * Copyright 2010-2013, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
