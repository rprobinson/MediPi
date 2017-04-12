/*
 *
 * Copyright (C) 2016 Krishna Kuntala @ Mastek <krishna.kuntala@mastek.com>
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
 *
 */
package org.medipi.clinical.threshold;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThresholdTestFactory {

    @Autowired
    private SimpleInclusiveHighLowTest simpleInclusiveHighLowTest;

    @Autowired
    private ChangeOverTimeTest changeOverTimeTest;

    @Autowired
    private RelativeInclusiveHighTest relativeInclusiveHighTest;

    @Autowired
    private QuestionnaireTest questionnaireTest;

    public AttributeThresholdTest getInstance(final String testType) {
        AttributeThresholdTest attributeThresholdTest = null;

        switch (testType) {
            case "org.medipi.clinical.threshold.SimpleInclusiveHighLowTest":
                attributeThresholdTest = simpleInclusiveHighLowTest;
                break;
            case "org.medipi.clinical.threshold.ChangeOverTimeTest":
                attributeThresholdTest = changeOverTimeTest;
                break;
            case "org.medipi.clinical.threshold.RelativeInclusiveHighTest":
                attributeThresholdTest = relativeInclusiveHighTest;
                break;
            case "org.medipi.clinical.threshold.QuestionnaireTest":
                attributeThresholdTest = questionnaireTest;
                break;
            default:
                attributeThresholdTest = simpleInclusiveHighLowTest;
        }
        return attributeThresholdTest;
    }
}
