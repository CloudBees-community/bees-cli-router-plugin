package org.cloudbees.sdk.commands.router;

import com.cloudbees.api.BeesClient;
import com.cloudbees.api.config.ConfigParameters;
import com.cloudbees.api.config.ParameterMap;
import com.cloudbees.sdk.cli.ACommand;
import com.cloudbees.sdk.cli.AbstractCommand;
import com.cloudbees.sdk.cli.BeesClientFactory;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandService;
import com.cloudbees.sdk.maven.RepositoryService;
import com.ning.http.util.Base64;
import org.apache.commons.io.FileUtils;
import org.kohsuke.args4j.Option;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
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

        // push the new configuration
        configBuilder.update(appid);


        ACommand deploy = commandService.getCommand("app:deploy");
        return deploy.run(Arrays.asList("app:deploy",
            "-t","java",
            "-a",appid,
            "-R","classpath="+findBootJar(zip),
            "-R","class=org.codehaus.classworlds.Launcher",
            "-R","JAVA_OPTS=-Dcom.cloudbees.router.impl.Main=com.cloudbees.router.impl.ApplianceMain -Dapp.port=$app_port -Dapp.home=$app_dir/app -Dclassworlds.conf=$app_dir/app/boot/classworlds.conf",
            zip.getAbsolutePath()));
    }

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
