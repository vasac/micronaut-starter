/*
 * Copyright 2020 original authors
 *
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
 * limitations under the License.
 */
package io.micronaut.starter.feature.validation;

import io.micronaut.context.annotation.Primary;
import io.micronaut.starter.feature.Feature;
import io.micronaut.starter.options.Language;

import javax.inject.Singleton;
import java.util.List;

@Primary
@Singleton
public class CompositeFeatureValidator implements FeatureValidator {

    private final List<FeatureValidator> featureValidators;

    public CompositeFeatureValidator(List<FeatureValidator> featureValidators) {
        this.featureValidators = featureValidators;
    }

    public void validate(Language language, List<Feature> features) {
        for (FeatureValidator featureValidator: featureValidators) {
            featureValidator.validate(language, features);
        }
    }
}
