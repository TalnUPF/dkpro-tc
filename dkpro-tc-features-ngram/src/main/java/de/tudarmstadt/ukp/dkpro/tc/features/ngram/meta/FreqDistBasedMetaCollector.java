/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.tc.features.ngram.meta;

import java.io.File;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.model.DfStore;
import de.tudarmstadt.ukp.dkpro.tc.api.features.meta.MetaCollector;

public abstract class FreqDistBasedMetaCollector
    extends MetaCollector
{

    protected FrequencyDistribution<String> fd;
    protected DfStore dfStore;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        fd = new FrequencyDistribution<String>();
        dfStore = new DfStore();
    }

    /**
     * @return The path where the lucene index should be stored for this meta collector.
     */
    protected abstract File getFreqDistFile();
    
    @Override
    public void collectionProcessComplete()
        throws AnalysisEngineProcessException
    {
        super.collectionProcessComplete();

        try {
            fd.save(getFreqDistFile());
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

}