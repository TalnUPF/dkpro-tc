/*
 * Copyright 2015
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
 */

package org.dkpro.tc.svmhmm.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections.BidiMap;
import org.dkpro.lab.reporting.ReportBase;
import org.dkpro.lab.storage.StorageService;
import org.dkpro.lab.storage.StorageService.AccessMode;
import org.dkpro.tc.core.Constants;
import org.dkpro.tc.core.ml.TCMachineLearningAdapter;
import org.dkpro.tc.svmhmm.SVMHMMAdapter;
import org.dkpro.tc.svmhmm.util.SVMHMMUtils;

public class SVMHMMOutcomeIDReport
    extends ReportBase
    implements Constants
{

    protected List<String> goldLabels;

    protected List<String> predictedLabels;

    public static final String SEPARATOR_CHAR = ";";

    /*
     * Dummy value as threshold which is expected by the evaluation module but not created/needed by
     * SvmHmm
     */
    private static final String THRESHOLD_DUMMY_CONSTANT = "-1";

    /**
     * Returns the current test file
     *
     * @return test file
     */
    protected File locateTestFile()
    {
        // test file with gold labels
        File testDataStorage = getContext().getFolder(TEST_TASK_INPUT_KEY_TEST_DATA,
                StorageService.AccessMode.READONLY);
        String fileName = new SVMHMMAdapter()
                .getFrameworkFilename(TCMachineLearningAdapter.AdapterNameEntries.featureVectorsFile);
        return new File(testDataStorage, fileName);
    }

    /**
     * Loads gold labels and predicted labels
     *
     * @throws IOException
     */
    protected void loadGoldAndPredictedLabels()
        throws IOException
    {
        // predictions
        File predictionFolder = getContext().getFolder(TEST_TASK_OUTPUT_KEY,
                StorageService.AccessMode.READONLY);
        String predictionFileName = new SVMHMMAdapter()
                .getFrameworkFilename(TCMachineLearningAdapter.AdapterNameEntries.predictionsFile);
        File predictionsFile = new File(predictionFolder, predictionFileName);

        // test file with gold labels
        File testFile = locateTestFile();

        // load the mappings from labels to integers
        File mappingFolder = getContext().getFolder(TEST_TASK_OUTPUT_KEY,
                StorageService.AccessMode.READWRITE);
        File mappingFile = new File(mappingFolder, SVMHMMUtils.LABELS_TO_INTEGERS_MAPPING_FILE_NAME);
        BidiMap labelsToIntegersMapping = SVMHMMUtils.loadMapping(mappingFile);

        // gold label tags
        goldLabels = SVMHMMUtils.extractOutcomeLabels(testFile);

        // predicted tags
        predictedLabels = SVMHMMUtils.extractOutcomeLabelsFromPredictions(predictionsFile,
                labelsToIntegersMapping);

        // sanity check
        if (goldLabels.size() != predictedLabels.size()) {
            throw new IllegalStateException("Gold labels and predicted labels differ in size!");
        }
    }

    @Override
    public void execute()
        throws Exception
    {
        // load gold and predicted labels
        loadGoldAndPredictedLabels();

        File testFile = locateTestFile();

        // original tokens
        List<String> originalTokens = SVMHMMUtils.extractOriginalTokens(testFile);

        // sequence IDs
        List<Integer> sequenceIDs = SVMHMMUtils.extractOriginalSequenceIDs(testFile);

        // sanity check
        if (goldLabels.size() != originalTokens.size() || goldLabels.size() != sequenceIDs.size()) {
            throw new IllegalStateException(
                    "Gold labels, original tokens or sequenceIDs differ in size!");
        }

        File evaluationFolder = getContext().getFolder("", AccessMode.READWRITE);
        File evaluationFile = new File(evaluationFolder, ID_OUTCOME_KEY);

        // write results into CSV
        // form: gold;predicted;token;seqID

        // build header
        Map<String, Integer> label2id = createLabel2IdMapping(goldLabels, predictedLabels);
        String header = buildHeader(label2id);

        Properties prop = new Properties();
        for (int i = 0; i < goldLabels.size(); i++) {
            String gold = goldLabels.get(i);
            String pred = predictedLabels.get(i);

            int g = label2id.get(gold);
            int p = label2id.get(pred);
            prop.setProperty("" + i, p + SEPARATOR_CHAR + g + SEPARATOR_CHAR
                    + THRESHOLD_DUMMY_CONSTANT);
        }
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(evaluationFile));
        prop.store(osw, header);
        osw.close();
    }

    private String buildHeader(Map<String, Integer> label2id)
        throws UnsupportedEncodingException
    {
        StringBuilder sb = new StringBuilder();

        sb.append("ID=PREDICTION" + SEPARATOR_CHAR + "GOLDSTANDARD" + SEPARATOR_CHAR + "THRESHOLD"
                + "\n" + "labels" + " ");

        List<String> keySet = new ArrayList<>(label2id.keySet());
        for (int i = 0; i < keySet.size(); i++) {
            String key = keySet.get(i);
            sb.append(label2id.get(key) + "=" + URLEncoder.encode(key, "UTF-8"));
            if (i + 1 < keySet.size()) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    private Map<String, Integer> createLabel2IdMapping(List<String> goldLabels,
            List<String> predictedLabels)
    {
        Set<String> all = new HashSet<>();
        all.addAll(goldLabels);
        all.addAll(predictedLabels);

        Map<String, Integer> map = new HashMap<>();

        Integer id = 0;
        for (String label : all) {
            map.put(label, id++);
        }

        return map;
    }
}