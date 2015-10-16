package fr.treeptik.cloudunit.cli.integration.application;

import fr.treeptik.cloudunit.cli.integration.AbstractShellIntegrationTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.CommandResult;

/**
 * Created by guillaume on 16/10/15.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractApplicationCommandsIT extends AbstractShellIntegrationTest {

    protected String serverType;

    private String applicationName = "app";

    @Test
    public void test00_shouldCreateApplication() {
        CommandResult cr = getShell().executeCommand("connect --login johndoe --password abc2015");

        cr = getShell().executeCommand("create-app --name " + applicationName + " --type " + serverType);
        String result = cr.getResult().toString();
    }
}
