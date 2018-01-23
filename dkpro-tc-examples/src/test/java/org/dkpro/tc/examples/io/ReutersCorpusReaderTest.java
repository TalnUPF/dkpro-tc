/**
 * Copyright 2018
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
package org.dkpro.tc.examples.io;

import static org.junit.Assert.assertEquals;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.examples.TestCaseSuperClass;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class ReutersCorpusReaderTest extends TestCaseSuperClass
{
    @Test
    public void testReutersCorpusReader()
        throws Exception
    {
        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                ReutersCorpusReader.class,
                ReutersCorpusReader.PARAM_SOURCE_LOCATION, "classpath:/data/reuters/training",
                ReutersCorpusReader.PARAM_GOLD_LABEL_FILE, "classpath:/data/reuters/cats.txt",
                ReutersCorpusReader.PARAM_LANGUAGE, "en",
                ReutersCorpusReader.PARAM_PATTERNS, new String[] {
                    ReutersCorpusReader.INCLUDE_PREFIX + "*.txt" });

        int i=0;
        for (JCas jcas : new JCasIterable(reader)) {
            DocumentMetaData md = DocumentMetaData.get(jcas);
            dumpMetaData(md);
            i++;

// FIXME should test not write to console
//            for (TextClassificationOutcome outcome : JCasUtil.select(jcas, TextClassificationOutcome.class)) {
//                System.out.println(outcome);
//            }
        }
        assertEquals(5, i);
    }

    private void dumpMetaData(final DocumentMetaData aMetaData)
    {
        System.out.println("Collection ID: "+aMetaData.getCollectionId());
        System.out.println("ID           : "+aMetaData.getDocumentId());
        System.out.println("Base URI     : "+aMetaData.getDocumentBaseUri());
        System.out.println("URI          : "+aMetaData.getDocumentUri());
    }
}
