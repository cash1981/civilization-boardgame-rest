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

package no.asgari.civilization.server.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

public class CivilizationConfiguration extends Configuration {
    public static final String CIVILIZATION = "playciv";

    @JsonProperty
    @NotEmpty
    public String mongouri = System.getenv("ATLAS_URI") == null ? "localhost" : System.getenv("ATLAS_URI");

    @JsonProperty
    public String mongohost = System.getenv("MONGODB_HOST") == null ? "localhost" : System.getenv("MONGODB_HOST");

    @JsonProperty
    public int mongoport = System.getenv("MONGODB_PORT") == null ? 27017 : Integer.parseInt(System.getenv("MONGODB_PORT"));

    @JsonProperty
    public String mongodb = System.getenv("MONGODB_NAME") == null ? CIVILIZATION : System.getenv("MONGODB_NAME");

}