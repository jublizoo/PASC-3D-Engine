import java.util.ArrayList;

public class Projection extends Display{
	double scaleX;
	double scaleY;
	//The diagonal distance from the "viewing box" and the position of the camera
	double zNear = 1.0;
	final double FOV = 1.0;
	final double viewerWidth = Math.sin(FOV / 2.0) * 2.0 * zNear;
	final double viewerHeight = viewerWidth / aspectRatio;
	Double[] viewerPos = new Double[] { 0.0, 0.0, 0.0 };
	Double[] viewerAngle = new Double[] {0.2, 0.0};
	ArrayList<Double[]> points3d = new ArrayList<Double[]>();
	ArrayList<Double[]> points2d = new ArrayList<Double[]>();

	public void createCube() {		
		points3d.add(new Double[] { -0.5, -0.5, 5.0 });
		points3d.add(new Double[] { -0.5, -0.5, 6.0 });
		points3d.add(new Double[] { 0.5, -0.5, 5.0 });
		points3d.add(new Double[] { 0.5, -0.5, 6.0 });
		points3d.add(new Double[] { -0.5, 0.5, 5.0 });
		points3d.add(new Double[] { -0.5, 0.5, 6.0 });
		points3d.add(new Double[] { 0.5, 0.5, 5.0 });
		points3d.add(new Double[] { 0.5, 0.5, 6.0 });

		for (int i = 0; i < points3d.size(); i++) {
			points2d.add(new Double[] { 0.0, 0.0, 0.0 });
		}
		
	}
	
	public Double[] projectPoint(Double[] point) {
		
		return(new Double[] {0.0, 0.0, 0.0});
	}
	
}
