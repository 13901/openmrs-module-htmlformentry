/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.htmlformentry.element;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.StringContains;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.internal.matchers.Contains;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptNumeric;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.htmlformentry.Translator;
import org.openmrs.module.htmlformentry.schema.HtmlFormSchema;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.StringContains.containsString;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(HtmlFormEntryUtil.class)
public class ObsSubmissionElementTest {

    private FormEntryContext context;
    private Map<String, String> params;

    @Before
    public void setUp() throws Exception {
        HtmlFormSchema schema = new HtmlFormSchema();
        Translator translator = new Translator() {
            @Override
            public String translate(String localeStr, String key) {
                return key;
            }
        };

        params = new HashMap<String, String>();

        context = mock(FormEntryContext.class);
        when(context.getSchema()).thenReturn(schema);
        when(context.getTranslator()).thenReturn(translator);
    }

    @Test
    public void testShowUnitsUsingTrue() {
        ConceptDatatype numeric = new ConceptDatatype();
        numeric.setUuid(ConceptDatatype.NUMERIC_UUID);

        ConceptNumeric weight = new ConceptNumeric();
        String units = "kg";
        weight.setUnits(units);
        weight.setDatatype(numeric);

        mockStatic(HtmlFormEntryUtil.class);
        PowerMockito.when(HtmlFormEntryUtil.getConcept(anyString())).thenReturn(weight);

        params.put("showUnits", "true");
        params.put("conceptId", "5089");
        ObsSubmissionElement element = new ObsSubmissionElement(context, params);
        String html = element.generateHtml(context);

        assertThat(html, containsString("<span class=\"units\">" + units + "</span>"));
    }

    @Test
    public void testShowUnitsUsingCode() {
        ConceptDatatype numeric = new ConceptDatatype();
        numeric.setUuid(ConceptDatatype.NUMERIC_UUID);

        ConceptNumeric weight = new ConceptNumeric();
        weight.setUnits("kg");
        weight.setDatatype(numeric);

        mockStatic(HtmlFormEntryUtil.class);
        PowerMockito.when(HtmlFormEntryUtil.getConcept(anyString())).thenReturn(weight);

        String unitsCode = "units.kg";
        params.put("showUnits", unitsCode);
        params.put("conceptId", "5089");
        ObsSubmissionElement element = new ObsSubmissionElement(context, params);
        String html = element.generateHtml(context);

        assertThat(html, containsString("<span class=\"units\">" + unitsCode + "</span>"));
    }
}
