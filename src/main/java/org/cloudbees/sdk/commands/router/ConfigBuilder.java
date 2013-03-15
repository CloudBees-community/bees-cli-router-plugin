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
import com.cloudbees.api.config.ConfigParameters;
import com.cloudbees.api.config.ParameterMap;
import com.cloudbees.sdk.cli.BeesClientFactory;
import com.cloudbees.sdk.cli.CommandScope;
import com.cloudbees.sdk.cli.HasOptions;
import com.ning.http.util.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.Option;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Encapsulates the logic of passing router configuration via configuration parameters.
 *
 * @author Kohsuke Kawaguchi
 */
@CommandScope
public class ConfigBuilder implements HasOptions {
    @Inject
    BeesClient bees;

    @Inject
    BeesClientFactory beesClientFactory;    // because this participates in the options parsing

    @Option(name="-f",aliases="--dsl",usage="Groovy router DSL script")
    public File dslScript;

    public void update(String appid) throws Exception {
        ConfigParameters config = bees.configurationParametersAsObject(appid, "application");
        ParameterMap p = cleanOld(config.getParameters());
        buildParameters(appid, p);

        p = cleanOld(config.getRuntimeParameters());
        buildRuntimeParameters(p);

        bees.configurationParametersUpdate(appid, "application",config);
    }

    public void buildParameters(String appid, Map<String,String> p) throws IOException {
        p.put("router.appId", appid);
        p.put("bees.api.server", beesClientFactory.getApiUrl());
        p.put("bees.api.key", beesClientFactory.key);
        p.put("bees.api.secret", new String(Hex.encodeHex(beesClientFactory.secret.getBytes("UTF-8")))); // '=' in the parameter value seems to confuse the java stack
    }

    public void buildRuntimeParameters(Map<String,String> p) throws IOException {
        p.put("router.script.base64", Base64.encode(FileUtils.readFileToString(dslScript).getBytes()));
    }

    /**
     * Remove any "router.*" values from config.
     *
     * When different versions of the router plugin is used on the same app, this prevents
     * left-over from earlier versions to affect the runtime.
     */
    private ParameterMap cleanOld(ParameterMap p) {
        Iterator<Entry<String, String>> itr = p.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, String> e = itr.next();
            if (e.getKey().startsWith("router."))
                itr.remove();
        }
        return p;
    }
}
