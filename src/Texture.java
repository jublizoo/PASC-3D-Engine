import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Texture {
	//Stores all images, correlates with objects in Projection
	ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
	
	public void cycleAllTriangles() {
		
	}
	
	public void cycleTriangleVerts() {
		//Swap verts
		//(two sections)
		//left to right
		//top to bottom
		//find 2d baryCoords
		//interpolateCoords using 2d baryCoords
		//We now have the baryCoords for the 3d triangle, but we will use them on the uv coords
	}
	
	public void calculateBaryCoords(Double[][] triangle, Double[] point) {
		for(int i = 0; i < 3; i++) {
			
		}
	}
	
	public double calculateArea(Double[][] triangle) {
		double area = Math.abs(triangle[0][0] * (triangle[1][1] - triangle[2][1]) +
							triangle[1][0] * (triangle[2][1] - triangle[0][1]) + 
							triangle[2][0] * (triangle[0][1] - triangle[1][1]));
		return area;
	}
	
	public Double[] interpolateCoords(Double[][] triangle, Double[] baryCoords) {
		//The y coordinate of our point
		double y;
		//The final uv coordinates
		double u;
		double v;
		Double[] uv;
		
		/*
		 * Using the barycentric coordinates to find the z-coord. We use triangle[i][1], because we want to access
		 * the y coordinate of our point. Normally this would be referred to as z or w, but we use a different 
		 * coordinate system where z is vertical. Technically, our camera does not rotate (we rotate the world 
		 * instead), so our y-coordinate is the "depth" of each point. The equation is listed below, where 
		 * b1, b2, and b3 are the barycentric coordinates of each point, and v1, v2, and v3 is each point.
		 * 1 / ( ( 1 / v1.y ) * b1 + ( 1 / v2.y ) * b2 + ( 1 / v3.y ) * b3 )
		 */
		y = 1 / (baryCoords[0] / triangle[0][1] + 
				baryCoords[1] / triangle[1][1] + 
				baryCoords[2] / triangle[2][1]);
		
		//y * ( v1.x / v1.y ) * b1 + ( v2.x / v2.y ) * b2 + ( v3.x / v3.y ) * b3
		u = y * (triangle[0][0] * baryCoords[0] / triangle[0][1] + 
				triangle[1][0] * baryCoords[1] / triangle[1][1] + 
				triangle[2][0] * baryCoords[2] / triangle[2][1]);
		//y * ( v1.z / v1.y ) * b1 + ( v2.z / v2.y ) * b2 + ( v3.z / v3.y ) * b3
		v = y * (triangle[1][2] * baryCoords[0] / triangle[0][1] + 
				triangle[1][2] * baryCoords[1] / triangle[1][1] + 
				triangle[2][2] * baryCoords[2] / triangle[2][1]);
		
		uv = new Double[] {u, v};
		return uv;
	}
	
	//% sideways, % down sides, triangle we interpolate to
	public Double[] findTexCoord(Double t, Double v, Double[][] triangle2d) {
		Double[] point1;
		Double[] point2;
		Double[] point3;
		
		point1 = new Double[] {triangle2d[0][0] + v * (triangle2d[1][0] - triangle2d[0][0]),
								triangle2d[0][1] + v * (triangle2d[1][1] - triangle2d[0][1])};
		point2 = new Double[] {triangle2d[0][0] + v * (triangle2d[2][0] - triangle2d[0][0]),
				triangle2d[0][1] + v * (triangle2d[2][1] - triangle2d[0][1])};
		point1 = new Double[] {point1[0] + t * (point2[0] - point1[0]),
								point1[1] + t * (point2[1] - point1[1])};
		return point1;
	}
	//c = new Color(image.getRGB(j, i));
}
