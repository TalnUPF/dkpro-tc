/**
 * Copyright (c) 2012, Regents of the University of Colorado
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For a complete copy of the license please see the file LICENSE distributed
 * with the cleartk-syntax-berkeley project or visit
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html.
 */
package org.cleartk.classifier.weka;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.cleartk.classifier.Feature;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;

import weka.core.Attribute;

/**
 * 
 * 
 * EXPERIMENTAL - DO NOT USE
 * 
 * 
 * 
 * 
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 *
 * @author Philip Ogren
 *
 *         Oliver Ferschke: Fix provided. featureToAttribute should not escape the feature name,
 *         since this is done by Instances.toString(), which is used in the ArffSaver. Escaping the
 *         names here results in doubly-escaped features in the arff.
 *
 */
public class WekaSerializedFeaturesEncoder
    implements FeaturesEncoder<Iterable<Feature>>
{

    private static final long serialVersionUID = 1L;

    private final ArrayList<Attribute> attributes;

    private final Map<String, Attribute> attributeMap;

    public WekaSerializedFeaturesEncoder()
    {
        attributes = new ArrayList<Attribute>();
        attributeMap = new HashMap<String, Attribute>();
    }

    public Iterable<Feature> encodeAll(Iterable<Feature> features)
    {
        for (Feature feature : features) {
            addFeatureAsAttribute(feature);
        }
        return features;
    }

	/**
	 * Converts a cleartk feature to a weka attribute and adds it to the attribute list+map
	 * 
	 * @param feature cleartk feature
	 */
	private void addFeatureAsAttribute(Feature feature) {
		String name = feature.getName();
		Attribute attribute = attributeMap.get(name);
		if (attribute == null) {
			// avoid double escaping in ARFF
			Object value = feature.getValue();

			// if value is a number then create a numeric attribute
			if (value instanceof Number) {
				attribute = new Attribute(name);
			}// if value is a boolean then create a numeric attribute
			else if (value instanceof Boolean) {
				attribute = new Attribute(name);
			}
			// if value is an Enum then create a nominal attribute
			else if (value instanceof Enum) {
				Object[] enumConstants = value.getClass().getEnumConstants();
				ArrayList<String> attributeValues = new ArrayList<String>(enumConstants.length);
				for (Object enumConstant : enumConstants) {
					attributeValues.add(enumConstant.toString());
				}
				attribute = new Attribute(name, attributeValues);
			}
			// if value is not a number, boolean, or enum, then we will create a
			// string attribute
			else {
				attribute = new Attribute(name, (ArrayList<String>) null);
			}

			attributes.add(attribute);
			attributeMap.put(name, attribute);
		}
		//return attribute;
	}
	
	
    public void finalizeFeatureSet(File outputDirectory)
    {
    }

    public ArrayList<Attribute> getWekaAttributes()
    {
        return attributes;
    }

    public Map<String, Attribute> getWekaAttributeMap()
    {
        return attributeMap;
    }

}
