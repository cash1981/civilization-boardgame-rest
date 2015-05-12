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

package no.asgari.civilization.server.application;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.java8.auth.Authenticator;
import lombok.Cleanup;
import no.asgari.civilization.server.model.Player;
import org.apache.commons.codec.digest.DigestUtils;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;

import java.util.Optional;

public class CivAuthenticator implements Authenticator<BasicCredentials, Player> {
    private final JacksonDBCollection<Player, String> playerCollection;

    public CivAuthenticator(DB db) {
        this.playerCollection = JacksonDBCollection.wrap(db.getCollection(Player.COL_NAME), Player.class, String.class);
    }

    @Override
    public Optional<Player> authenticate(BasicCredentials credentials) {
        @Cleanup DBCursor<Player> dbPlayer = playerCollection.find(
                DBQuery.is("username", credentials.getUsername()), new BasicDBObject());

        if (dbPlayer == null || !dbPlayer.hasNext()) {
            return Optional.empty();
        }

        Player player = dbPlayer.next();

        CivSingleton.instance().playerCache().put(player.getId(), player.getUsername());

        if (player.getPassword().equals(DigestUtils.sha1Hex(credentials.getPassword()))) {
            return Optional.of(player);
        }
        return Optional.empty();
    }
}