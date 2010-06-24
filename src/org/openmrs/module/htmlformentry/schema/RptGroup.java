package org.openmrs.module.htmlformentry.schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an RptGroup in the HTML Form Schema
 */
public class RptGroup implements HtmlFormField {
	
	private List<Integer> childrenobs = new ArrayList<Integer>();

	/* the string to hold the xml representation of the content this repeat*/
	private String xmlfragment;
	
	/* see if we need to append <tr><td> ...</td></tr> pair outside this repeat */
	private boolean intd;
	
	private Integer repeattime = 0;

	public List<Integer> getChildrenobs() {
		return childrenobs;
	}

	public void setChildrenobs(List<Integer> childrenobs) {
		this.childrenobs = childrenobs;
	}

	public String getXmlfragment() {
		return xmlfragment;
	}

	public void setXmlfragment(String xmlfragment) {
		this.xmlfragment = xmlfragment;
	}

	public Integer getRepeattime() {
		return repeattime;
	}

	public void setRepeattime(Integer repeattime) {
		this.repeattime = repeattime;
	}
	
	/***
	 * return howmany obs are in this repeat
	 */
	public Integer getObsNum(){
		return this.childrenobs.size();
	}

	public boolean isIntd() {
		return intd;
	} 

	public void setIntd(boolean intd) {
		this.intd = intd;
	}
	
	
}

