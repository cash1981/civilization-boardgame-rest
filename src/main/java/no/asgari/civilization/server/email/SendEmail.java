/*
 * Copyright (c) 2015-2021 Shervin Asgari
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package no.asgari.civilization.server.email;

import com.google.common.base.Strings;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import lombok.extern.log4j.Log4j;

import java.io.IOException;

/**
 * Class that will send emails
 */
@Log4j
public class SendEmail {
    public static final String SENDGRID_USERNAME = "SENDGRID_USERNAME";
    public static final String SENDGRID_PASSWORD = "SENDGRID_PASSWORD";
    public static final String NOREPLY_PLAYCIV_COM = "noreply@playciv.com";
    public static final String URL = "http://playciv.com/";
    public static final String REST_URL = "https://civilization-boardgame.herokuapp.com/";
    private static final String SENDGRID_API_KEY = "SENDGRID_API_KEY";

    public static String gamelink(String pbfId) {
        return URL + "#/game/" + pbfId;
    }

    public static boolean sendYourTurn(String gamename, String emailToo, String pbfId) {
        if (System.getenv(SENDGRID_USERNAME) == null || System.getenv(SENDGRID_PASSWORD) == null) {
            log.error("Missing environment variable for SENDGRID_USERNAME or SENDGRID_PASSWORD");
            return false;
        }

        Email from = new Email(NOREPLY_PLAYCIV_COM);
        Email to = new Email(emailToo);
        Content content = new Content("text/html", "It's your turn to play in " + gamename + "!\n\n" +
                "Go to " + gamelink(pbfId) + " to start your turn");
        Mail mail = new Mail(from, "It is your turn", to, content);

        SendGrid sg = new SendGrid(System.getenv(SENDGRID_API_KEY));
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            log.error("Error sending email: " + ex.getMessage(), ex);
        }
        return false;
    }

    public static boolean sendMessage(String email, String subject, String message, String playerId) {
        if (System.getenv(SENDGRID_USERNAME) == null || System.getenv(SENDGRID_PASSWORD) == null) {
            log.error("Missing environment variable for SENDGRID_USERNAME or SENDGRID_PASSWORD");
            return false;
        }

        Email from = new Email(NOREPLY_PLAYCIV_COM);
        Email to = new Email(email);
        Content content = new Content("text/plain", message + UNSUBSCRIBE(playerId));
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(System.getenv(SENDGRID_API_KEY));
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            log.error("Error sending email: " + ex.getMessage(), ex);
        }

        return false;
    }

    private static String UNSUBSCRIBE(String playerId) {
        if (Strings.isNullOrEmpty(playerId)) {
            return "";
        }
        return "\n\nIf you wish to unsubscribe from ALL emails, then push this link: " + REST_URL + "api/admin/email/notification/" + playerId + "/stop";
    }

}
