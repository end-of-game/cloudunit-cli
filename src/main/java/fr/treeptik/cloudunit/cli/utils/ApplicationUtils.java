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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import fr.treeptik.cloudunit.cli.commands.ShellStatusCommand;
import fr.treeptik.cloudunit.cli.model.Application;
import fr.treeptik.cloudunit.cli.model.LogUnit;
import fr.treeptik.cloudunit.cli.processor.InjectLogger;
import fr.treeptik.cloudunit.cli.rest.JsonConverter;
import fr.treeptik.cloudunit.cli.rest.RestUtils;
import fr.treeptik.cloudunit.cli.shell.CloudUnitPromptProvider;

@Component
public class ApplicationUtils {

	@InjectLogger
	private Logger log;

	@Autowired
	private UrlLoader urlLoader;

	@Autowired
	private AuthentificationUtils authentificationUtils;

	@Autowired
	private ShellStatusCommand statusCommand;

	@Autowired
	private RestUtils restUtils;

	@Autowired
	private CheckUtils checkUtils;

	@Autowired
	private ModuleUtils moduleUtils;

	@Autowired
	private CloudUnitPromptProvider clPromptProvider;

	@Autowired
	private FileUtils fileUtils;

	private Application application;

	private Integer loop = 0;

	public String getInformations() {
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

		if (application == null) {
			log.log(Level.SEVERE,
					"No application is currently selected by use <application name>");
			statusCommand.setExitStatut(1);
			return null;
		}

		this.useApplication(application.getName());
		String dockerManagerIP = application.getManagerIp();
		statusCommand.setExitStatut(0);

		MessageConverter.buildApplicationMessage(this.getApplication(),
				dockerManagerIP);
		return null;
	}

	public String useApplication(String applicationName) {
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

		if (!checkUtils.checkApplicationExist(applicationName)) {
			log.log(Level.SEVERE, applicationName
					+ " is not found in your application's list");
			statusCommand.setExitStatut(1);
			return null;
		}

		try {

			json = restUtils.sendGetCommand(
					authentificationUtils.finalHost
							+ urlLoader.actionApplication + applicationName,
					authentificationUtils.getMap()).get("body");
			statusCommand.setExitStatut(0);
		}

		catch (ResourceAccessException e) {
			log.log(Level.SEVERE,
					"The CLI can't etablished connexion with host servers. Please try later or contact an admin");
			return null;

		} catch (Exception e) {
			statusCommand.setExitStatut(1);
			return json;
		}
		moduleUtils.setApplicationName(applicationName);
		this.setApplication(JsonConverter.getApplication(json));
		clPromptProvider.setPrompt("cloudunit-" + applicationName + "> ");
		return "Current application : " + getApplication().getName();
	}

	public String createApp(String applicationName, String serverName) {
		String response = null;
		if (authentificationUtils.getMap().isEmpty()) {
			log.log(Level.SEVERE,
					"You are not connected to CloudUnit host! Please use connect command");
			statusCommand.setExitStatut(1);
			return "";
		}

		if (fileUtils.isInFileExplorer()) {
			log.log(Level.SEVERE,
					"You are currently in a container file explorer. Please exit it with close-explorer command");
			statusCommand.setExitStatut(1);
			return null;
		}

		if (checkUtils.checkImageNoExist(serverName)) {
			statusCommand.setExitStatut(1);
			return null;
		}

		try {
			Map<String, String> parameters = new HashMap<>();
			parameters.put("applicationName", applicationName);
			parameters.put("serverName", serverName);

			restUtils.sendPostCommand(
					authentificationUtils.finalHost
							+ urlLoader.actionApplication,
					authentificationUtils.getMap(), parameters).get("body");

			statusCommand.setExitStatut(0);

			response = "Your application " + applicationName
					+ " is currently being installed";

		} catch (ResourceAccessException e) {
			log.log(Level.SEVERE,
					"The CLI can't etablished connexion with host servers. Please try later or contact an admin");
			statusCommand.setExitStatut(1);
			return null;
		} catch (Exception e) {
			statusCommand.setExitStatut(1);
			return null;
		}

		this.useApplication(applicationName);

		return response;
	}

