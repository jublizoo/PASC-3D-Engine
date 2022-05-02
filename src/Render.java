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
	
	Main m;
	volatile Double[][] zBuffer;

	Graphics2D g2d;
	
	BufferedImage texture;
	BufferedImage finalImg;
	
	ArrayList<Thread> threads;
	ArrayList<BufferedImage> imageSections;
	ArrayList<Double[]> corner1s;
	ArrayList<Double[]> corner2s;
	
	int screenWidth;
	int screenHeight;
	boolean useWidth;
	double aspectRatio = 16.0f / 9.0f;
	double innerWidth;
	double innerHeight;
	double startX;
	double startY;
	
	double scaleX;
	double scaleY;
	//The distance from the "viewing box" and the position of the camera
	double zNear = 1.0;
	final double FOV = 1.0;
	final double viewerWidth = Math.tan(FOV / 2.0) * 2.0 * zNear;
	final double viewerHeight = viewerWidth / aspectRatio;
	Double[] viewerPos = new Double[] { -5.0, -5.0, 0.0 };
	Double[] viewerAngle = new Double[] {-0.78, 0.0};
	
	/*
	 * The triangles arrayList will store the location of each vertex in each triangle in 3D space. Each Double[][] stores a 
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
	ArrayList<Double[][]> triangles = new ArrayList<Double[][]>();
	ArrayList<Double[]> triangleMidPoints = new ArrayList<Double[]>();
	ArrayList<Double> midPointDistances = new ArrayList<Double>(); 
	ArrayList<Double[][]> triangles2d = new ArrayList<Double[][]>();
	ArrayList<Double[][]> triangleUvs = new ArrayList<Double[][]>();
	/*
	 * This stores the start and end index of each object within the triangles array. Each element of the ArrayList
	 * represents a different object. Each Double[] stores the index of the first triangle of the object in the 
	 * triangles ArrayList, and then the index of the last triangle of the object in the triangles ArrayList
	 */
	ArrayList<Integer[]> objIndexes = new ArrayList<Integer[]>();

	public Render(Main m) {
		this.m = m;
		loadImages();
		
	}
	
	@Override
	public void run() {		
		textureTriangles();
		//Either access of triangles/2d, or the projection function being called multiple times could be the issue
		//Also maybe clip the whole triangles 2d and not each one at a time
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		//setDisplayDimensions();
		this.g2d = (Graphics2D) g;
		
		//
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, screenWidth, screenHeight);
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fillRect((int) startX, (int) startY, (int) innerWidth, (int) innerHeight);
		g2d.setColor(Color.BLACK);
		
		zBuffer = new Double[(int) innerWidth][(int) innerHeight];
		
		calculateTriangleMidPoints();
		calculateMidPointDistances();
		sortLists();
		projectAll();
		
		initializeThreads();
		
		try {
			for(Thread thread : threads) {
				thread.join();
			}
		}catch(Exception e) { 	
			System.out.println("Could not join threads");
		}
		
		drawImages();
		
	}
	
	public void initializeThreads() { 
		threads = new ArrayList<Thread>();
		
		for(int i = 0; i < 4; i++) {
			threads.add(new Thread(this, "" + i));
		}
		
		imageSections = new ArrayList<BufferedImage>();
		
		for(int i = 0; i < threads.size(); i++) {
			if(innerWidth != 0) {
				imageSections.add(new BufferedImage((int) (innerWidth / 2.0), (int) (innerHeight / 2.0), BufferedImage.TYPE_INT_ARGB));
			}else {
				System.out.println("bals");
				return;
			}
		}
		
		corner1s = new ArrayList<Double[]>();
		
		corner1s.add(new Double[] {0.0, 0.0});
		corner1s.add(new Double[] {Math.floor(innerWidth / 2), 0.0});
		corner1s.add(new Double[] {0.0, Math.floor(innerHeight / 2)});
		corner1s.add(new Double[] {Math.floor(innerWidth / 2), Math.floor(innerHeight / 2)});
		
		corner2s = new ArrayList<Double[]>();
		
		for(int i = 0; i < corner1s.size(); i++) {
			corner2s.add(new Double[] {Math.floor(corner1s.get(i)[0] + innerWidth / 2), Math.floor(corner1s.get(i)[1] + innerHeight / 2)});
		}
		
		for(Thread t : threads) {
			t.start();
		}
	}
	
	//Draws the corresponding images for each thread
	public void drawImages() {
		int x;
		int y;
		int width = (int) (innerWidth / 2.0);
		int height = (int) (innerHeight / 2.0);
		for(int i = 0; i < imageSections.size(); i++) {
			x = (int) (double) (startX + corner1s.get(i)[0]);
			y = (int) (double) (startY + corner1s.get(i)[1]);
			
			g2d.drawImage(imageSections.get(i), x, y, width, height, null);
		}
	}
	
	public void loadImages() {
		try {
			texture = ImageIO.read(new File("download.png"));
		} catch (IOException e) {	}
	}
	
	//TODO Check if each point is within the inner window
	public void drawPoints() {
		g2d.setColor(Color.BLACK);
		int x;
		int y;
		calculateTriangleMidPoints();
		calculateMidPointDistances();
		sortLists();
		projectAll();
		
		for(int a = 0; a < triangles2d.size(); a++) {
			if(midPointDistances.get(a) > zNear / 2) {
				for(int b = 0; b < 3; b++) {
					x = (int) Math.round(triangles2d.get(a)[b][0]);
					y = (int) Math.round(triangles2d.get(a)[b][1]);
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
	public void textureTriangles() {
		int threadNumber;
		
		//The surface normal of a given triangle
		Double[] triangleVector;
		Double[] viewerVector;
		Double[] viewerToTriangleVector;
		Double angle;
		Double angle2;	
		
		Double[][] triangle;
		ArrayList<Double[][]> clippedTriangles;
		
		//Update z-buffer size / reset values to null
		//zBuffer = new Double[(int) innerWidth][(int) innerHeight];
		
		for(int a = 0; a < triangles2d.size(); a++) {
			
			viewerToTriangleVector = new Double[] {triangleMidPoints.get(a)[0] - viewerPos[0], triangleMidPoints.get(a)[1] - viewerPos[1], triangleMidPoints.get(a)[2] - viewerPos[2]};
			triangleVector = calculateVector(triangles.get(a));
			angle2 = calculateVectorAngle(triangleVector, viewerToTriangleVector);
						
			if(midPointDistances.get(a) > 0.5 && Math.abs(angle2) > Math.PI / 2) {			
				viewerVector = new Double[] {-Math.sin(viewerAngle[0]), Math.cos(viewerAngle[0]), 0.0};
				angle = calculateVectorAngle(triangleVector, viewerVector);
				
				threadNumber = Integer.parseInt(Thread.currentThread().getName());
				clippedTriangles = clipTriangle(triangles2d.get(a), corner1s.get(threadNumber), corner2s.get(threadNumber));
				//clippedTriangles = new ArrayList<Double[][]>();
				//clippedTriangles.add(triangles2d.get(a));
				
				for(int b = 0; b < clippedTriangles.size(); b++) {
					//TODO use the original triangle as an input, for interpolation
					traverse(clippedTriangles.get(b), triangles2d.get(a), a, angle);
				}
			}
		}
		
	}
	
	public void traverse(Double[][] clippedTriangle, Double[][] originalTriangle, int triangleNum, double angle) {	
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
		Double[][] texCoords = triangleUvs.get(triangleNum);
		Double[][] triangle = clippedTriangle.clone();
		
		//The starting and ending x values for each triangle "slice" (startX was already taken)
		int x1;
		int x2;
		
		for(int i = 0; i < 3; i++) {
			triangleY[i] = rotatePoint(triangles.get(triangleNum)[i], -viewerAngle[0], -viewerAngle[1])[1] - viewerPos[1];
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
						drawPoint(i, b, originalTriangle, triangleY, texCoords, angle);
					}
				}
			} else if(x1 < x2) {
				for(int b = x1; b <= x2; b++) {
					if(b >= corner1s.get(threadNumber)[0] && b < corner2s.get(threadNumber)[0] && i >= corner1s.get(threadNumber)[1] && i < corner2s.get(threadNumber)[1]) {
						drawPoint(i, b, originalTriangle, triangleY, texCoords, angle);
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
						drawPoint(i, b, originalTriangle, triangleY, texCoords, angle);
					}
				}
			} else if(x1 < x2) {
				for(int b = x1; b <= x2; b++) {
					if(b >= corner1s.get(threadNumber)[0] && b < corner2s.get(threadNumber)[0] && i >= corner1s.get(threadNumber)[1] && i < corner2s.get(threadNumber)[1]) {
						drawPoint(i, b, originalTriangle, triangleY, texCoords, angle);
					}
				}
			}
		}
		
	}
	
	public void drawPoint(int i, int b, Double[][] originalTriangle, Double[] triangleY, Double[][] texCoords, double angle) {
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
	
		
		uv = interpolateCoords(triangleY, calculateBaryCoords(originalTriangle, point), texCoords, b, i);
		
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
	public void drawTriangles() {
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
		calculateTriangleMidPoints();
		calculateMidPointDistances();
		sortLists();
		projectAll();
		
		for(int a = 0; a < triangles2d.size(); a++) {
			viewerVector = new Double[] {-Math.sin(viewerAngle[0]), Math.cos(viewerAngle[0]), 0.0};
			viewerToTriangleVector = new Double[] {triangleMidPoints.get(a)[0] - viewerPos[0], triangleMidPoints.get(a)[1] - viewerPos[1], triangleMidPoints.get(a)[2] - viewerPos[2]};
			triangleVector = calculateVector(triangles.get(a));
			angle = calculateVectorAngle(triangleVector, viewerVector);
			angle2 = calculateVectorAngle(triangleVector, viewerToTriangleVector);
			System.out.println(angle2);

			
			if(midPointDistances.get(a) > 0 && Math.abs(angle2) > Math.PI / 2) {
				lightLevel = (int) (255 * calculateLight(angle));
				c = new Color(lightLevel, lightLevel, lightLevel);
				
				g2d.setColor(c);
				
				Path2D.Double triangle = new Path2D.Double();
				triangle.moveTo(triangles2d.get(a)[0][0], triangles2d.get(a)[0][1]);
				triangle.lineTo(triangles2d.get(a)[1][0], triangles2d.get(a)[1][1]);
				triangle.lineTo(triangles2d.get(a)[2][0], triangles2d.get(a)[2][1]);
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
		
	public Double[] projectPoint(Double[] point) {
		Double[] point2d = new Double[2];
		double relativeX = point[0] - viewerPos[0];
		double relativeZ = point[2] - viewerPos[2];
		double distance = point[1] - viewerPos[1];
		
		
		for(int i = 0; i < 3; i++) {
			point2d[0] = innerWidth / 2 + scaleX * (relativeX * zNear / distance);
			point2d[1] = innerHeight / 2 + scaleY * (relativeZ * zNear / distance);
		}
		
		return (point2d);
		
	}
	
	//TODO Rewrite this math, and rename variables.
	public Double[] rotatePoint(Double[] startPoint, double angleX, double angleY) {
		Double[] relativePoint = new Double[] { startPoint[0] - viewerPos[0], startPoint[1] - viewerPos[1], startPoint[2] - viewerPos[2] };
		Double[] rotation1 = new Double[] { 0.0, 0.0, 0.0 };
		Double[] rotation2 = new Double[] { 0.0, 0.0, 0.0 };

		rotation1[0] = relativePoint[0] * Math.cos(angleX) - relativePoint[1] * Math.sin(angleX);
		rotation1[1] = relativePoint[0] * Math.sin(angleX) + relativePoint[1] * Math.cos(angleX);
		rotation1[2] = relativePoint[2];

		rotation2[1] = rotation1[1] * Math.cos(angleY) - rotation1[2] * Math.sin(angleY);
		rotation2[2] = rotation1[1] * Math.sin(angleY) + rotation1[2] * Math.cos(angleY);
		rotation2[0] = rotation1[0];
		
		for(int i = 0; i < 3; i++) {
			/*
			 * Subtracting the relativePoint, to find the difference in position. The final point is also
			 * relative, but rotated, so we want to find the difference between the final relative point and
			 * the initial relative point. Then, we  have the difference the rotation causes, which we add
			 * to the start point to get the true final point.
			 */
			rotation2[i] += startPoint[i] - relativePoint[i];
		}
		
		return (new Double[] { rotation2[0], rotation2[1], rotation2[2] });
	}
	
	//TODO Replace get() expression with set() function
	public void projectAll() {
		scaleX = innerWidth / viewerWidth;
		scaleY = innerHeight / viewerHeight;
		Double[] point2d = new Double[] {};
		
		for(int a = 0; a < triangles.size(); a++) {
			for(int b = 0; b < 3; b++) {
				point2d = projectPoint(rotatePoint(triangles.get(a)[b], -viewerAngle[0], -viewerAngle[1]));
					
				triangles2d.get(a)[b] = point2d;
			}
		}

	}
	
	public void calculateTriangleMidPoints() {
		Double[] tempAverage = new Double[] {0.0, 0.0, 0.0};
		
		for(int a = 0; a < triangles.size(); a++) {
			for(int b = 0; b < 3; b++) {
				for(int c = 0; c < 3; c++) {
					tempAverage[c] += triangles.get(a)[b][c];
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
	 * Sorts the triangles list, the triangleMidPoints list, and the midPointDistances list, based on the midPointDistances
	 * list. We do not need to sort the triangles2d list, because we will re-project the triangles each time, and we do
	 * so in the order of the already ordered triangles list. We cannot calculate triangleMidPoints and midPointDistances
	 * afterwards, because we need updated information from these lists to be able to sort. Therefore, we have to calculate
	 * these lists beforehand, and then sort them along with triangles.
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
			Double[][] key2 = triangles.get(i);
			Double[] key3 = triangleMidPoints.get(i);
			Double[][] key4 = triangleUvs.get(i);
			//The index of the variable we compare our key to.
			int a = i - 1;
			
			/*
			 * Here, we move backwards, starting from the index of the double we are sorting. We stop if the index is less
			 * than 0, or if the double that we are checking is less than our key. 			 
			 */
			while (a >= 0 && midPointDistances.get(a) > key) {
				//Starting at the element to the left of double we are sorting, we move the element to the right
				midPointDistances.set(a + 1, midPointDistances.get(a));
				triangles.set(a + 1, triangles.get(a));
				triangleMidPoints.set(a + 1, triangleMidPoints.get(a));
				triangleUvs.set(a + 1, triangleUvs.get(a));
				a--;
			}
			
			/*
			 * We stopped the while loop when we found the FIRST element that our key was smaller than. Our 'a' value 
			 * represents the index of this first element, because we decreased the value of a on the previous iteration
			 * of the while loo Therefore, we want the element we are sorting to be to the
			 * right of the first smaller element, which will be a + 1. We do not have to worry about overwriting the 
			 * element at a + 1, because we already moved it to the right in the previous iteration of the while loo
			 */
			midPointDistances.set(a + 1, key);
			triangles.set(a + 1, key2);
			triangleMidPoints.set(a + 1, key3);
			triangleUvs.set(a + 1, key4);
		}

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
		
		//Cross product
		vectorFinal[0] = side1[1] * side2[2] - side1[2] * side2[1];
		vectorFinal[1] = side1[2] * side2[0] - side1[0] * side2[2];
		vectorFinal[2] = side1[0] * side2[1] - side1[1] * side2[0];
		
		return vectorFinal;
	}
	
	public Double calculateVectorAngle(Double[] vector1, Double[] vector2) {
		//arccos[(xa * xb + ya * yb + za * zb) / (dqrt(xa2 + ya2 + za2) * sqrt(xb2 + yb2 + zb2))]
		Double ax = vector1[0];
		Double ay = vector1[1];
		Double az = vector1[2];
		Double bx = vector2[0];
		Double by = vector2[1];
		Double bz = vector2[2];
		
		Double angle  = Math.acos((ax * bx + ay * by + az * bz) / (Math.sqrt(Math.pow(ax, 2) + Math.pow(ay, 2) + Math.pow(az, 2)) * Math.sqrt(Math.pow(bx, 2) + Math.pow(by, 2) + Math.pow(bz, 2))));
		
		return angle;
	}
	
	public void resetParameters() {
		/* If the aspect ratio of the window is smaller, then the width of the window must be proportionally smaller
		 * because the width is the numerator of the ratio. When the width is smaller, using the height of the display
		 * will result in the sides of the internal window being cut off. Therefore, we should use the width.
		 */
		if(screenHeight > 0) {
			useWidth = screenWidth / screenHeight < aspectRatio;
		}else {
			useWidth = false;
		}
		
		if (useWidth) {
			innerWidth = (double) screenWidth;
			innerHeight = innerWidth / aspectRatio;
		} else {
			innerHeight = (double) screenHeight;
			innerWidth = innerHeight * aspectRatio;
		}
		startX = screenWidth / 2 - innerWidth / 2;
		startY = screenHeight / 2 - innerHeight / 2;
		
	}
	
	public void setDisplayDimensions(int width, int height) {
		screenWidth = width;
		screenHeight = height;
	}
	
}
