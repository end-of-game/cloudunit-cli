package fr.treeptik.cloudunit.cli.shell;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.plugin.PromptProvider;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CloudUnitPromptProvider
        implements PromptProvider, CommandMarker {
    @Value("${default.prompt}")
    private String prompt;

    public String getProviderName() {
        return null;
    }

    public String getPrompt() {
        return prompt + " ";
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

}
