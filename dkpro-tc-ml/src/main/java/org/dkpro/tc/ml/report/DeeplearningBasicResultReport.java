/*******************************************************************************
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.ml.report;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.impl.PropertiesAdapter;
import org.dkpro.lab.task.Task;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.task.ExtractFeaturesTask;
import org.dkpro.tc.evaluation.Id2Outcome;
import org.dkpro.tc.evaluation.evaluator.EvaluatorBase;
import org.dkpro.tc.evaluation.evaluator.EvaluatorFactory;
import org.dkpro.tc.evaluation.measures.label.Accuracy;
import org.dkpro.tc.evaluation.measures.label.MacroFScore;
import org.dkpro.tc.evaluation.measures.label.MicroFScore;
import org.dkpro.tc.evaluation.measures.regression.MeanAbsoluteError;
import org.dkpro.tc.evaluation.measures.regression.RootMeanSquaredError;
import org.dkpro.tc.evaluation.measures.regression.SpearmanCorrelation;
import org.dkpro.tc.ml.report.util.SortedKeyProperties;

import de.tudarmstadt.ukp.dkpro.statistics.correlation.PearsonCorrelation;

/**
 * A result report which creates a few basic measures and writes them to the output folder of a run
 * to provide by default at least some result values.
 */
public class DeeplearningBasicResultReport
    extends ReportBase
    implements Constants
{

    static String OUTPUT_FILE = "results.txt";

    @Override
    public void execute()
        throws Exception
    {
        StorageService store = getContext().getStorageService();

        File id2outcomeFile = getContext().getStorageService().locateKey(getContext().getId(),
                Constants.ID_OUTCOME_KEY);

        // FIXME - hardcoded learning mode
        Id2Outcome o = new Id2Outcome(id2outcomeFile, Constants.LM_SINGLE_LABEL);

        EvaluatorBase createEvaluator = EvaluatorFactory.createEvaluator(o, true, false);
        Properties pa = new SortedKeyProperties();

        Double acc = get(Accuracy.class, createEvaluator);
        Double microF1 = get(MicroFScore.class, createEvaluator);
        Double macroF1 = get(MacroFScore.class, createEvaluator);
        pa.setProperty("Accuracy:", "" + acc);
        pa.setProperty("Micro F1:", "" + microF1);
        pa.setProperty("Macro F1:", "" + macroF1);

        File key = store.locateKey(getContext().getId(), OUTPUT_FILE);
        FileOutputStream fileOutputStream = new FileOutputStream(key);
        pa.store(fileOutputStream, "Results");
        fileOutputStream.close();

    }

    private Double get(Class<?> class1, EvaluatorBase createEvaluator)
        throws TextClassificationException
    {
        return createEvaluator.calculateEvaluationMeasures().get(class1.getSimpleName());
    }
}