package org.oracle.odci;

public interface ODCIIndex {
	
	public void ODCIIndexCreate(String indexName);
	public void ODCIIndexDrop(String indexName);
	public void ODCIIndexInsert(String indexName, String imagePath);
	public void ODCIIndexDelete(String indexName, String imagePath);
	public void ODCIIndexUpdate(String indexName, String oldImagePath, String newImagePath);
	public void ODCIIndexStart(String uuid, String indexName, String inputImage);
	public void ODCIIndexFetch(String uuid, int n);
	public void ODCIIndexClose(String uuid);

}
