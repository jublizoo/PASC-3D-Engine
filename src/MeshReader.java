import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MeshReader {
	
	File file;
	FileReader fReader;
	BufferedReader reader;
	Projection p;
	ArrayList<String> wholeText;
	Double[][] currentTriangle;
	Double[][] currentUv;
	/* 
	 * Where we should start looking for faces, vertices, and UVs, because the first few characters are used purely for
	 * identification, and therefore we do not need to search them for information.
	 */
	final static int faceStartChar = 2;
	final static int vertexStartChar = 2;
	final static int uvStartChar = 3;
	
	Integer uvStartLine;
	
	public MeshReader(Projection p) {
		this.p = p;
		
	}
	
	//TODO Search for the line number of the first line containing UVs.
	public void readFile(String url) {
		uvStartLine = null;
		//The text resets for every file, not every line, so we must reset it here.
		wholeText = new ArrayList<String>();
		System.out.println("MeshReader running");
		int currentLineNum = 0;
		int triangleIndex = p.getTriangles3d().size() - 1;
		String currentLine = "";
		
		file = new File(url);

		try {
			fReader = new FileReader(file);
			reader = new BufferedReader(fReader);
		} catch (FileNotFoundException e) {
			System.out.println(e);
			return;
		}
		
		/*
		 * We need to add one to the triangle index, because no triangles have been added for this object, so it 
		 * represents the last triangle of the previous object.
		 */
		p.objIndexes.add(new Integer[] {triangleIndex + 1, null});
		
		while(true) {
			try {
				currentLine = reader.readLine();
			} catch (IOException e) {
				//This try catch is purely technical. We should not encounter this error, therefore we do not need a return statement
				System.out.println(e);
			}
			
			currentTriangle = new Double[3][3];
			currentUv = new Double[3][2];
			
			if(currentLine == null) {
				//Adding the index of the last triangle in this file.
				p.objIndexes.get(p.objIndexes.size() - 1)[1] = triangleIndex;
				System.out.println("MeshReader search completed.");
				return;
			}else {
				wholeText.add(currentLine);
				if(currentLine.charAt(0) == 'f') {
					findLineNums(currentLine);
					/*
					 * We want to add elements to the following ArrayLists AFTER we call the findLineNums function,
					 * because we need to set the most recent element of triangles3d directly after adding the element
					 * with a null value. If we do not do this, the projectAll function will run through every triangle,
					 * including the "unset" null triangle. This will lead to a null pointer exception. To set the most
					 * recent element of triangles3d directly after adding the element, the currentTriangle Double[][]
					 * must already be set, which is done by calling the findLineNums function, meaning it must be 
					 * called first.
					 */
					initNewArrayListElements();
					triangleIndex++;
					p.getTriangles3d().set(triangleIndex, currentTriangle);
					p.triangleUvs.set(triangleIndex, currentUv);
					setNewArrayListElements();
					/*
					 * try { Thread.sleep(20); }catch(Exception e){
					 * 
					 * }
					 */
				}else if(currentLine.charAt(1) == 't' && uvStartLine == null) {
					//This will run before we get to the faces
					uvStartLine = currentLineNum;
				}
				currentLineNum++;
				
			}
		}
		
	}

	public void initNewArrayListElements() {
		Double[] point2d;
		p.getTriangles3d().add(new Double[3][3]);
		p.triangleUvs.add(new Double[3][2]);
		p.triangles2d.add(new Double[3][2]);
		p.triangleMidPoints.add(new Double[3]);
		p.midPointDistances.add(null);
		
	}
	
	public void setNewArrayListElements() {
		//TODO Create function to calculate these for a single point, instead of calling a function to calculate all
		p.projectAll();
		p.calculateTriangleMidPoints();
		p.calculateMidPointDistances();
		//Add uv projection
	}
	
	public void findLineNums(String currentLine) {
		//Specifies if the current character/upcoming character being read is a UV or a default coordinate
		boolean uvChar = false;
		//Specifies the current vertex (0, 1, 2), for indexing purposes
		int vertexIndex = 0;
		int uvVertexIndex = 0;
		StringBuilder currentVertexLineNum = new StringBuilder("");
		StringBuilder currentUvLineNum = new StringBuilder("");
		
		for(int i = faceStartChar; i < currentLine.length(); i++) {
			if(currentLine.charAt(i) == ' ') {
				readUV(Integer.valueOf(currentUvLineNum.toString()) + uvStartLine, uvVertexIndex);
				currentUvLineNum = new StringBuilder("");
				uvVertexIndex++;
				uvChar = false;
			}else if(currentLine.charAt(i) == '/') {
				readVertex(Integer.valueOf(currentVertexLineNum.toString()), vertexIndex);
				currentVertexLineNum = new StringBuilder("");
				vertexIndex++;
				uvChar = true;
			}else {
				if(uvChar) {
					currentUvLineNum.append(currentLine.charAt(i));
				}else {
					currentVertexLineNum.append(currentLine.charAt(i));
				}
			}
		}
		
		//There is no space character to tell us to read UVs at the end of each line, so we must add it manually
		readUV(Integer.valueOf(currentUvLineNum.toString()) + uvStartLine, uvVertexIndex);
		
	}
	
	public void readUV(int uvLineNum, int uvVertexIndex) {
		//Specifies the current dimension (0, 1, 2 / x , y, z), for indexing purposes
				int dimensionIndex = 0;
				//Wavefront files have the lines start at 1, so we need to compensate for Java starting at 0.
				uvLineNum -= 1;
				/*
				 * This " " is created, because otherwise the code to add the last vertex is not run. The code to add each vertex
				 * is only run when there is a space, but the last character is not a space, so the final vertex will not be
				 * added to getTriangles3d(), unless we manually add a space.
				 */
				String currentLine = wholeText.get(uvLineNum) + " ";
				//TODO Technically only one dimension of a vertex, so might want to change
				StringBuilder currentUvCoord = new StringBuilder("");
				
				for(int i = uvStartChar; i < currentLine.length(); i++) {
					if(currentLine.charAt(i) == ' ') {
						currentUv[uvVertexIndex][dimensionIndex] = Double.valueOf(currentUvCoord.toString());
						currentUvCoord = new StringBuilder("");
						dimensionIndex++;
					}else {
						currentUvCoord.append(currentLine.charAt(i));
					}
				}
	}
	
	public void readVertex(int vertexLineNum, int vertexIndex) {
		//Specifies the current dimension (0, 1, 2 / x , y, z), for indexing purposes
		int dimensionIndex = 0;
		//Wavefront files have the lines start at 1, so we need to compensate for Java starting at 0.
		vertexLineNum -= 1;
		/*This " " is created, because otherwise the code to add the last vertex is not run. The code to add each vertex
		 * is only run when there is a space, but the last character is not a space, so the final vertex will not be
		 * added to getTriangles3d(), unless we manually add a space.
		 */
		String currentLine = wholeText.get(vertexLineNum) + " ";
		//TODO Technically only one dimension of a vertex, so might want to change
		StringBuilder currentVertexCoord = new StringBuilder("");
		
		for(int i = vertexStartChar; i < currentLine.length(); i++) {
			if(currentLine.charAt(i) == ' ') {
				currentTriangle[vertexIndex][dimensionIndex] = Double.valueOf(currentVertexCoord.toString());
				currentVertexCoord = new StringBuilder("");
				dimensionIndex++;
			}else {
				currentVertexCoord.append(currentLine.charAt(i));
			}
		}
		
	}
	
}
