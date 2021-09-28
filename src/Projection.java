import java.util.ArrayList;

public class Projection extends Display{
	
	double scaleX;
	double scaleY;
	//The distance from the "viewing box" and the position of the camera
	double zNear = 1.0;
	final double FOV = 1.0;
	final double viewerWidth = Math.tan(FOV / 2.0) * 2.0 * zNear;
	final double viewerHeight = viewerWidth / aspectRatio;
	Double[] viewerPos = new Double[] { 0.0, -5.0, 0.0 };
	Double[] viewerAngle = new Double[] {0.0, 0.0};
	/*
	 * The getTriangles3d() arrayList will store the location of each vertex in each triangle in 3D space. Each Double[][] stores a 
	 * triangle, the first field of the array represents which vertex we are looking for, and the second field of the
	 * array represents which dimension we are looking for (x, y, z). 
	 * The trianglesMid arrayList will store the location of the midpoint of each corresponding triangle. Each Double[]
	 * stores a midpoint, and the field of the array represents which dimension of the midpoint we are looking for 
	 * (x, y, z).
	 * The midDistance arrayList will store the distance of each midpoint to the viewer. Each Double stores a distance
	 * from the viewer to the corresponding midPoint.
	 * The triangles2d arrayList will store the location of each corresponding vertex in each corresponding triangle in
	 * 3D space. Each Double[][] stores a triangle,  the first field of the array represents which vertex we are looking
	 * for, and the second field of the array represents which dimension we are looking for (x, y). There is no need
	 * for a third dimension to represent distance, because we created the midDistance arrayList.
	 */
	ArrayList<Double[][]> triangles3d = new ArrayList<Double[][]>();
	ArrayList<Double[]> triangleMidPoints = new ArrayList<Double[]>();
	ArrayList<Double> midPointDistances = new ArrayList<Double>(); 
	ArrayList<Double[][]> triangles2d = new ArrayList<Double[][]>();
	/*
	 * This stores the start and end index of each object within the getTriangles3d() array. Each element of the ArrayList
	 * represents a different object. Each Double[] stores the index of the first triangle of the object in the 
	 * getTriangles3d() ArrayList, and then the index of the last triangle of the object in the getTriangles3d() ArrayList
	 */
	ArrayList<Integer[]> objIndexes = new ArrayList<Integer[]>();
	
	public synchronized ArrayList<Double[][]> getTriangles3d() {
		return(triangles3d);
	}

	public Double[] projectPoint(Double[] point3d) {
		Double[] point2d = new Double[2];
		double relativeX = point3d[0] - viewerPos[0];
		double relativeZ = point3d[2] - viewerPos[2];
		double distance = point3d[1] - viewerPos[1];
		
		
		for(int i = 0; i < 3; i++) {
			point2d[0] = screenWidth / 2 + scaleX * (relativeX * zNear / distance);
			point2d[1] = screenHeight / 2 + scaleY * (relativeZ * zNear / distance);
		}
		
		return (point2d);
		
	}
	
	//TODO Rewrite this math, and rename variables.
	public Double[] rotatePoint(Double[] startPoint, double angleX, double angleY) {
		Double[] relativePoint = new Double[] { startPoint[0] - viewerPos[0], startPoint[1] - viewerPos[1], startPoint[2] - viewerPos[2] };
		Double[] rotatedPoint = new Double[] { 0.0, 0.0, 0.0 };
		Double[] finalPoint = new Double[] { 0.0, 0.0, 0.0 };

		rotatedPoint[0] = relativePoint[0] * Math.cos(angleX) - relativePoint[1] * Math.sin(angleX);
		rotatedPoint[1] = relativePoint[0] * Math.sin(angleX) + relativePoint[1] * Math.cos(angleX);
		rotatedPoint[2] = startPoint[2];

		finalPoint[1] = rotatedPoint[1] * Math.cos(angleY) - rotatedPoint[2] * Math.sin(angleY) - relativePoint[1]
				+ startPoint[1];
		finalPoint[2] = rotatedPoint[1] * Math.sin(angleY) + rotatedPoint[2] * Math.cos(angleY) - relativePoint[2]
				+ startPoint[2];
		finalPoint[0] = rotatedPoint[0] - relativePoint[0] + startPoint[0];

		return (new Double[] { finalPoint[0], finalPoint[1], finalPoint[2] });
	}
	
	//TODO Replace get() expression with set() function
	public void projectAll() {
		scaleX = innerWidth / viewerWidth;
		scaleY = innerHeight / viewerHeight;
		Double[] point2d = new Double[] {};
		
		for(int a = 0; a < getTriangles3d().size(); a++) {
			for(int b = 0; b < 3; b++) {
				point2d = projectPoint(rotatePoint(getTriangles3d().get(a)[b], -viewerAngle[0], -viewerAngle[1]));
				
				triangles2d.get(a)[b][0] = point2d[0];
				triangles2d.get(a)[b][1] = point2d[1];
			}
		}

	}
	
	public void calculateTriangleMidPoints() {
		Double[] tempAverage = new Double[] {0.0, 0.0, 0.0};
		
		for(int a = 0; a < getTriangles3d().size(); a++) {
			for(int b = 0; b < 3; b++) {
				for(int c = 0; c < 3; c++) {
					tempAverage[c] += getTriangles3d().get(a)[b][c];
				}
			}
			
			for(int i = 0; i < 3; i++) {
				tempAverage[i] /= 3;
			}
			
			triangleMidPoints.set(a, tempAverage);
			tempAverage = new Double[] {0.0, 0.0, 0.0};
		}
		
	}
	
	public void calculateMidPointDistances() {
		Double[] point3d;
		Double[] rotatedPoint3d;
		Double distance;
		
		for(int i = 0; i < triangleMidPoints.size(); i++) {
			point3d = triangleMidPoints.get(i);
			rotatedPoint3d = rotatePoint(point3d, -viewerAngle[0], -viewerAngle[1]);
			distance = rotatedPoint3d[1] - viewerPos[1];
			midPointDistances.set(i, distance);
		}
	}
	
}