	public String rmApp(String applicationName, Boolean scriptUsage) {

		String response = null;
		String confirmation = "";

		if (loop == 0) {
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

			if (applicationName != null) {
				this.useApplication(applicationName);
				if (this.getApplication() == null) {
					statusCommand.setExitStatut(1);
					return null;
				}
			}
			if (this.getApplication() == null) {
				log.log(Level.SEVERE,
						"No application is currently selected by the following command line : use <application name>");
				statusCommand.setExitStatut(1);
				return null;
			}
		}

		// Enter the non interactive mode (for script)
		if (scriptUsage) {
			restUtils.sendDeleteCommand(
					authentificationUtils.finalHost
							+ urlLoader.actionApplication
							+ this.getApplication().getName(),
					authentificationUtils.getMap()).get("body");
			response = "Your application " + this.getApplication().getName()
					+ " is currently being removed";
			resetPrompt();
			statusCommand.setExitStatut(0);
			this.setApplication(null);
			return response;
		}

		// for human users

		if (loop <= 3) {
			loop++;
			Scanner scanner = new Scanner(System.in);
			log.log(Level.WARNING,
					"Confirm the suppression of your application : "
							+ this.getApplication().getName()
							+ " - (yes/y or no/n)");
			confirmation = scanner.nextLine();
			try {

				switch (confirmation.toLowerCase()) {
				case "yes":
				case "y":

					restUtils.sendDeleteCommand(
							authentificationUtils.finalHost
									+ urlLoader.actionApplication
									+ this.getApplication().getName(),
							authentificationUtils.getMap()).get("body");
					response = "Your application "
							+ this.getApplication().getName()
							+ " is currently being removed";
					resetPrompt();
					statusCommand.setExitStatut(0);
					break;

				case "no":
				case "n":
					this.setApplication(null);
					resetPrompt();
					statusCommand.setExitStatut(0);

					break;

				default:

					if (loop >= 3) {
						log.log(Level.SEVERE,
								"sorry 3 tries is the limit, you seem too tired to take a decision so important as delete an application, take a break !!!");
						this.setApplication(null);
						resetPrompt();
						loop = 0;
						scanner.close();
						return null;
					}
					log.log(Level.SEVERE,
							"confirmation response are yes/y or no/n ");
					scanner.close();
					return this.rmApp(applicationName, scriptUsage);
				}
			} catch (ResourceAccessException e) {
				log.log(Level.SEVERE,
						"The CLI can't etablished connexion with host servers. Please try later or contact an admin");
				statusCommand.setExitStatut(1);
				scanner.close();
				return null;
			}

			this.setApplication(null);
			scanner.close();
			loop = 0;
		}
		return response;

	}

	public String startApp(String applicationName) {
		String response = null;

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

		if (this.getApplication() == null && applicationName == null) {
			log.log(Level.SEVERE,
					"No application is currently selected by the following command line : use <application name>");
			statusCommand.setExitStatut(1);
			return null;
		}

		if (applicationName != null) {
			this.useApplication(applicationName);
		} else {
			applicationName = this.getApplication().getName();
		}

		try {
			Map<String, String> parameters = new HashMap<>();
			parameters.put("applicationName", applicationName);
			restUtils.sendPostCommand(
					authentificationUtils.finalHost
							+ urlLoader.actionApplication + urlLoader.start,
					authentificationUtils.getMap(), parameters).get("body");
			response = "Your application " + applicationName
					+ " is currently being started";
			statusCommand.setExitStatut(0);

		} catch (ResourceAccessException | ClientProtocolException e) {
			log.log(Level.SEVERE,
					"The CLI can't etablished connexion with host servers. Please try later or contact an admin");
			statusCommand.setExitStatut(1);
			return null;
		}

		return response;
	}

