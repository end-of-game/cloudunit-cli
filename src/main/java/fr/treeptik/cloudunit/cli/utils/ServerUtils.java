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

import fr.treeptik.cloudunit.cli.commands.ShellStatusCommand;
import fr.treeptik.cloudunit.cli.exception.ManagerResponseException;
import fr.treeptik.cloudunit.cli.processor.InjectLogger;
import fr.treeptik.cloudunit.cli.rest.RestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ServerUtils
        implements CommandMarker {

    @InjectLogger
    private Logger log;

    @Autowired
    private AuthentificationUtils authentificationUtils;

    @Autowired
    private ApplicationUtils applicationUtils;

    @Autowired
    private ShellStatusCommand statusCommand;

    @Autowired
    private RestUtils restUtils;

    @Autowired
    private FileUtils fileUtils;

    private List<String> availableJavaVersion = Arrays.asList(new String[]{"jdk1.7.0_55", "jdk1.8.0_25"});

    /**
     * @param memory
     * @return
     */
    public String changeMemory(String memory) {
        String checkResponse = applicationUtils.checkAndRejectIfError(null);
        if (checkResponse != null) {
            return checkResponse;
        }
        List<String> values = Arrays.asList("512", "1024", "2048", "3072");
        if (!values.contains(memory)) {
            log.log(Level.SEVERE, "The memory value you have put is not authorized (512, 1024, 2048, 3072)");
            statusCommand.setExitStatut(1);
            return null;
        }

        try {

            Map<String, String> parameters = new HashMap<>();
            parameters.put("applicationName", applicationUtils.getApplication().getName());
            parameters.put("jvmMemory", memory);
            parameters.put("jvmRelease", applicationUtils.getApplication().getJvmRelease());
            parameters.put("jvmOptions",
                    applicationUtils.getApplication().getServers().get(0).getJvmOptions().toString());
            restUtils.sendPutCommand(authentificationUtils.finalHost + "/server/configuration/jvm",
                    authentificationUtils.getMap(), parameters).get("body");
            applicationUtils.useApplication(applicationUtils.getApplication().getName());

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
        return "Change memory on " + applicationUtils.getApplication().getName() + " successful";
    }

    /**
     * Add an option for JVM
     *
     * @param opts
     * @return
     */
    public String addOpts(String opts) {

        String checkResponse = applicationUtils.checkAndRejectIfError(null);
        if (checkResponse != null) {
            return checkResponse;
        }

        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("applicationName", applicationUtils.getApplication().getName());
            parameters.put("jvmOptions", opts);
            parameters.put("jvmRelease", applicationUtils.getApplication().getJvmRelease());
            parameters.put("jvmMemory",
                    applicationUtils.getApplication().getServers().get(0).getJvmMemory().toString());
            restUtils.sendPutCommand(authentificationUtils.finalHost + "/server/configuration/jvm",
                    authentificationUtils.getMap(), parameters).get("body");
            applicationUtils.useApplication(applicationUtils.getApplication().getName());

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
        return "Add java options to " + applicationUtils.getApplication().getName() + " application successfully";
    }

    /**
     * Change JVM Release
     *
     * @param applicationName
     * @param jvmRelease
     * @return
     */
    public String changeJavaVersion(String applicationName, String jvmRelease) {

        String checkResponse = applicationUtils.checkAndRejectIfError(applicationName);
        if (checkResponse != null) {
            return checkResponse;
        }

        if (applicationName != null) {
            applicationUtils.useApplication(applicationName);
        } else {
            applicationName = applicationUtils.getApplication().getName();
        }

        if (!availableJavaVersion.contains(jvmRelease)) {
            log.log(Level.SEVERE, "The specified java version is not available");
            statusCommand.setExitStatut(1);
            return null;
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("applicationName", applicationName);
        parameters.put("jvmRelease", jvmRelease);
        parameters.put("jvmMemory",
                applicationUtils.getApplication().getServers().get(0).getJvmMemory().toString());
        parameters.put("jvmOptions",
                applicationUtils.getApplication().getServers().get(0).getJvmOptions().toString());
        try {
            restUtils.sendPutCommand(authentificationUtils.finalHost + "/server/configuration/jvm",
                    authentificationUtils.getMap(), parameters).get("body");
        } catch (ManagerResponseException e) {
            e.printStackTrace();
        }
        log.log(Level.INFO, "Your java version has been successfully changed");
        applicationUtils.useApplication(applicationName);
        statusCommand.setExitStatut(0);


        return null;
    }

    /**
     * TODO
     *
     * @param applicationName
     * @param portToOpen
     * @param alias
     * @return
     */
    public String openPort(String applicationName, String portToOpen, String alias) {

        try {
            Integer.parseInt(portToOpen);

        } catch (NumberFormatException e) {
            log.log(Level.SEVERE, "The port is not correct");
            statusCommand.setExitStatut(1);
            return null;
        }

        String checkResponse = applicationUtils.checkAndRejectIfError(applicationName);
        if (checkResponse != null) {
            return checkResponse;
        }

        if (applicationName != null) {
            applicationUtils.useApplication(applicationName);
        } else {
            applicationName = applicationUtils.getApplication().getName();
        }
        if (Integer.parseInt(portToOpen) < 1024) {
            log.log(Level.SEVERE, "You must open a port bigger than 1024");
            statusCommand.setExitStatut(1);
            return null;
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("applicationName", applicationName);
        parameters.put("portToOpen", portToOpen);
        parameters.put("alias", alias);

        try {
            restUtils.sendPostCommand(authentificationUtils.finalHost + "/server/ports/open",
                    authentificationUtils.getMap(), parameters).get("body");
        } catch (ManagerResponseException e) {
            e.printStackTrace();
        }
        log.log(Level.INFO, "The port " + portToOpen + " was been successfully opened on " + applicationName);
        statusCommand.setExitStatut(0);


        return null;
    }

}
