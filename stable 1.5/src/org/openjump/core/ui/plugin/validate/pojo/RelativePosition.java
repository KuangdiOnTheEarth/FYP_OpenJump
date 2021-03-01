package org.openjump.core.ui.plugin.validate.pojo;

import com.vividsolutions.jump.feature.Feature;

/**
 * Used in the process of ordering the surrounding objects clockwise
 * provides methods to compare the order of two centroid points referring to the center point
 * @author Kuangdi
 *
 */
public class RelativePosition {
	
	private Feature feature;
	private Double sin;
	private Double cos;
	
	public RelativePosition(Feature feature, Double sin, Double cos) {
		this.feature = feature;
		this.sin = sin;
		this.cos = cos;
	}
	
	public Feature feature() {
		return this.feature;
	}
	
	public Double sin() {
		return this.sin;
	}
	
	public Double cos() {
		return this.cos;
	}
	
	/**
	 * return true if this is at the anti-clockwise side of input p
	 * @param p
	 * @return
	 */
	public boolean isAntiClockwiseThan(RelativePosition p) {
		if (this.sin >= 0) {
			if (p.sin() < 0) {
				return false;
			} else { // this.sin >= 0 && p.sin() >= 0
				return this.cos < p.cos();
//				if (this.cos >= 0 && p.cos() >= 0) {
//					return this.sin > p.sin();
//				} else if (this.cos < 0 && p.cos < 0) {
//					return this.sin < p.sin();
//				} else if (this.cos >= 0 && p.cos() < 0) {
//					return false;
//				} else if (this.cos < 0 && p.cos() >= 0) {
//					return true;
//				}
			}
		} else if (this.sin < 0){
			if (p.sin() >= 0) {
				return true;
			} else {
				return this.cos > p.cos();
//				if (this.cos >= 0 && p.cos() >= 0) {
//					return this.sin > p.sin();
//				} else if (this.cos < 0 && p.cos < 0) {
//					return this.sin < p.sin();
//				} else if (this.cos >= 0 && p.cos() < 0) {
//					return true;
//				} else if (this.cos < 0 && p.cos() >= 0) {
//					return false;
//				}
			}
		}
		System.out.print("--RelativePosition-- None of the case is met in method'isAntiClockwiseThan' with this.sin=" + this.sin);
		return false;
	}

}
