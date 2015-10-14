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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import fr.treeptik.cloudunit.cli.commands.ShellStatusCommand;
import fr.treeptik.cloudunit.cli.model.Application;
import fr.treeptik.cloudunit.cli.model.ContainerUnit;
import fr.treeptik.cloudunit.cli.model.LogUnit;
import fr.treeptik.cloudunit.cli.processor.InjectLogger;
import fr.treeptik.cloudunit.cli.rest.JsonConverter;
import fr.treeptik.cloudunit.cli.rest.RestUtils;
import fr.treeptik.cloudunit.cli.shellcustom.CloudUnitPromptProvider;

@Component
public class LogsUtils {

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
	private ApplicationUtils applicationUtils;

	@Autowired
	private FileUtils fileUtils;

	private String containerResponse;

	public static volatile boolean killThread = false;

	@Autowired
	private CloudUnitPromptProvider clPromptProvider;

	public String getLogs(Integer numberOfLines, String containerName) {

		if (numberOfLines == null) {
			numberOfLines = 100;
		}

		Application application = applicationUtils.getApplication();

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

		// get all container

		String url = authentificationUtils.finalHost
				+ urlLoader.actionApplication + application.getName()
				+ "/containers";
		try {
			containerResponse = restUtils.sendGetCommand(url,
					authentificationUtils.getMap()).get("body");

			if (!containerResponse.contains(("name\":\""
					+ application.getUser().getFirstName()
					+ application.getUser().getLastName() + "-"
					+ application.getName() + "-" + containerName)
					.toLowerCase())) {
				log.log(Level.INFO, "Please set a correct container name : ");
				shownAllAvailableContainerName();
				return null;
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

		try {

			String body = restUtils
					.sendGetCommand(
							authentificationUtils.finalHost
									+ urlLoader.logs
									+ application.getName()
									+ "/container/"
									+ getContainerIDFromContainerName(application
											.getUser().getFirstName()
											+ application.getUser()
													.getLastName()
											+ "-"
											+ application.getName()
											+ "-"
											+ containerName) + "/rows/"
									+ numberOfLines,
							authentificationUtils.getMap()).get("body");
			statusCommand.setExitStatut(0);

			List<LogUnit> logsLogUnits = JsonConverter.getLogUnit(body);
			Collections.sort(logsLogUnits, new Comparator<LogUnit>() {
				@Override
				public int compare(LogUnit lu1, LogUnit lu2) {
					int result = 0;
					try {
						result = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss")
								.parse(lu1.getDate()).compareTo(
										new SimpleDateFormat(
												"dd-MM-YYYY HH:mm:ss")
												.parse(lu2.getDate()));
					} catch (ParseException e) {
						log.log(Level.SEVERE, e.getLocalizedMessage());
					}
					return result;
				}
			});

			for (LogUnit logUnit : logsLogUnits) {
				log.log(Level.INFO,
						logUnit.getSource() + " " + logUnit.getDate() + " "
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

	public void shownAllAvailableContainerName() {
		Application application = applicationUtils.getApplication();
		MessageConverter.buildListContainerUnits(
				JsonConverter.getContainerUnits(containerResponse), "servers",
				application);
	}

	private String getContainerIDFromContainerName(String containerName) {
		List<ContainerUnit> containerUnits = new ArrayList<>();
		containerUnits.addAll(JsonConverter
				.getContainerUnits(containerResponse));

		for (ContainerUnit containerUnit : containerUnits) {
			if (containerUnit.getName().equalsIgnoreCase(containerName)) {
				return containerUnit.getId();
			}
		}
		return containerName;
	}

}