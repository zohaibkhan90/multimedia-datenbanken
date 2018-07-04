package org.oracle.odci;

import org.lira.IndexService;

class ContextData {
	
	private String inputImage;
	private String[] similarityResults;
	
	private int skip = 0;
	public boolean hasMoreResults(){
		return skip<=Integer.valueOf(IndexService.getProperties().getProperty("num_of_results", "12"));
	}
	public int getSkip() {
		return skip;
	}

	public void setSkip(int skip) {
		this.skip = skip;
	}

	
	public String getInputImage() {
		return inputImage;
	}
	public void setInputImage(String inputImage) {
		this.inputImage = inputImage;
	}
	public String[] getSimilarityResults() {
		return similarityResults;
	}
	public void setSimilarityResults(String[] similarityResults) {
		this.similarityResults = similarityResults;
	}
}