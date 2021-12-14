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
	
	public Double[]  calculateBaryCoords(Double[][] triangle, Double[] point) {
		//The sub triangle opposite the corresponding points
		Double[][] currentTriangle = new Double[3][2];
		//The index of points 1 and 2 of each sub triangle (the third point is the point within the larger triangle)
		int p1;
		int p2;
		//The areas of the sub triangles opposite the corresponding points
		Double[] areas = new Double[3];
		double totalArea = 0;
		Double[] baryCoords = new Double[3];
		
		//Calculating the area of each sub triangle.
		for(int i = 0; i < 3; i++) {
			//Setting p1 and p2 to the "opposite" point on the triangle
			p1 = i + 1;
			p2 = i + 2;
			
			//Looping back around
			if(p1 >= 3) {
				p1 -= 3;
			}
			if(p2 >= 3) {
				p2 -= 3;
			}
			
			currentTriangle[0] = triangle[p1];
			currentTriangle[1] = triangle[p2];
			currentTriangle[2] = point;
		
			areas[i] = calculateArea(currentTriangle);
		}
		
		//Summing totalArea
		for(int i = 0; i < 3; i++) {
			totalArea += areas[i];
		}
		
		//Calculating the final baryentric coordinate values
		for(int i = 0; i < 3; i++) {
			baryCoords[i] = areas[i] / totalArea;
		}
		
		return baryCoords;
	}
	
	public double calculateArea(Double[][] triangle) {
		double area = 0.5 * Math.abs(triangle[0][0] * (triangle[1][1] - triangle[2][1]) +
								triangle[1][0] * (triangle[2][1] - triangle[0][1]) + 
								triangle[2][0] * (triangle[0][1] - triangle[1][1]));
		return area;
	}
	
	public Double[] interpolateCoords(Double[] triangleY, Double[] baryCoords, Double[][] texCoords) {
		//The y coordinate of our point
		double y;
		//The final uv coordinates
		double u;
		double v;
		Double[] uv;
		//this is fax af
		/*
		 * Using the barycentric coordinates to find the z-coord. We use triangle[i][1], because we want to access
		 * the y coordinate of our point. Normally this would be referred to as z or w, but we use a different 
		 * coordinate system where z is vertical. Technically, our camera does not rotate (we rotate the world 
		 * instead), so our y-coordinate is the "depth" of each point. The equation is listed below, where 
		 * b1, b2, and b3 are the barycentric coordinates of each point, and v1, v2, and v3 is each point.
		 * 1 / ( ( 1 / v1.y ) * b1 + ( 1 / v2.y ) * b2 + ( 1 / v3.y ) * b3 )
		 */
		y = 1 / (baryCoords[0] / triangleY[0] + 
				baryCoords[1] / triangleY[1] + 
				baryCoords[2] / triangleY[2]);
		
		//y * (( t1.x / v1.y ) * b1 + ( t2.x / v2.y ) * b2 + ( t3.x / v3.y ) * b3)
		u = y * (texCoords[0][0] * baryCoords[0] / triangleY[0] + 
				texCoords[1][0] * baryCoords[1] / triangleY[1] + 
				texCoords[2][0] * baryCoords[2] / triangleY[2]);
		//y * (( t1.z / v1.y ) * b1 + ( t2.z / v2.y ) * b2 + ( t3.z / v3.y ) * b3)
		v = y * (texCoords[0][1] * baryCoords[0] / triangleY[0] + 
				texCoords[1][1] * baryCoords[1] / triangleY[1] + 
				texCoords[2][1] * baryCoords[2] / triangleY[2]);
		
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
