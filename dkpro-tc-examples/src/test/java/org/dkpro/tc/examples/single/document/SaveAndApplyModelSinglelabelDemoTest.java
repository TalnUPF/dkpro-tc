/**
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.tc.examples.single.document;

import org.dkpro.lab.task.ParameterSpace;
import org.junit.Before;
import org.junit.Test;
import org.dkpro.tc.examples.single.document.SaveAndApplyModelSinglelabelDemo;
import org.dkpro.tc.examples.utils.JavaDemosTest_Base;

/**
 * This test just ensures that the experiment runs without throwing
 * any exception.
 */
public class SaveAndApplyModelSinglelabelDemoTest extends JavaDemosTest_Base
{
    SaveAndApplyModelSinglelabelDemo javaExperiment;
    ParameterSpace pSpace;

    @Before
    public void setup()
        throws Exception
    {
        super.setup();
        
        javaExperiment = new SaveAndApplyModelSinglelabelDemo();
        pSpace = SaveAndApplyModelSinglelabelDemo.getParameterSpace();
    }

    @Test
    public void testJavaSaveModel()
        throws Exception
    {
        javaExperiment.runSaveModel(pSpace);
        javaExperiment.applyStoredModel("This is a fancy example text.");
    }
}