package fr.treeptik.cloudunit.cli.integration.application;

import fr.treeptik.cloudunit.cli.integration.AbstractShellIntegrationTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.shell.core.CommandResult;

import java.util.Random;

/**
 * Created by guillaume on 16/10/15.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractApplicationCommandsIT extends AbstractShellIntegrationTest {

    private static String applicationName;
    protected String serverType;

    @BeforeClass
    public static void generateApplicationName() {
        applicationName = "App" + new Random().nextInt(10000);
    }

    @Test
    public void test00_shouldCreateApplication() {
        CommandResult cr = getShell().executeCommand("connect --login johndoe --password abc2015");
        cr = getShell().executeCommand("create-app --name " + applicationName + " --type " + serverType);
        String result = cr.getResult().toString();
        String expectedResult = "Your application " + applicationName + " is currently being installed";
        Assert.assertEquals(result, expectedResult);
    }

    @Test
    public void test00_shouldNotCreateApplicationBeforeNameAlreadyInUse() {
        CommandResult cr = getShell().executeCommand("connect --login johndoe --password abc2015");
        cr = getShell().executeCommand("create-app --name " + applicationName + " --type " + serverType);
        String result = cr.getResult().toString();
        String expectedResult = "This application name already exists";
        Assert.assertEquals(result, expectedResult);
    }
}
