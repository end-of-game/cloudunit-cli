/*
 * LICENCE : CloudUnit is available under the GNU Affero General Public License : https://gnu.org/licenses/agpl.html
 *     but CloudUnit is licensed too under a standard commercial license.
 *     Please contact our sales team if you would like to discuss the specifics of our Enterprise license.
 *     If you are not sure whether the GPL is right for you,
 *     you can always test our software under the GPL and inspect the source code before you contact us
 *     about purchasing a commercial license.
 *
 *     LEGAL TERMS : "CloudUnit" is a registered trademark of Treeptik and can't be used to endorse
 *     or promote products derived from this project without prior written permission from Treeptik.
 *     Products or services derived from this software may not be called "CloudUnit"
 *     nor may "Treeptik" or similar confusing terms appear in their names without prior written permission.
 *     For any questions, contact us : contact@treeptik.fr
 */

package fr.treeptik.cloudunit.cli.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import fr.treeptik.cloudunit.cli.commands.ShellStatusCommand;
import fr.treeptik.cloudunit.cli.model.Snapshot;
import fr.treeptik.cloudunit.cli.processor.InjectLogger;
import fr.treeptik.cloudunit.cli.rest.JsonConverter;
import fr.treeptik.cloudunit.cli.rest.RestUtils;

@Component
public class SnapshotUtils {

    @InjectLogger
    private Logger log;

    @Autowired
    private AuthentificationUtils authentificationUtils;

    @Autowired
    private ApplicationUtils applicationUtils;

    @Autowired
    private RestUtils restUtils;

    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private ShellStatusCommand statusCommand;

    public String createSnapshot(String tag, String applicationName) {

        if (authentificationUtils.getMap().isEmpty()) {
            log.log(Level.SEVERE,
                    "You are not connected to CloudUnit host! Please use connect command");
            statusCommand.setExitStatut(1);
            return null;
        }

        if (fileUtils.isInFileExplorer()) {
            log.log(Level.SEVERE,
                    "You are currently in a container file explorer. Please exit it with close-explorer command");
            statusCommand.setExitStatut(1);
            return null;
        }

        if (applicationUtils.getApplication() == null
                && applicationName == null) {
            log.log(Level.SEVERE,
                    "No application is currently selected by the following command line : use <application name>");
            statusCommand.setExitStatut(1);
            return null;
        }

        if (applicationName != null) {
            applicationUtils.useApplication(applicationName);
        } else {
            applicationName = applicationUtils.getApplication().getName();
        }

        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("applicationName", applicationName);
            parameters.put("tag", tag);

            restUtils.sendPostCommand(
                    authentificationUtils.finalHost + "/snapshot",
                    authentificationUtils.getMap(), parameters).get("body");
            log.log(Level.INFO, "A new snapshot called " + tag
                    + " was successfully created.");

        } catch (ResourceAccessException e) {
            log.log(Level.SEVERE,
                    "The CLI can't etablished connexion with host servers. Please try later or contact an admin");
            statusCommand.setExitStatut(1);
            return null;
        } catch (ClientProtocolException e) {

        } catch (Exception e) {
            log.log(Level.SEVERE, "Severe error");
            statusCommand.setExitStatut(1);
            return null;
        }

        return null;
    }

    public String deleteSnapshot(String tag) {

        if (authentificationUtils.getMap().isEmpty()) {
            log.log(Level.SEVERE,
                    "You are not connected to CloudUnit host! Please use connect command");
            statusCommand.setExitStatut(1);
            return null;
        }
        if (fileUtils.isInFileExplorer()) {
            log.log(Level.SEVERE,
                    "You are currently in a container file explorer. Please exit it with close-explorer command");
            statusCommand.setExitStatut(1);
            return null;
        }
        try {
            restUtils.sendDeleteCommand(
                    authentificationUtils.finalHost + "/snapshot/" + tag,
                    authentificationUtils.getMap()).get("body");

        } catch (ResourceAccessException e) {
            log.log(Level.SEVERE,
                    "The CLI can't etablished connexion with host servers. Please try later or contact an admin");
            statusCommand.setExitStatut(1);
            return null;

        } catch (Exception e) {
            log.log(Level.SEVERE, "Severe error");
            statusCommand.setExitStatut(1);
            return null;
        }
        log.log(Level.INFO, "The snapshot " + tag
                + " was successfully deleted.");
        return null;
    }

    public List<Snapshot> listAllSnapshots() {
        List<Snapshot> listSnapshots;
        String json = null;
        if (authentificationUtils.getMap().isEmpty()) {
            log.log(Level.SEVERE,
                    "You are not connected to CloudUnit host! Please use connect command");
            statusCommand.setExitStatut(1);
            return null;
        }
        if (fileUtils.isInFileExplorer()) {
            log.log(Level.SEVERE,
                    "You are currently in a container file explorer. Please exit it with close-explorer command");
            statusCommand.setExitStatut(1);
            return null;
        }
        try {

            json = (String) restUtils.sendGetCommand(
                    authentificationUtils.finalHost + "/snapshot/list",
                    authentificationUtils.getMap()).get("body");
            statusCommand.setExitStatut(0);
        } catch (ResourceAccessException e) {
            log.log(Level.SEVERE,
                    "The CLI can't etablished connexion with host servers. Please try later or contact an admin");
            statusCommand.setExitStatut(1);
            return null;
        } catch (Exception e) {
            statusCommand.setExitStatut(1);
            return null;
        }
        listSnapshots = JsonConverter.getSnapshot(json);
        MessageConverter.buildListSnapshots(listSnapshots);
        statusCommand.setExitStatut(0);
        return listSnapshots;
    }

    public String clone(String applicationName, String tag) {
        if (authentificationUtils.getMap().isEmpty()) {
            log.log(Level.SEVERE,
                    "You are not connected to CloudUnit host! Please use connect command");
            statusCommand.setExitStatut(1);
            return null;
        }
        if (fileUtils.isInFileExplorer()) {
            log.log(Level.SEVERE,
                    "You are currently in a container file explorer. Please exit it with close-explorer command");
            statusCommand.setExitStatut(1);
            return null;
        }
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("applicationName", applicationName);
            parameters.put("tag", tag);
            restUtils.sendPostCommand(
                    authentificationUtils.finalHost + "/snapshot/clone",
                    authentificationUtils.getMap(), parameters).get("body");
            statusCommand.setExitStatut(0);
        } catch (ResourceAccessException e) {
            log.log(Level.SEVERE,
                    "The CLI can't etablished connexion with host servers. Please try later or contact an admin");
            statusCommand.setExitStatut(1);
            return null;
        } catch (Exception e) {
            statusCommand.setExitStatut(1);
            return null;
        }
        statusCommand.setExitStatut(0);
        return "Your application " + applicationName
                + " was successfully created.";
    }

}
