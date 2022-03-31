import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Render extends JPanel implements Runnable{
	
	Projection p;
	Main m;
	Double[][] zBuffer;
	
	Graphics2D g2d;
	
	BufferedImage texture;
	BufferedImage finalImg;
	
	ArrayList<Thread> threads;
	ArrayList<BufferedImage> imageSections;
	ArrayList<Double[]> corner1s;
	ArrayList<Double[]> corner2s;

	public Render(Main m) {
		p = new Projection(m);
		this.m = m;
		loadImages();
		
	}
	
	@Override
	public void run() {		
		textureTriangles(g2d);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		//p.setDisplayDimensions();
		this.g2d = (Graphics2D) g;
		
		//
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, p.screenWidth, p.screenHeight);
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fillRect((int) p.startX, (int) p.startY, (int) p.innerWidth, (int) p.innerHeight);
		g2d.setColor(Color.BLACK);
		
		initializeThreads();
		
		try {
			for(Thread thread : threads) {
				thread.join();
			}
		}catch(Exception e) {
			System.out.println("Could not join threads");
		}
		
		drawImages(g2d);
		
	}
	
	public void initializeThreads() {
		threads = new ArrayList<Thread>();
		
		for(int i = 0; i < 4; i++) {
			threads.add(new Thread(this, "" + i));
		}
		
		imageSections = new ArrayList<BufferedImage>();
		
		for(int i = 0; i < threads.size(); i++) {
			if(p.innerWidth != 0) {
				imageSections.add(new BufferedImage((int) (p.innerWidth / 2.0), (int) (p.innerHeight / 2.0), BufferedImage.TYPE_INT_ARGB));
			}else {
				System.out.println("bals");
				return;
			}
		}
		
		corner1s = new ArrayList<Double[]>();
		
		corner1s.add(new Double[] {0.0, 0.0});
		corner1s.add(new Double[] {Math.floor(p.innerWidth / 2), 0.0});
		corner1s.add(new Double[] {0.0, Math.floor(p.innerHeight / 2)});
		corner1s.add(new Double[] {Math.floor(p.innerWidth / 2), Math.floor(p.innerHeight / 2)});
		
		corner2s = new ArrayList<Double[]>();
		
		for(int i = 0; i < corner1s.size(); i++) {
			corner2s.add(new Double[] {Math.floor(corner1s.get(i)[0] + p.innerWidth / 2), Math.floor(corner1s.get(i)[1] + p.innerHeight / 2)});
		}
		
		for(Thread t : threads) {
			t.start();
		}
	}
	
	//Draws the corresponding images for each thread
	public void drawImages(Graphics g2d) {
		int x;
		int y;
		int width = (int) (p.innerWidth / 2.0);
		int height = (int) (p.innerHeight / 2.0);
		for(int i = 0; i < imageSections.size(); i++) {
			x = (int) (double) (p.startX + corner1s.get(i)[0]);
			y = (int) (double) (p.startY + corner1s.get(i)[1]);
			
			g2d.drawImage(imageSections.get(i), x, y, width, height, null);
		}
	}
	
	public void loadImages() {
		try {
			texture = ImageIO.read(new File("download.png"));
		} catch (IOException e) {	}
	}
	
	//TODO Check if each point is within the inner window
	public void drawPoints(Graphics2D g2d) {
		g2d.setColor(Color.BLACK);
		int x;
		int y;
		p.calculateTriangleMidPoints();
		p.calculateMidPointDistances();
		p.sortLists();
		p.projectAll();
		
		for(int a = 0; a < p.triangles2d.size(); a++) {
			if(p.midPointDistances.get(a) > p.zNear / 2) {
				for(int b = 0; b < 3; b++) {
					x = (int) Math.round(p.triangles2d.get(a)[b][0]);
					y = (int) Math.round(p.triangles2d.get(a)[b][1]);
					g2d.fillOval(x, y, 5, 5);
				}
			}
		}
				
	}	
	
	//Corner 1 is the topleft corner. Corner 2 is the bottomright corner
	public ArrayList<Double[][]> clipTriangle(Double[][] originalTriangle, Double[] corner1, Double[] corner2) {
		ArrayList<Double[][]> clippedTriangles = new ArrayList<Double[][]>();
		clippedTriangles.add(originalTriangle);
		//The starting triangle (may have already been clipped on other boundaries)
		Double[][] triangle = null;
		//The triangle after being clipped on the current boundary
		Double[][] clippedTriangle = new Double[3][2];
		
		//If a point is inside the bounding box
		boolean inside;
		int[] insidePoints;
		int insideSize;
		int[] outsidePoints;
		int outsideSize;
		
		//initial size of the clipped triangle array 
		int initialSize;
		
		//The 2 intersections with the current line (if it intersects)
		Double[][] intersections;
				
		for(int i = 0; i < 4; i++) {
			//We only want to cycle to the initial size. We will be adding new triangles, and if we use the actual size, it will continue removing the new triangles.
			initialSize = clippedTriangles.size();
			for(int b = 0; b < initialSize; b++) {
				insidePoints = new int[3];
				insideSize = 0;
				outsidePoints = new int[3];
				outsideSize = 0;
				
				triangle = clippedTriangles.get(0);
				clippedTriangles.remove(0);
				
				for(int c = 0; c < 3; c++) {
					inside = false;
					
					switch(i) {
					//top
					case 0:
						if(triangle[c][1] > corner1[1]) {
							inside = true;
						}
						break;
					//bottom
					case 1:
						if(triangle[c][1] < corner2[1]) {
							inside = true;
						}
						break;
					//left
					case 2:
						if(triangle[c][0] > corner1[0]) {
							inside = true;
						}
						break;
					//right
					case 3:
						if(triangle[c][0] < corner2[0]) {
							inside = true;
						}
						break;
					}
					
					if(inside) {
						insidePoints[insideSize] = (c); 
						insideSize++;
					}else {
						outsidePoints[outsideSize] = (c); 
						outsideSize++;
					}
				}
				
				intersections = new Double[2][2];
				
				//If a triangle is outside (outsideSize == 3), we do nothing. The triangle is already removed.
				if(outsideSize == 2) {						
					switch(i) {
					//top
					case 0:
						intersections = collision(corner1[1], triangle[outsidePoints[0]], triangle[outsidePoints[1]], triangle[insidePoints[0]], true);
						break;
					//bottom
					case 1:
						intersections = collision(corner2[1], triangle[outsidePoints[0]], triangle[outsidePoints[1]], triangle[insidePoints[0]], true);
						break;
					//left
					case 2:
						intersections = collision(corner1[0], triangle[outsidePoints[0]], triangle[outsidePoints[1]], triangle[insidePoints[0]], false);
						break;
					//right
					case 3:
						intersections = collision(corner2[0], triangle[outsidePoints[0]], triangle[outsidePoints[1]], triangle[insidePoints[0]], false);
						break;
					}
											
					clippedTriangle[0] = triangle[insidePoints[0]];
					clippedTriangle[1] = intersections[0];
					clippedTriangle[2] = intersections[1];
					clippedTriangles.add(clippedTriangle.clone());
				}else if(outsideSize == 1) {
					switch(i) {
					//top
					case 0:	
						intersections = collision(corner1[1], triangle[insidePoints[0]], triangle[insidePoints[1]], triangle[outsidePoints[0]], true);
						break;
					//bottom
					case 1:
						intersections = collision(corner2[1], triangle[insidePoints[0]], triangle[insidePoints[1]], triangle[outsidePoints[0]], true);
						break;
					//left
					case 2:
						intersections = collision(corner1[0], triangle[insidePoints[0]], triangle[insidePoints[1]], triangle[outsidePoints[0]], false);
						break;
					//right
					case 3:
						intersections = collision(corner2[0], triangle[insidePoints[0]], triangle[insidePoints[1]], triangle[outsidePoints[0]], false);
						break;
					}
					
					clippedTriangle[0] = triangle[insidePoints[0]];
					clippedTriangle[1] = intersections[0];
					clippedTriangle[2] = intersections[1];
					clippedTriangles.add(clippedTriangle.clone());
					
					//The commented line is uneccesary because it already has that value.
					clippedTriangle[0] = triangle[insidePoints[0]];
					clippedTriangle[1] = triangle[insidePoints[1]];
					clippedTriangle[2] = intersections[1];
					clippedTriangles.add(clippedTriangle.clone());
				}else if(outsideSize == 0){
					//If the triangle is fully inside, we want to add it back (we already removed it)
					clippedTriangles.add(triangle.clone());
				}
			}
		}	
		
		return clippedTriangles;
			
	}
	
	//Triangle collision, p3 is the point that touches both lines which will be tested for collision
	Double[][] collision(Double l1, Double[] p1, Double[] p2, Double[] p3, boolean horizontal){
		//Each intersection point
		Double[] i1 = new Double[2];
		Double[] i2 = new Double[2];
		
		if(horizontal) {
			i1[0] = p1[0] + (l1 - p1[1]) * (p3[0] - p1[0]) / (p3[1] - p1[1]);
			i1[1] = l1;
			
			i2[0] = p2[0] + (l1 - p2[1]) * (p3[0] - p2[0]) / (p3[1] - p2[1]);
			i2[1] = l1;
		}else{
			i1[0] = l1;
			i1[1] = p1[1] + (l1 - p1[0]) * (p3[1] - p1[1]) / (p3[0] - p1[0]);
			
			i2[0] = l1;
			i2[1] = p2[1] + (l1 - p2[0]) * (p3[1] - p2[1]) / (p3[0] - p2[0]);
		}
		
		return new Double[][] {i1, i2};
		
	}
	
	int cycle(int in, int mod) {
		int out;
		
		if(in >= mod) {
			out = in % mod;
		}else {
			out = in % mod;
			
			if(out != 0) {
				out += mod;
			}
		}
		
		return out;
		
	}
	
	//Tex coords are not sorted
	public void textureTriangles(Graphics2D g2d) {
		int threadNumber;
		
		//The surface normal of a given triangle
		Double[] triangleVector;
		Double[] viewerVector;
		Double[] viewerToTriangleVector;
		Double angle;
		Double angle2;	
		
		Double[][] triangle;
		ArrayList<Double[][]> clippedTriangles;
		
		p.calculateTriangleMidPoints();
		p.calculateMidPointDistances();
		p.sortLists();
		p.projectAll();
		
		//Update z-buffer size / reset values to null
		zBuffer = new Double[(int) p.innerWidth][(int) p.innerHeight];
		
		for(int a = 0; a < p.triangles2d.size(); a++) {
			
			viewerToTriangleVector = new Double[] {p.triangleMidPoints.get(a)[0] - p.viewerPos[0], p.triangleMidPoints.get(a)[1] - p.viewerPos[1], p.triangleMidPoints.get(a)[2] - p.viewerPos[2]};
			triangleVector = p.calculateVector(p.triangles3d.get(a));
			angle2 = p.calculateVectorAngle(triangleVector, viewerToTriangleVector);
						
			//if(p.midPointDistances.get(a) > 0 && Math.abs(angle2) > Math.PI / 2) {			
				viewerVector = new Double[] {-Math.sin(p.viewerAngle[0]), Math.cos(p.viewerAngle[0]), 0.0};
				angle = p.calculateVectorAngle(triangleVector, viewerVector);
				
				threadNumber = Integer.parseInt(Thread.currentThread().getName());
				clippedTriangles = clipTriangle(p.triangles2d.get(a), corner1s.get(threadNumber), corner2s.get(threadNumber));
				//clippedTriangles = new ArrayList<Double[][]>();
				//clippedTriangles.add(p.triangles2d.get(a));
				
				for(int b = 0; b < clippedTriangles.size(); b++) {
					//TODO use the original triangle as an input, for interpolation
					traverse(clippedTriangles.get(b), p.triangles2d.get(a), g2d, a, angle);
				}
			//}
		}
		
	}
	
	public void traverse(Double[][] clippedTriangle, Double[][] originalTriangle, Graphics2D g2d, int triangleNum, double angle) {	
		int threadNumber = Integer.parseInt(Thread.currentThread().getName());
		BufferedImage img = imageSections.get(threadNumber);
		
		Double[] temp;
		
		Integer[] p1 = new Integer[2];
		Integer[] p2 = new Integer[2];
		Integer[] p3 = new Integer[2];
		
		//Slope from points 1 - 2
		double m1;
		//Slope from points 1 - 3
		double m2;
		//Slope from points 2 - 3
		double m3;
		
		Double[] triangleY = new Double[3];
		Double[][] texCoords = p.triangleUvs.get(triangleNum);
		Double[][] triangle = clippedTriangle.clone();
		
		//The starting and ending x values for each triangle "slice" (startX was already taken)
		int x1;
		int x2;
		
		for(int i = 0; i < 3; i++) {
			triangleY[i] = p.rotatePoint(p.triangles3d.get(triangleNum)[i], -p.viewerAngle[0], -p.viewerAngle[1])[1] - p.viewerPos[1];
		}	 

		//Setting p1 to the largest y-value point
		for(int i = 1; i < 3; i++) {
			if(triangle[i][1] 
					> 
			triangle[0][1]) {
				//Swap
				temp = triangle[i];
				triangle[i] = triangle[0];
				triangle[0] = temp;
			}
		}
		
		//Setting p2 to the second largest y-value point
		if(triangle[1][1] < triangle[2][1]) {
			//Swap
			temp = triangle[1];
			triangle[1] = triangle[2];
			triangle[2] = temp;
		}
		
		p1[0] = (int) Math.round(triangle[0][0]);
		p1[1] = (int) Math.round(triangle[0][1]);
		p2[0] = (int) Math.round(triangle[1][0]);
		p2[1] = (int) Math.round(triangle[1][1]);
		p3[0] = (int) Math.round(triangle[2][0]);
		p3[1] = (int) Math.round(triangle[2][1]);
						
		m1 = (double) (p2[1] - p1[1]) / (p2[0] - p1[0]);
		m2 = (double) (p3[1] - p1[1]) / (p3[0] - p1[0]);
		m3 = (double) (p3[1] - p2[1]) / (p3[0] - p2[0]);
		
		g2d.setColor(Color.BLACK);
		
		//Upper part of triangle, going from line 1 - 2;
		for(int i = p1[1]; i > p2[1]; i--) {
			x1 = (int) Math.round(p1[0] + (i - p1[1]) / m1);
			x2 = (int) Math.round(p1[0] + (i - p1[1]) / m2);
			
			if(x1 > x2) {
				for(int b = x1; b >= x2; b--) {
					if(b >= corner1s.get(threadNumber)[0] && b < corner2s.get(threadNumber)[0] && i >= corner1s.get(threadNumber)[1] && i < corner2s.get(threadNumber)[1]) {
						drawPoint(g2d, i, b, originalTriangle, triangleY, texCoords, angle);
					}
				}
			} else if(x1 < x2) {
				for(int b = x1; b <= x2; b++) {
					if(b >= corner1s.get(threadNumber)[0] && b < corner2s.get(threadNumber)[0] && i >= corner1s.get(threadNumber)[1] && i < corner2s.get(threadNumber)[1]) {
						drawPoint(g2d, i, b, originalTriangle, triangleY, texCoords, angle);
					}
				}
			}
		}
		
		//Lower part of triangle, going from line 3 - 2
		for(int i = p2[1]; i > p3[1]; i--) {
			x1 = (int) Math.round(p2[0] + (i - p2[1]) / m3);
			x2 = (int) Math.round(p1[0] + (i - p1[1]) / m2);
			
			if(x1 > x2) {
				for(int b = x1; b >= x2; b--) {
					if(b >= corner1s.get(threadNumber)[0] && b < corner2s.get(threadNumber)[0] && i >= corner1s.get(threadNumber)[1] && i < corner2s.get(threadNumber)[1]) {
						drawPoint(g2d, i, b, originalTriangle, triangleY, texCoords, angle);
					}
				}
			} else if(x1 < x2) {
				for(int b = x1; b <= x2; b++) {
					if(b >= corner1s.get(threadNumber)[0] && b < corner2s.get(threadNumber)[0] && i >= corner1s.get(threadNumber)[1] && i < corner2s.get(threadNumber)[1]) {
						drawPoint(g2d, i, b, originalTriangle, triangleY, texCoords, angle);
					}
				}
			}
		}
		
	}
	
	public void drawPoint(Graphics2D g2d, int i, int b, Double[][] originalTriangle, Double[] triangleY, Double[][] texCoords, double angle) {
		double lightLevel;
		Color color;
		Double[] point;
		Double[] uv;
		point = new Double[] {(double) b, (double) i};
		
		int threadNumber = Integer.parseInt(Thread.currentThread().getName());
		BufferedImage img = imageSections.get(threadNumber);
		//For specific threads, not the absolute x (adjusted for corner1)
		int x;
		int y;
	
		
		uv = interpolateCoords(triangleY, p.calculateBaryCoords(originalTriangle, point), texCoords, b, i);
		
		if(uv == null) {
			return;
		}
		
		uv[0] *= texture.getWidth();
		uv[1] *= texture.getHeight();
		
		if(uv[0] >= texture.getWidth()) {
			uv[0] = (double) (texture.getWidth() - 1);
		}else if(uv[0] <= 0) {
			uv[0] = 1.0;
		}
		if(uv[1] >= texture.getHeight()) {
			uv[1] = (double) texture.getHeight() - 1;
		}else if(uv[1] <= 0) {
			uv[1] = 1.0;
		}
		
		lightLevel = calculateLight(angle);
		color = new Color(texture.getRGB((int)(double) (uv[0]), (int)(double) (uv[1])));
		color = new Color((int) (color.getRed() * lightLevel), (int) (color.getGreen() * lightLevel), (int) (color.getBlue() * lightLevel), 254);
		g2d.setColor(color);
		
		x = (int) (b - corner1s.get(threadNumber)[0]);
		y = (int) (i - corner1s.get(threadNumber)[1]);
		
		if(x >= img.getWidth()) {
			x = img.getWidth() - 1;
		}
		if(y >= img.getHeight()) {
			y = img.getHeight() - 1;
		}
		img.setRGB(x, y, color.getRGB());
				
	}
	
	//TODO Fix bug where triangles show up while looking sideways. Maybe each point is out of range but its drawing both?
	public void drawTriangles(Graphics2D g2d) {
		int random;
		Color c;
		//The direction of the viewer
		Double[] viewerVector;
		//The surface normal of a given triangle
		Double[] triangleVector;
		//The vector starting at the viewer, going to the midpoint of a given triangle.
		Double[] viewerToTriangleVector;
		//Angle between the direction of a certain vector (usually a light), and the direction of a given triangle
		Double angle;
		/*
		 * This angle is between the direction a triangle is facing, and the vector starting at the viewer,
		 * going to the midpoint of this triangle. It is used for backface culling. We cannot use our first 
		 * angle for back face culling, because it results in faces that you should be able to see, but are
		 * not rendered. Imagine your rotate the camera to face the triangle. In the case of the first angle
		 * calculation, the angle would change, which could affect if the triangle is displayed. This should
		 * not happen, because rotating the camera should not change what faces are displayed.
		 */
		Double angle2;
		int lightLevel;
		p.calculateTriangleMidPoints();
		p.calculateMidPointDistances();
		p.sortLists();
		p.projectAll();
		
		for(int a = 0; a < p.triangles2d.size(); a++) {
			viewerVector = new Double[] {-Math.sin(p.viewerAngle[0]), Math.cos(p.viewerAngle[0]), 0.0};
			viewerToTriangleVector = new Double[] {p.triangleMidPoints.get(a)[0] - p.viewerPos[0], p.triangleMidPoints.get(a)[1] - p.viewerPos[1], p.triangleMidPoints.get(a)[2] - p.viewerPos[2]};
			triangleVector = p.calculateVector(p.triangles3d.get(a));
			angle = p.calculateVectorAngle(triangleVector, viewerVector);
			angle2 = p.calculateVectorAngle(triangleVector, viewerToTriangleVector);
			System.out.println(angle2);

			
			if(p.midPointDistances.get(a) > 0 && Math.abs(angle2) > Math.PI / 2) {
				lightLevel = (int) (255 * calculateLight(angle));
				c = new Color(lightLevel, lightLevel, lightLevel);
				
				g2d.setColor(c);
				
				Path2D.Double triangle = new Path2D.Double();
				triangle.moveTo(p.triangles2d.get(a)[0][0], p.triangles2d.get(a)[0][1]);
				triangle.lineTo(p.triangles2d.get(a)[1][0], p.triangles2d.get(a)[1][1]);
				triangle.lineTo(p.triangles2d.get(a)[2][0], p.triangles2d.get(a)[2][1]);
				triangle.closePath(); 
				g2d.fill(triangle);
			}
		}
				
	}
	
	public Double[] interpolateCoords(Double[] triangleY, Double[] baryCoords, Double[][] texCoords, int b, int i) {
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
		y = 1.0 / (baryCoords[0] / triangleY[0] + 
				baryCoords[1] / triangleY[1] + 
				baryCoords[2] / triangleY[2]);
		
		Double bufferedY  = zBuffer[b][i];
		
		if(bufferedY != null) {
			if(bufferedY < y) {
				return null;
			}
		}
		
		zBuffer[b][i] = y;
		
		//y * (( t1.x / v1.y ) * b1 + ( t2.x / v2.y ) * b2 + ( t3.x / v3.y ) * b3)
		u = y * ((texCoords[0][0] * baryCoords[0] / triangleY[0]) + 
				(texCoords[1][0] * baryCoords[1] / triangleY[1]) + 
				(texCoords[2][0] * baryCoords[2] / triangleY[2]));
		//y * (( t1.z / v1.y ) * b1 + ( t2.z / v2.y ) * b2 + ( t3.z / v3.y ) * b3)
		v = y * ((texCoords[0][1] * baryCoords[0] / triangleY[0]) + 	
				(texCoords[1][1] * baryCoords[1] / triangleY[1]) + 
				(texCoords[2][1] * baryCoords[2] / triangleY[2]));
		/*
		u = (texCoords[0][0] * baryCoords[0] + 
				texCoords[1][0] * baryCoords[1] + 
				texCoords[2][0] * baryCoords[2]);
		v = (texCoords[0][1] * baryCoords[0]  + 
				texCoords[1][1] * baryCoords[1]  + 
				texCoords[2][1] * baryCoords[2]);
		*/
		
		uv = new Double[] {u, v};
		return uv;
	}
	
	public double calculateLight(Double angle) {
		double lightLevel =  ((angle / Math.PI));
		return lightLevel;
		
	}

}
