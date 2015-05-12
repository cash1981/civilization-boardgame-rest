/*
 * Copyright (c) 2015 Shervin Asgari
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

import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;
import lombok.extern.log4j.Log4j;

/**
 * Class that will send emails
 */
@Log4j
public class SendEmail {
    public static final String SENDGRID_USERNAME = "SENDGRID_USERNAME";
    public static final String SENDGRID_PASSWORD = "SENDGRID_PASSWORD";
    private static final SendGrid sendgrid = new SendGrid(System.getenv(SENDGRID_USERNAME), System.getenv(SENDGRID_PASSWORD));

    public static boolean sendYourTurn(String gamename, String emailToo) {
        if(System.getenv(SENDGRID_USERNAME) == null || System.getenv(SENDGRID_PASSWORD) == null) {
            log.error("Missing environment variable for SENDGRID_USERNAME or SENDGRID_PASSWORD");
            return false;
        }
        SendGrid.Email email = new SendGrid.Email();
        email.addTo(emailToo);
        email.setFrom("no-reply@asgari.no");
        email.setSubject("It is your turn");
        email.setText("It's your turn to play in " + gamename + "!");

        try {
            SendGrid.Response response = sendgrid.send(email);
            return response.getStatus();
        } catch (SendGridException e) {
            log.error("Error sending email: " + e.getMessage(), e);
        }
        return false;
    }
}