	public String stopApp(String applicationName) {

		String response = null;

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

		if (this.getApplication() == null && applicationName == null) {
			log.log(Level.SEVERE,
					"No application is currently selected by the following command line : use <application name>");
			statusCommand.setExitStatut(1);
			return null;
		}

		if (applicationName != null) {
			this.useApplication(applicationName);
		} else {
			applicationName = this.getApplication().getName();
		}

		try {

			Map<String, String> parameters = new HashMap<>();
			parameters.put("applicationName", applicationName);

			restUtils.sendPostCommand(
					authentificationUtils.finalHost
							+ urlLoader.actionApplication + urlLoader.stop,
					authentificationUtils.getMap(), parameters).get("body");
			response = "Your application " + applicationName
					+ " is currently being stopped";
			statusCommand.setExitStatut(0);

		} catch (ResourceAccessException | ClientProtocolException e) {
			log.log(Level.SEVERE,
					"The CLI can't etablished connexion with host servers. Please try later or contact an admin");
			statusCommand.setExitStatut(1);
			return null;
		}

		return response;
	}

	public List<Application> listAllApps() {
		List<Application> listApplications;
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
					authentificationUtils.finalHost
							+ urlLoader.listAllApplications,
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
		listApplications = JsonConverter.getApplications(json);
		statusCommand.setExitStatut(0);
		return listApplications;
	}

	public String listAll() {
		List<Application> listApplications = this.listAllApps();
		if (listApplications != null) {
			MessageConverter.buildListApplications(this.listAllApps());
		}
		return null;
	}

	public String deployFromAWar(File path, boolean openBrowser)
			throws MalformedURLException, URISyntaxException {

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
		if (path == null) {
			log.log(Level.SEVERE,
					"You must precise the file path with -p option");
			statusCommand.setExitStatut(1);
			return null;
		}

		if (this.getApplication() == null) {
			log.log(Level.SEVERE,
					"No application is currently selected by use <application name>");
			statusCommand.setExitStatut(1);
			return null;
		} else {
			// refresh application informations
			this.useApplication(this.getApplication().getName());

			try {
				File file = path;
				FileInputStream fileInputStream = new FileInputStream(file);
				fileInputStream.available();
				fileInputStream.close();
				FileSystemResource resource = new FileSystemResource(file);
				Map<String, Object> params = new HashMap<>();
				params.put("file", resource);
				params.putAll(authentificationUtils.getMap());
				String body = (String) restUtils.sendPostForUpload(
						authentificationUtils.finalHost
								+ urlLoader.actionApplication
								+ this.getApplication().getName() + "/deploy",
						params).get("body");
				statusCommand.setExitStatut(0);

				if (!body.equalsIgnoreCase("") && openBrowser) {
					DesktopAPI.browse(new URL(this.application.getLocation())
							.toURI());
					log.log(Level.INFO, "War deployed - Access on "
							+ this.application.getLocation());
				}

			} catch (ResourceAccessException e) {
				log.log(Level.SEVERE,
						"The CLI can't etablished connexion with host servers. Please try later or contact an admin");
				statusCommand.setExitStatut(1);
				return null;
			} catch (IOException e) {
				log.log(Level.SEVERE, "File not found! Check the path file");
				statusCommand.setExitStatut(1);
				return null;
			} catch (Exception e) {
				statusCommand.setExitStatut(1);
				return null;
			}
		}

		return null;
	}

