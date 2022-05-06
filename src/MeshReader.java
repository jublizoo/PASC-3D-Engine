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
	
	public void readMesh(String url, Mesh mesh) {	
		file = new File(url);

		try {
			fReader = new FileReader(file);
			reader = new BufferedReader(fReader);
		} catch (FileNotFoundException e) {
			System.out.println(e);
			return;
		}
		
		int lineNum = 0;
		String line = "";
		try { line = reader.readLine(); } catch (IOException e) { e.printStackTrace(); }
		
		//Read vertices
		while(line.charAt(0) == 'v' && line.charAt(1) == ' ') {
			mesh.addVertex(readVertex(line));
			lineNum++;
			try { line = reader.readLine(); } catch (IOException e) { e.printStackTrace(); }
			
		};
		
		//Read UVs
		while(line.charAt(0) == 'v' && line.charAt(1) == 't') {
			mesh.addUV(readUV(line));
			lineNum++;
			try { line = reader.readLine(); } catch (IOException e) { e.printStackTrace(); }
			
		}
		
		ArrayList<Integer> triangles;
		Integer[] triangle;
		//Line is currently set to an irrelevant line, so we read again to start reading triangles
		try { line = reader.readLine(); } catch (IOException e) { e.printStackTrace(); }
		
		//Read triangle indexes and UV triangle indexes
		while(line != null) {
			triangles = readTriangles(line);
			
			triangle = new Integer[] {triangles.get(0) - 1, triangles.get(2) - 1, triangles.get(4) - 1};
			mesh.addTriangle(triangle);
			
			triangle = new Integer[] {triangles.get(1) - 1, triangles.get(3) - 1, triangles.get(5) - 1};
			mesh.addUVTriangle(triangle);
			
			try { line = reader.readLine(); } catch (IOException e) { e.printStackTrace(); }
		}
				
	}
	
	private Double[] readVertex(String line) {
		int charNum = 2;
		StringBuilder num = new StringBuilder("");
		Double[] vertex = new Double[3];
		//Index of vertex to set to num
		int i = 0;
		
		while(charNum < line.length()) {
			if(line.charAt(charNum) == ' ') {
				vertex[i] = Double.parseDouble(num.toString());
				num = new StringBuilder("");
				i++;
			}else {
				num.append(line.charAt(charNum));
			}
			
			charNum++;
		}
		
		vertex[i] = Double.parseDouble(num.toString());
		
		return vertex;
		
	}
	
	private Double[] readUV(String line) {
		int charNum = 3;
		StringBuilder num = new StringBuilder("");
		Double[] uv = new Double[3];
		//Index of vertex to set to num
		int i = 0;
		
		while(charNum < line.length()) {
			if(line.charAt(charNum) == ' ') {
				uv[i] = Double.parseDouble(num.toString());
				num = new StringBuilder("");
				i++;
			}else {
				num.append(line.charAt(charNum));
			}
			
			charNum++;
		}
		
		uv[i] = Double.parseDouble(num.toString());
		
		return uv;
		
	}
	
	private ArrayList<Integer> readTriangles(String line){
			int charNum = 2;
			StringBuilder num = new StringBuilder("");
			ArrayList<Integer> triangles = new ArrayList<Integer>();
			
			while(charNum < line.length()) {
				if(line.charAt(charNum) == ' ' || line.charAt(charNum) == '/') {
					triangles.add(Integer.parseInt(num.toString()));
					num = new StringBuilder("");
				}else {
					num.append(line.charAt(charNum));
				}
				
				charNum++;
			}
			
			triangles.add(Integer.parseInt(num.toString()));
			
			return triangles;
		
	}
	
}
