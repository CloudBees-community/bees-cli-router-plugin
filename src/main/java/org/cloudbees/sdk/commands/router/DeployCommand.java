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

import com.cloudbees.sdk.cli.ACommand;
import com.cloudbees.sdk.cli.AbstractCommand;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandService;
import com.cloudbees.sdk.maven.RepositoryService;
import org.kohsuke.args4j.Option;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Command to deploy the new router application.
 *
 * @author Kohsuke Kawaguchi
 */
@BeesCommand(group="Router")
@CLICommand("router:deploy")
public class DeployCommand extends AbstractCommand {
    @Inject
    CommandService commandService;

    @Inject
    RepositoryService repository;

    @Option(name="--router",metaVar="groupId:artifactId:packaging:classifier:version",
            usage="Override the router bundle to use (advanced)")
    public String routerGav = "com.cloudbees.router:cloudbees-router:zip:dist:LATEST";

    @Option(name="-a",aliases="--appid",usage="Application ID that the router will run as",required=true)
    public String appid;

    @Inject
    ConfigBuilder configBuilder;

    @Override
    public int main() throws Exception {
        // obtain the zip to deploy
        File zip = repository.resolveArtifact(new DefaultArtifact(routerGav)).getArtifact().getFile();

        // TODO: turn appId resolution into a reusable module

        List<String> args = new ArrayList<String>(Arrays.asList("app:deploy",
                "-t", "java",
                "-a", appid,
                "-R", "classpath=" + findBootJar(zip),
                "-R", "class=org.codehaus.classworlds.Launcher",
                "-R", "JAVA_OPTS=-Dcom.cloudbees.router.impl.Main=com.cloudbees.router.impl.ApplianceMain -Dapp.port=$app_port -Dapp.home=$app_dir/app -Dclassworlds.conf=$app_dir/app/boot/classworlds.conf"));

        Map<String,String> m = new HashMap<String, String>();
        configBuilder.buildParameters(appid, m);
        addMap(args,"-P",m);

        m = new HashMap<String, String>();
        configBuilder.buildRuntimeParameters(m);
        addMap(args,"-R",m);

        args.add(zip.getAbsolutePath());

        ACommand deploy = commandService.getCommand("app:deploy");
        return deploy.run(args);
    }

    private void addMap(List<String> args, String prefix, Map<String, String> m) {
        for (Entry<String, String> e : m.entrySet()) {
            args.add(prefix);
            args.add(e.getKey()+'='+e.getValue());
        }
    }

    /**
     * Locates the bootstrap classworlds jar from the bundle
     */
    private String findBootJar(File zip) throws IOException {
        ZipFile z = new ZipFile(zip);
        try {
            Enumeration<? extends ZipEntry> e = z.entries();
            while (e.hasMoreElements()) {
                ZipEntry n = e.nextElement();
                if (n.getName().matches("boot/plexus-classworlds-.+\\.jar"))
                    return n.getName();
            }
            throw new IllegalArgumentException("No bootstrap classworlds jar found");
        } finally {
            z.close();
        }
    }
}