	public String getLogs(Integer numberOfLines) {
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

		if (application == null) {
			log.log(Level.SEVERE,
					"No application is currently selected by use <application name>");
			statusCommand.setExitStatut(1);
			return null;
		}
		try {

			String body = restUtils.sendGetCommand(
					authentificationUtils.finalHost + urlLoader.logs
							+ application.getName() + "/rows/" + numberOfLines,
					authentificationUtils.getMap()).get("body");
			statusCommand.setExitStatut(0);

			List<LogUnit> logsLogUnits = JsonConverter.getLogUnit(body);
			for (LogUnit logUnit : logsLogUnits) {
				log.log(Level.FINE,
						logUnit.getLevel() + " " + logUnit.getDate() + " "
								+ logUnit.getMessage());
			}

		} catch (ResourceAccessException e) {
			log.log(Level.SEVERE,
					"The CLI can't etablished connexion with host servers. Please try later or contact an admin");
			statusCommand.setExitStatut(1);
			return null;
		} catch (Exception e) {
			statusCommand.setExitStatut(1);
			return null;
		}
		return null;

	}

	public String addNewAlias(String applicationName, String alias) {
		String response = null;

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
		if (this.getApplication() == null && applicationName == null) {
			log.log(Level.SEVERE,
					"No application is currently selected by the following command line : use <application name>");
			statusCommand.setExitStatut(1);
			return null;
		}

		if (applicationName != null) {
			this.useApplication(applicationName);
		} else {
			applicationName = this.getApplication().getName();
		}

		try {
			Map<String, String> parameters = new HashMap<>();
			parameters.put("applicationName", applicationName);
			parameters.put("alias", alias);
			restUtils.sendPostCommand(
					authentificationUtils.finalHost
							+ urlLoader.actionApplication + "/alias",
					authentificationUtils.getMap(), parameters).get("body");
			statusCommand.setExitStatut(0);
			response = "Your alias " + alias
					+ " has been successfully added to " + applicationName;
		} catch (ResourceAccessException e) {
			log.log(Level.SEVERE,
					"The CLI can't etablished connexion with host servers. Please try later or contact an admin");
			statusCommand.setExitStatut(1);
			return null;
		} catch (ClientProtocolException e) {

		}

		return response;
	}

	public String listAllAliases(String applicationName) {
		String response = null;

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

		if (this.getApplication() == null && applicationName == null) {
			log.log(Level.SEVERE,
					"No application is currently selected by the following command line : use <application name>");
			statusCommand.setExitStatut(1);
			return null;
		}

		if (applicationName != null) {
			this.useApplication(applicationName);
		} else {
			applicationName = this.getApplication().getName();
		}

		try {

			response = restUtils.sendGetCommand(
					authentificationUtils.finalHost
							+ urlLoader.actionApplication + applicationName
							+ "/alias", authentificationUtils.getMap()).get(
					"body");

			MessageConverter.buildListAliases(JsonConverter
					.getAliases(response));

			statusCommand.setExitStatut(0);

		} catch (ResourceAccessException e) {
			log.log(Level.SEVERE,
					"The CLI can't etablished connexion with host servers. Please try later or contact an admin");
			statusCommand.setExitStatut(1);
			return null;
		}

		return null;
	}

	public String removeAlias(String applicationName, String alias) {

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
		if (this.getApplication() == null && applicationName == null) {
			log.log(Level.SEVERE,
					"No application is currently selected by the following command line : use <application name>");
			statusCommand.setExitStatut(1);
			return null;
		}

		if (applicationName != null) {
			this.useApplication(applicationName);
		} else {
			applicationName = this.getApplication().getName();
		}

		try {

			restUtils
					.sendDeleteCommand(
							authentificationUtils.finalHost
									+ urlLoader.actionApplication
									+ applicationName + "/alias/" + alias,
							authentificationUtils.getMap()).get("body");
			log.log(Level.INFO, "This alias has successful been deleted");

			statusCommand.setExitStatut(0);

		} catch (ResourceAccessException e) {
			log.log(Level.SEVERE,
					"The CLI can't etablished connexion with host servers. Please try later or contact an admin");
			statusCommand.setExitStatut(1);
			return null;
		}

		return null;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public void resetPrompt() {
		clPromptProvider.setPrompt("cloudunit> ");
	}
}
