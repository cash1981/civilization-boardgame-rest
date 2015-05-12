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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public class CivilizationConfiguration extends Configuration {
    public static final String CIVILIZATION = "civilization";

    @JsonProperty
    @NotEmpty
    public String mongohost = System.getenv("MONGODB_HOST") == null ? "localhost" : System.getenv("MONGODB_HOST");

    @JsonProperty
    @Min(1)
    @Max(65535)
    public int mongoport = System.getenv("MONGODB_PORT") == null ? 27017 : Integer.parseInt(System.getenv("MONGODB_PORT"));

    @JsonProperty
    @NotEmpty
    public String mongodb = System.getenv("MONGODB_NAME") == null ? CIVILIZATION : System.getenv("MONGODB_NAME");

    public String mongodbUser = System.getenv("MONGODB_USER") == null ? null : System.getenv("MONGODB_USER");

    public String mongodbPassword = System.getenv("MONGODB_PASS") == null ? null : System.getenv("MONGODB_PASS");
}