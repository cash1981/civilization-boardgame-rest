package no.asgari.civilization.server.mongodb;

import io.dropwizard.logging.LoggingFactory;
import io.dropwizard.testing.junit.DropwizardAppRule;
import no.asgari.civilization.server.CivilizationIntegrationTestApplication;
import no.asgari.civilization.server.CivilizationTestConfiguration;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.ClassRule;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public abstract class AbstractCivilizationTest {

    static {
        LoggingFactory.bootstrap();
    }

    @ClassRule
    public static final DropwizardAppRule<CivilizationTestConfiguration> RULE = new DropwizardAppRule<>(CivilizationIntegrationTestApplication.class, "src/main/resources/config.yml");

    protected static CivilizationIntegrationTestApplication getApp() {
        return RULE.getApplication();
    }

    protected static String getUsernameAndPassEncoded() {
        return "Basic " + B64Code.encode("cash1981" + ":" + "foo", StringUtil.__ISO_8859_1);
    }

    protected static Client client() {
        Client client = ClientBuilder.newClient();
        client.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
        return client;
    }

}
