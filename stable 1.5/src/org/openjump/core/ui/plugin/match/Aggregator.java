/*
Copyright (c) 2010, Michaël Michaud
All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of its authors nor the names of its contributors may
      be used to endorse or promote products derived from this software without
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.openjump.core.ui.plugin.match;

import com.vividsolutions.jump.feature.Feature;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Interface for all aggregate functions.
 * @author Micha&euml;l Michaud
 * @version 0.5 (2011-12-01)
 */
public interface Aggregator<V> {
    

    /**
     * Aggregate values of attribute in all features
     * @return a double in the range [0-1] representative of the match quality
     * between f and ref. 
     *
     * @throws an exception if input data cannot be processed.
     */
     public V aggregate(Collection<Feature> features, 
                        String attribute, boolean ignoreNull) throws Exception;
     
     
     public static Aggregator<Integer> COUNT = new Aggregator<Integer>() {
         public Integer aggregate(Collection<Feature> features, 
             String attribute, boolean ignoreNull) throws Exception {
             int count = 0;
             for (Feature f : features) {
                 Object val = f.getAttribute(attribute);
                 if (val != null || !ignoreNull) count++;
             }
             return count;
         }
         public String toString() {
             return I18NPlug.getI18N("aggregator.Count");
         }
     };
     
     public static Aggregator<Integer> SUM_INTEGER = new Aggregator<Integer>() {
         public Integer aggregate(Collection<Feature> features, 
             String attribute, boolean ignoreNull) throws Exception {
             int sum = 0;
             for (Feature f : features) {
                 Integer val = (Integer)f.getAttribute(attribute);
                 if (val != null) sum += val;
             }
             return sum;
         }
         public String toString() {
             return I18NPlug.getI18N("aggregator.Sum-integer");
         }
     };
     
     public static Aggregator<Double> SUM_DOUBLE = new Aggregator<Double>() {
         public Double aggregate(Collection<Feature> features, 
             String attribute, boolean ignoreNull) throws Exception {
             double sum = 0;
             for (Feature f : features) {
                 Double val = (Double)f.getAttribute(attribute);
                 if (val != null) sum += val;
             }
             return sum;
         }
         public String toString() {
             return I18NPlug.getI18N("aggregator.Sum-double");
         }
     };
     
     public static Aggregator<Integer> MEAN_INTEGER = new Aggregator<Integer>() {
         public Integer aggregate(Collection<Feature> features, 
             String attribute, boolean ignoreNull) throws Exception {
             int sum = 0;
             int count = 0;
             for (Feature f : features) {
                 Integer val = (Integer)f.getAttribute(attribute);
                 if (val != null) sum += val;
                 if (val != null || !ignoreNull) count++;
             }
             return count > 0 ? sum/count : null;
         }
         public String toString() {
             return I18NPlug.getI18N("aggregator.Mean-integer");
         }
     };
     
     public static Aggregator<Double> MEAN_DOUBLE = new Aggregator<Double>() {
         public Double aggregate(Collection<Feature> features, 
             String attribute, boolean ignoreNull) throws Exception {
             double sum = 0.0;
             int count = 0;
             for (Feature f : features) {
                 Double val = (Double)f.getAttribute(attribute);
                 if (val != null) sum += val;
                 if (val != null || !ignoreNull) count++;
             }
             return count > 0 ? sum/count : null;
         }
         public String toString() {
             return I18NPlug.getI18N("aggregator.Mean-double");
         }
     };
     
     public static Aggregator<Date> MEAN_DATE = new Aggregator<Date>() {
         public Date aggregate(Collection<Feature> features, 
             String attribute, boolean ignoreNull) throws Exception {
             long sum = 0L;
             int count = 0;
             for (Feature f : features) {
                 Date val = (Date)f.getAttribute(attribute);
                 if (val != null) sum += val.getTime();
                 if (val != null || !ignoreNull) count++;
             }
             return count > 0 ? new Date(sum/count) : null;
         }
         public String toString() {
             return I18NPlug.getI18N("aggregator.Mean-date");
         }
     };
     
     public static Aggregator<Integer> MAX_INTEGER = new Aggregator<Integer>() {
         public Integer aggregate(Collection<Feature> features, 
             String attribute, boolean ignoreNull) throws Exception {
             Integer max = null;
             for (Feature f : features) {
                 Integer val = (Integer)f.getAttribute(attribute);
                 if (max == null || (val != null && val > max)) max = val;
             }
             return max;
         }
         public String toString() {
             return I18NPlug.getI18N("aggregator.Max-integer");
         }
     };
     
