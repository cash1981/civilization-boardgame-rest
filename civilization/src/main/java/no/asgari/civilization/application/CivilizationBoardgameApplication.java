package no.asgari.civilization.application;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import no.asgari.civilization.health.TemplateHealthCheck;
import no.asgari.civilization.resource.HelloWorldResource;

public class CivilizationBoardgameApplication extends Application<CivilizationBoardgameConfiguration> {

    public static void main(String[] args) throws Exception {
        new CivilizationBoardgameApplication().run(args);
    }

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<CivilizationBoardgameConfiguration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(CivilizationBoardgameConfiguration configuration,
                    Environment environment) {
        final HelloWorldResource resource = new HelloWorldResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );

        final TemplateHealthCheck healthCheck = new TemplateHealthCheck(configuration.getTemplate());

        environment.healthChecks().register("template", healthCheck);
        environment.jersey().register(resource);
    }

}
