/*******************************************************************************
 * Copyright 2016
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
package org.dkpro.tc.evaluation.evaluator;

import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.evaluation.confusion.matrix.AbstractLargeContingencyTable;


/**
 * "The bipartitions-based evaluation measures are calculated based
 * on the comparison of the predicted relevant labels with the
 * ground truth relevant labels. This group of evaluation measures
 * is further divided into example-based and label-based."
 * Gj. Madjarov, et al., An extensive experimental comparison of methods for multi-label learning, 
 * Pattern Recognition (2012)
 */
public interface BipartitionBased {
	
	public <T> AbstractLargeContingencyTable<T> buildLargeContingencyTable() throws TextClassificationException;
}