     public static Aggregator<Double> MAX_DOUBLE = new Aggregator<Double>() {
         public Double aggregate(Collection<Feature> features, 
             String attribute, boolean ignoreNull) throws Exception {
             Double max = 0.0;
             for (Feature f : features) {
                 Double val = (Double)f.getAttribute(attribute);
                 if (max == null || (val != null && val > max)) max = val;
             }
             return max;
         }
         public String toString() {
             return I18NPlug.getI18N("aggregator.Max-double");
         }
     };
     
     public static Aggregator<Date> MAX_DATE = new Aggregator<Date>() {
         public Date aggregate(Collection<Feature> features, 
             String attribute, boolean ignoreNull) throws Exception {
             Date max = null;
             for (Feature f : features) {
                 Date val = (Date)f.getAttribute(attribute);
                 if (max == null || (val != null && val.getTime() > max.getTime())) max = val;
             }
             return max;
         }
         public String toString() {
             return I18NPlug.getI18N("aggregator.Max-date");
         }
     };
     
     public static Aggregator<Integer> MIN_INTEGER = new Aggregator<Integer>() {
         public Integer aggregate(Collection<Feature> features, 
             String attribute, boolean ignoreNull) throws Exception {
             Integer min = null;
             for (Feature f : features) {
                 Integer val = (Integer)f.getAttribute(attribute);
                 if (min == null || (val != null && val < min)) min = val;
             }
             return min;
         }
         public String toString() {
             return I18NPlug.getI18N("aggregator.Min-integer");
         }
     };
     
     public static Aggregator<Double> MIN_DOUBLE = new Aggregator<Double>() {
         public Double aggregate(Collection<Feature> features, 
             String attribute, boolean ignoreNull) throws Exception {
             Double min = 0.0;
             for (Feature f : features) {
                 Double val = (Double)f.getAttribute(attribute);
                 if (min == null || (val != null && val < min)) min = val;
             }
             return min;
         }
         public String toString() {
             return I18NPlug.getI18N("aggregator.Min-double");
         }
     };
     
     public static Aggregator<Date> MIN_DATE = new Aggregator<Date>() {
         public Date aggregate(Collection<Feature> features, 
             String attribute, boolean ignoreNull) throws Exception {
             Date min = null;
             for (Feature f : features) {
                 Date val = (Date)f.getAttribute(attribute);
                 if (min == null || (val != null && val.getTime() > min.getTime())) min = val;
             }
             return min;
         }
         public String toString() {
             return I18NPlug.getI18N("aggregator.Min-date");
         }
     };
     
     public static Aggregator<String> CONCATENATE = new Aggregator<String>() {
         public String aggregate(Collection<Feature> features, 
             String attribute, boolean ignoreNull) throws Exception {
             StringBuilder sb = new StringBuilder();
             for (Feature f : features) {
                 String val = f.getString(attribute);
                 if (val != null) sb.append(val).append("|");
             }
             if (sb.length()>0) sb.setLength(sb.length()-1);
             return sb.toString();
         }
         public String toString() {
             return I18NPlug.getI18N("aggregator.Concatenate");
         }
     };
     
     public static Aggregator<String> CONCATENATE_UNIQUE = new Aggregator<String>() {
         public String aggregate(Collection<Feature> features, 
             String attribute, boolean ignoreNull) throws Exception {
             TreeSet<String> set = new TreeSet<String>();
             for (Feature f : features) {
                 String val = f.getString(attribute);
                 if (val != null) set.add(val);
             }
             StringBuilder sb = new StringBuilder();
             for (String s : set) {
                 sb.append(s).append("|");
             }
             if (sb.length()>0) sb.setLength(sb.length()-1);
             return sb.toString();
         }
         public String toString() {
             return I18NPlug.getI18N("aggregator.Concatenate-unique");
         }
     };
     
     public static Aggregator<String> MOST_FREQUENT = new Aggregator<String>() {
         public String aggregate(Collection<Feature> features, 
             String attribute, boolean ignoreNull) throws Exception {
             Map<String,Integer> map = new TreeMap<String,Integer>();
             for (Feature f : features) {
                 String val = f.getString(attribute);
                 if (map.containsKey(val)) map.put(val, map.get(val)+1);
                 else map.put(val, 1);
             }
             String mostFrequent = null;
             int maxOcc = 0;
             for (Map.Entry<String,Integer> entry : map.entrySet()) {
                 if (entry.getValue() > maxOcc) {
                     maxOcc = entry.getValue();
                     mostFrequent = entry.getKey();
                 }
             }
             return mostFrequent;
         }
         public String toString() {
             return I18NPlug.getI18N("aggregator.Most-frequent");
         }
     };

}
