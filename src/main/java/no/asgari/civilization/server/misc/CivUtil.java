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

package no.asgari.civilization.server.misc;

import no.asgari.civilization.server.model.Player;
import no.asgari.civilization.server.model.Playerhand;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CivUtil {

    public static <T> Stream<T> streamFromIterable(Iterable<T> in) {
        return StreamSupport.stream(in.spliterator(), false);
    }

    public static boolean shouldSendEmail(Playerhand nextPlayer) {
        long lastEmailSent = nextPlayer.getEmailSent().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        long now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();

        //30 min
        int min = 60 * 30;
        if(min < (now - lastEmailSent)) {
            nextPlayer.setEmailSent(LocalDateTime.now());
            return true;
        }
        return false;
    }

    public static boolean shouldSendEmail(Player player) {
        long lastEmailSent = player.getEmailSent().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        long now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();

        //3 hours
        int min = 60 * 180;
        if(min < (now - lastEmailSent)) {
            player.setEmailSent(LocalDateTime.now());
            return true;
        }
        return false;
    }
}
