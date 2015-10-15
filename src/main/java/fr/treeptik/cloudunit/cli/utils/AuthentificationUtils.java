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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.treeptik.cloudunit.cli.shell.CloudUnitPromptProvider;

import jline.console.ConsoleReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import fr.treeptik.cloudunit.cli.commands.ShellStatusCommand;
import fr.treeptik.cloudunit.cli.processor.InjectLogger;
import fr.treeptik.cloudunit.cli.rest.RestUtils;

@Component
public class AuthentificationUtils {

    public String finalHost;
    @InjectLogger
    private Logger log;
    private Map<String, Object> map = new HashMap<>();
    @Value("${host}")
    private String defaultHost;
    @Value("${manager.version}")
    private String apiVersion;
    @Autowired
    private UrlLoader urlLoader;
    @Autowired
    private CloudUnitPromptProvider clPromptProvider;
    @Autowired
    private ShellStatusCommand statusCommand;
    @Autowired
    private RestUtils restUtils;
    @Autowired
    private ApplicationUtils applicationUtils;
    @Autowired
    private FileUtils fileUtils;
    private Integer loop = 0;

    /**
     * Methode de connexion
     *
     * @param login
     * @param password
     * @param selectedHost
     * @return
     */
    public String connect(String login, String password, String selectedHost) {

        if (!map.isEmpty()) {
            statusCommand.setExitStatut(0);
            return (ANSIConstants.ANSI_PURPLE + "You are already connected to CloudUnit servers" + ANSIConstants.ANSI_RESET);
        }

        if (fileUtils.isInFileExplorer()) {
            log.log(Level.SEVERE,
                    "You are currently in a container file explorer. Please exit it with close-explorer command");
            statusCommand.setExitStatut(1);
            return null;
        }

        try {
            loop++;

            if (password.equalsIgnoreCase("")) {
                log.log(Level.INFO, "Enter your password : ");
                password = new ConsoleReader()
                        .readLine(new Character('*'));
            }
            Map<String, Object> loginInfo = new HashMap<>();
            loginInfo.put("login", login);
            loginInfo.put("password", password);

            // check the host

            if (!selectedHost.isEmpty()) {
                log.log(Level.INFO, "Trying to connect to " + selectedHost);
                finalHost = selectedHost;

            } else {
                log.log(Level.INFO,
                        "Trying to connect to default CloudUnit host...");
                finalHost = defaultHost;
            }

            // trying to connect with host manager
            String urlToCall = finalHost + urlLoader.connect;
            restUtils.connect(urlToCall, loginInfo).get("body");
            applicationUtils.setApplication(null);
            resetPrompt();
            statusCommand.setExitStatut(0);

        } catch (ResourceAccessException e) {
            log.log(Level.SEVERE,
                    "The CLI can't etablished connection with host servers. Please try later or contact a");
            statusCommand.setExitStatut(1);
            return null;
        } catch (Exception e) {
            if (loop >= 3) {
                return null;
            }
            statusCommand.setExitStatut(1);
            password = "";
            return this.connect(login, password, selectedHost);
        }

        map.put("login", login);
        map.put("password", password);
        loop = 0;

        Boolean checkingAPI = this.checkAPIVersion(finalHost);

        // Test if connection was successful
        /*
        if (checkingAPI == null) {
			restUtils.localContext = null;
			this.map.clear();
			return null;
		}
		// Test if CLI is compatible to CU API
		if (!checkingAPI) {
			log.log(Level.SEVERE, "Please update your CLI");
			restUtils.localContext = null;
			this.map.clear();
			return null;
		}
		*/
        return "Connection established";

    }

    private Boolean checkAPIVersion(String host) {
        String response = restUtils.sendGetCommand(
                host + urlLoader.imageFind + "/version", map).get("body");

        if (response == null)
            return null;
        if (response.equalsIgnoreCase(apiVersion))
            return true;
        return false;
    }

    /**
     * Appel de l'url spring-secu pour suppression session côté serveur
     */
    public String disconnect() {
        if (fileUtils.isInFileExplorer()) {
            log.log(Level.SEVERE,
                    "You are currently in a container file explorer. Please exit it with close-explorer command");
            statusCommand.setExitStatut(1);
            return null;
        }
        restUtils.sendGetCommand(finalHost + "/user/logout", map);
        resetPrompt();

        return null;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public void resetPrompt() {
        clPromptProvider.setPrompt("cloudunit> ");
    }

}
