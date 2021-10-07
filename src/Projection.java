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
	
	/*
	 * Sorts the triangles3d list, the triangleMidPoints list, and the midPointDistances list, based on the midPointDistances
	 * list. We do not need to sort the triangles2d list, because we will re-project the triangles each time, and we do
	 * so in the order of the already ordered triangles3d list. We cannot calculate triangleMidPoints and midPointDistances
	 * afterwards, because we need updated information from these lists to be able to sort. Therefore, we have to calculate
	 * these lists beforehand, and then sort them along with triangles3d.
	 */
	public void sortLists() {
		//The integer i is the index of the double we are currently sorting.
		for (int i = 1; i < midPointDistances.size(); ++i) {
			//The key is the double we are currently sorting.
			double key = midPointDistances.get(i);
			/*
			 * These are the keys of the elements we are sorting in the other lists. We do not need these to compare
			 * values, but we must store this information, because in the list it will be overwritten when we move
			 * each element to the right.
			 */
			Double[][] key2 = triangles3d.get(i);
			Double[] key3 = triangleMidPoints.get(i);
			//The index of the variable we compare our key to.
			int a = i - 1;
			
			/*
			 * Here, we move backwards, starting from the index of the double we are sorting. We stop if the index is less
			 * than 0, or if the double that we are checking is less than our key. 			 
			 */
			while (a >= 0 && midPointDistances.get(a) < key) {
				//Starting at the element to the left of double we are sorting, we move the element to the right
				midPointDistances.set(a + 1, midPointDistances.get(a));
				triangles3d.set(a + 1, triangles3d.get(a));
				triangleMidPoints.set(a + 1, triangleMidPoints.get(a));
				a--;
			}
			
			/*
			 * We stopped the while loop when we found the FIRST element that our key was smaller than. Our 'a' value 
			 * represents the index of this first element, because we decreased the value of a on the previous iteration
			 * of the while loop. Therefore, we want the element we are sorting to be to the
			 * right of the first smaller element, which will be a + 1. We do not have to worry about overwriting the 
			 * element at a + 1, because we already moved it to the right in the previous iteration of the while loop.
			 */
			midPointDistances.set(a + 1, key);
			triangles3d.set(a + 1, key2);
			triangleMidPoints.set(a + 1, key3);
		}

	}
	
	/*
	 * This function mostly involves vector math that I do not understand. Therefore, the calculations used to set the
	 * final vector will most likely not make sense. The calculations to set vectors one and two are just subtracting
	 * the points on each side that we use.
	 */
	public Double[] calculateVector(Double[][] triangle) {
		//The final vector coordinates
		Double[] vectorFinal = new Double[3];
		Double[] side1 = new Double[3];
		Double[] side2 = new Double[3];
		
		//The vectors of the two sides. Vector one is vertex two minus vertex one. Vector two is vertex three minus vertex one
		for(int i = 0; i < 3; i++) {
			side1[i] = triangle[1][i] - triangle[0][i];
			side2[i] = triangle[2][i] - triangle[0][i];
		}
		
		vectorFinal[0] = side1[1] * side2[2] - side1[2] - side2[1];
		vectorFinal[1] = side1[2] * side2[0] - side1[0] - side2[2];
		vectorFinal[2] = side1[0] * side2[1] - side1[1] - side2[0];
		
		return vectorFinal;
	}
	
}
