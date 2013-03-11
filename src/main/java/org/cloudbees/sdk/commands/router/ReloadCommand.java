package org.cloudbees.sdk.commands.router;

import com.cloudbees.sdk.cli.AbstractCommand;
import com.cloudbees.sdk.cli.BeesCommand;
import com.cloudbees.sdk.cli.CLICommand;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.kohsuke.args4j.Option;

import javax.inject.Inject;

/**
 * @author Kohsuke Kawaguchi
 */
@BeesCommand(group="Router")
@CLICommand("router:reload")
public class ReloadCommand extends AbstractCommand {
    @Option(name="-a",aliases="--appid",usage="Application ID that the router will run as",required=true)
    public String appid;

    @Inject
    ConfigBuilder configBuilder;

    @Override
    public int main() throws Exception {
        // TODO: turn appId resolution into a reusable module

        configBuilder.update(appid);

        String[] parts = appid.split("/");
        HttpClient client = new HttpClient();
        TickleMethod m = new TickleMethod("http://"+parts[1]+"."+parts[0]+".cloudbees.net");
        int r = client.executeMethod(m);
        if (r ==200) {
            System.out.println("Reloaded");
            return 0;
        } else {
            System.out.println("Failed to reload: "+r+" "+m.getStatusText());
            System.out.println(m.getResponseBodyAsString());
            return 1;
        }
    }

    private static final class TickleMethod extends GetMethod {
        private TickleMethod(String uri) {
            super(uri);
        }

        @Override
        public String getName() {
            return "TICKLE";
        }
    }
}
