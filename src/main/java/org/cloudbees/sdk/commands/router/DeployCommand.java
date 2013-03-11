package org.cloudbees.sdk.commands.router;

import com.cloudbees.api.BeesClient;
import com.cloudbees.api.config.ConfigParameters;
import com.cloudbees.sdk.api.BeesAPIClient;
import com.cloudbees.sdk.cli.ACommand;
import com.cloudbees.sdk.cli.AbstractCommand;
import com.cloudbees.sdk.cli.BeesClientFactory;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import com.cloudbees.sdk.cli.CommandService;
import com.cloudbees.sdk.maven.RepositoryService;
import com.ning.http.util.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.kohsuke.args4j.Option;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;

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

    @Inject
    BeesClient bees;

    @Inject
    BeesClientFactory beesClientFactory;    // because this participates in the options parsing

    @Option(name="--router",metaVar="groupId:artifactId:packaging:classifier:version",
            usage="Override the router bundle to use (advanced)")
    public String routerGav = "com.cloudbees.router:cloudbees-router:zip:dist:LATEST";

    @Option(name="-a",aliases="--appid",usage="Application ID that the router will run as",required=true)
    public String appid;

    @Option(name="-s",aliases="--dsl",usage="Groovy router DSL script")
    public File dslScript;

    @Override
    public int main() throws Exception {
        // obtain the zip to deploy
        File zip = repository.resolveArtifact(new DefaultArtifact(routerGav)).getArtifact().getFile();

        // TODO: turn appId resolution into a reusable module

        // push the new configuration
        ConfigParameters config = bees.configurationParametersAsObject(appid, "application");
        config.getParameters().put("script.base64", Base64.encode(FileUtils.readFileToString(dslScript).getBytes()));
        bees.configurationParametersUpdate(appid, "application",config);

        ACommand deploy = commandService.getCommand("app:deploy");
        return deploy.run(Arrays.asList(appid,zip.getAbsolutePath()));
    }
}
