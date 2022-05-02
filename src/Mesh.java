import java.util.ArrayList;

public class Mesh {
	
	private ArrayList<Integer[]> triangles;
	private ArrayList<Double[]> vertices;
	private Double[] center;
	//TODO UV storage
	private ArrayList<Integer[]> uvTriangles;
	private ArrayList<Double[]> uvs;
	
	
	public Mesh() {
		triangles = new ArrayList<Integer[]>();
		vertices = new ArrayList<Double[]>();
		uvTriangles = new ArrayList<Integer[]>();
		uvs = new ArrayList<Double[]>();
		
	}
	
	public void calculateCenter() {
		center = new Double[] {0.0, 0.0, 0.0};
		
		for(int i = 0; i < vertices.size(); i++) {
			for(int b = 0; b < 3; b++) {
				center[b] += vertices.get(i)[b];
			}
		}
		
		for(int i = 0; i < 3; i++) {
			center[i] /= vertices.size();
		}
		
	}
	
	public void move(double x, double y, double z) {
		for(int i = 0; i < vertices.size(); i++) {
			vertices.get(i)[0] += x;
			vertices.get(i)[0] += y;
			vertices.get(i)[0] += z;
		}
		
		calculateCenter();
		
	}
	
	public void rotate(double angleX, double angleY, double angleZ) {
		rotate(center, angleX, angleY, angleZ);
		
	}
	
	//TODO Check functionality
	public void rotate(Double[] origin, double angleX, double angleY, double angleZ) {
			Double[] vertex;
			Double[] relativeVertex;
			Double[] rotation1 = new Double[] { 0.0, 0.0, 0.0 };
			Double[] rotation2 = new Double[] { 0.0, 0.0, 0.0 };
			Double[] rotation3 = new Double[] { 0.0, 0.0, 0.0 };
			
			for(int i = 0; i < vertices.size(); i++) {
				vertex = vertices.get(i);
				relativeVertex = new Double[] { vertex[0] - origin[0], vertex[1] - origin[1], vertex[2] - origin[2] };
				
				rotation1[0] = relativeVertex[0] * Math.cos(angleX) - relativeVertex[1] * Math.sin(angleX);
				rotation1[1] = relativeVertex[0] * Math.sin(angleX) + relativeVertex[1] * Math.cos(angleX);
				rotation1[2] = relativeVertex[2];

				rotation2[1] = rotation1[1] * Math.cos(angleY) - rotation1[2] * Math.sin(angleY);
				rotation2[2] = rotation1[1] * Math.sin(angleY) + rotation1[2] * Math.cos(angleY);
				rotation2[0] = rotation1[0];
				
				rotation3[0] = rotation2[0] * Math.cos(angleZ) - rotation2[2] * Math.sin(angleZ);
				rotation3[2] = rotation2[0] * Math.sin(angleZ) + rotation2[2] * Math.cos(angleZ);
				rotation3[1] = rotation2[1];
				
				for(int b = 0; b < 3; b++) {
					/*
					 * Subtracting the relativePoint, to find the difference in position. The final point is also
					 * relative, but rotated, so we want to find the difference between the final relative point and
					 * the initial relative point. Then, we  have the difference the rotation causes, which we add
					 * to the start point to get the true final point.
					 */
					rotation3[i] += vertex[i] - relativeVertex[i];
				}
				
				vertices.set(i, rotation3);
			}

	}
	
	public void addTriangle(Integer[] triangle) {
		triangles.add(triangle);
		
	}
	
	public void addVertex(Double[] vertex) {
		vertices.add(vertex);
		
	}
	
	public void addUVTriangle(Integer[] uvTriangle) {
		uvTriangles.add(uvTriangle);
		
	}
	
	public void addUV(Double[] uv) {
		uvs.add(uv);
		
	}
	
	public ArrayList<Integer[]> getTriangles() {
		return triangles;
		
	}
	
	public ArrayList<Double[]> getVertices() {
		return vertices;
		
	}
	
	public ArrayList<Integer[]> getUVTriangles() {
		return uvTriangles;
		
	}
	
	public ArrayList<Double[]> getUVs() {
		return uvs;
		
	}
	
}
