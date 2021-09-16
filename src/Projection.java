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
	ArrayList<Double[]> points3d = new ArrayList<Double[]>();
	ArrayList<Double[]> points2d = new ArrayList<Double[]>();

	public void createCube() {		
		points3d.add(new Double[] { -0.5, -0.5, -0.5 });
		points3d.add(new Double[] { -0.5, -0.5, 0.5 });
		points3d.add(new Double[] { 0.5, -0.5, -0.5 });
		points3d.add(new Double[] { 0.5, -0.5, 0.5 });
		points3d.add(new Double[] { -0.5, 0.5, -0.5 });
		points3d.add(new Double[] { -0.5, 0.5, 0.5 });
		points3d.add(new Double[] { 0.5, 0.5, -0.5 });
		points3d.add(new Double[] { 0.5, 0.5, 0.5 });

		for (int i = 0; i < points3d.size(); i++) {
			points2d.add(new Double[] { 0.0, 0.0, 0.0 });
		}
		
	}
	
	//TODO Verify math, incorporate zNear
	public Double[] projectPoint(Double[] point3d) {
		Double[] point2d = new Double[3];
		double relativeX = point3d[0] - viewerPos[0];
		double relativeZ = point3d[2] - viewerPos[2];
		double distance = point3d[1] - viewerPos[1];
		
		point2d[0] = screenWidth / 2 + scaleX * (relativeX * zNear / distance);
		point2d[1] = screenHeight / 2 + scaleY * (relativeZ * zNear / distance);
		point2d[2] = distance;
		
		return (point2d);
		
	}
	
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
	
	//TODO Fix variable names
	public void projectAll() {
		scaleX = innerWidth / viewerWidth;
		scaleY = innerHeight / viewerHeight;
		Double[] point2d = new Double[] {};
		
		for(int i = 0; i < points2d.size(); i++) {
			//Angles are negative, because we are rotating to compensate for the angle of the viewers
			point2d = projectPoint(rotatePoint(points3d.get(i), -viewerAngle[0], -viewerAngle[1]));
			
			points2d.get(i)[0] = point2d[0];
			points2d.get(i)[1] = point2d[1];
			points2d.get(i)[2] = point2d[2];
		}
		
	}
	
	
	
}
