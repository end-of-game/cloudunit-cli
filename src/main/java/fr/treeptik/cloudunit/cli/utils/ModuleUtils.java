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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import fr.treeptik.cloudunit.cli.commands.ShellStatusCommand;
import fr.treeptik.cloudunit.cli.model.Module;
import fr.treeptik.cloudunit.cli.processor.InjectLogger;
import fr.treeptik.cloudunit.cli.rest.RestUtils;

@Component
public class ModuleUtils {

	@Autowired
	private ApplicationUtils applicationUtils;

	@Autowired
	private AuthentificationUtils authentificationUtils;

	@Autowired
	private CheckUtils checkUtils;

	@Autowired
	private UrlLoader urlLoader;

	@InjectLogger
	private Logger log;

	@Autowired
	private ShellStatusCommand statusCommand;

	@Autowired
	private RestUtils restUtils;

	private String applicationName;

	@Autowired
	private FileUtils fileUtils;

	public String getListModules() {
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
		if (applicationUtils.getApplication() == null) {
			log.log(Level.SEVERE,
					"No application is currently selected by use <application name>");
			statusCommand.setExitStatut(1);
			return null;
		}

		String dockerManagerIP = applicationUtils.getApplication()
				.getManagerIp();
		statusCommand.setExitStatut(0);

		MessageConverter.buildLightModuleMessage(
				applicationUtils.getApplication(), dockerManagerIP);
		return null;
	}

	public String addModule(final String imageName, final File script) {
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

		if (applicationUtils.getApplication() == null) {
			log.log(Level.SEVERE,
					"No application is currently selected by the following command line : use <application name>");
			statusCommand.setExitStatut(1);
			return null;
		}

		if (checkUtils.checkImageNoExist(imageName)) {
			return null;
		}

		try {
			Map<String, String> parameters = new HashMap<>();
			parameters.put("imageName", imageName);
			parameters.put("applicationName", applicationName);
			restUtils.sendPostCommand(
					authentificationUtils.finalHost + urlLoader.modulePrefix,
					authentificationUtils.getMap(), parameters).get("body");
			statusCommand.setExitStatut(0);

			response = "Your module " + imageName
					+ " is currently being added to your application "
					+ applicationUtils.getApplication().getName();

			if (script != null) {
				response += ", a script of initialization has been detected";
				ExecutorService executorService = Executors
						.newSingleThreadExecutor();
				executorService.execute(new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
						}
						initData(imageName, script);

					}
				});
			}

			applicationName = applicationUtils.getApplication().getName();
			applicationUtils.setApplication(null);
			applicationUtils.useApplication(applicationName);

		} catch (ResourceAccessException e) {
			log.log(Level.SEVERE,
					"The CLI can't etablished connexion with host servers. Please try later or contact an admin");
			statusCommand.setExitStatut(1);
			return null;
		} catch (Exception e) {
			statusCommand.setExitStatut(1);
			return null;
		}

		return response;

	}

	/**
	 * TODO check if introduction of applicationName don't cause other problems
	 */
	public String removeModule(String moduleName) {
		if (applicationName != null && !applicationName.isEmpty()) {
			applicationUtils.useApplication(applicationName);
		}

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

		if (applicationUtils.getApplication() == null) {
			log.log(Level.SEVERE,
					"No application is currently selected by the following command line : use <application name>");
			statusCommand.setExitStatut(1);
			return null;
		}

		try {

			for (Module module : applicationUtils.getApplication().getModules()) {

				if (module.getName().endsWith(moduleName)) {
					restUtils
							.sendDeleteCommand(
									authentificationUtils.finalHost
											+ urlLoader.modulePrefix
											+ applicationUtils.getApplication()
													.getName() + "/"
											+ module.getName(),
									authentificationUtils.getMap()).get("body");
				}
			}
			applicationName = applicationUtils.getApplication().getName();
			applicationUtils.setApplication(null);
			applicationUtils.useApplication(applicationName);

		} catch (ResourceAccessException e) {
			log.log(Level.SEVERE,
					"The CLI can't etablished connexion with host servers. Please try later or contact an admin");
			statusCommand.setExitStatut(1);
			return null;
		} catch (Exception e) {
			statusCommand.setExitStatut(1);
			return null;
		}

		return "Your module " + moduleName
				+ " is currently being removed from your application "
				+ applicationUtils.getApplication().getName();

	}

	public String initData(String moduleName, File path) {
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

		if (applicationUtils.getApplication() == null) {
			log.log(Level.SEVERE,
					"No application is currently selected by the following command line : use <application name>");
			statusCommand.setExitStatut(1);
			return null;
		}

		try {

			for (Module module : applicationUtils.getApplication().getModules()) {

				if (module.getName().endsWith(moduleName)) {

					File file = path;
					FileInputStream fileInputStream = new FileInputStream(file);
					fileInputStream.available();
					fileInputStream.close();
					FileSystemResource resource = new FileSystemResource(file);
					Map<String, Object> params = new HashMap<>();
					params.put("file", resource);
					params.putAll(authentificationUtils.getMap());

					restUtils.sendPostForUpload(
							authentificationUtils.finalHost
									+ urlLoader.modulePrefix
									+ applicationUtils.getApplication()
											.getName() + "/" + module.getName()
									+ "/initData", params).get("body");
				}
			}
		}

		catch (ResourceAccessException e) {
			log.log(Level.SEVERE,
					"The CLI can't etablished connexion with host servers. Please try later or contact an admin");
			statusCommand.setExitStatut(1);
			return null;
		} catch (Exception e) {
			statusCommand.setExitStatut(1);
			return null;
		}

		return "Datas correctly sent";

	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

}
