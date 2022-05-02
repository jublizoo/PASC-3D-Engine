import java.util.ArrayList;

public class Mesh {
	
	ArrayList<Integer[]> triangles;
	ArrayList<Double[]> vertices;
	Double[] center;
	
	public Mesh() {
		triangles = new ArrayList<Integer[]>();
		vertices = new ArrayList<Double[]>();
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
			Double[] point;
			Double[] relativePoint;
			Double[] rotation1 = new Double[] { 0.0, 0.0, 0.0 };
			Double[] rotation2 = new Double[] { 0.0, 0.0, 0.0 };
			Double[] rotation3 = new Double[] { 0.0, 0.0, 0.0 };
			
			for(int i = 0; i < vertices.size(); i++) {
				point = vertices.get(i);
				relativePoint = new Double[] { point[0] - origin[0], point[1] - origin[1], point[2] - origin[2] };
				
				rotation1[0] = relativePoint[0] * Math.cos(angleX) - relativePoint[1] * Math.sin(angleX);
				rotation1[1] = relativePoint[0] * Math.sin(angleX) + relativePoint[1] * Math.cos(angleX);
				rotation1[2] = relativePoint[2];

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
					rotation3[i] += point[i] - relativePoint[i];
				}
				
				vertices.set(i, rotation3);
			}

	}
	
}
