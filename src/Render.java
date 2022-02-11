import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Render extends JPanel {
	
	Projection p;
	Main m;
	
	BufferedImage image;

	public Render(Main m) {
		p = new Projection();
		this.m = m;
		loadImages();
	}

	protected void paintComponent(Graphics g) {
		//p.setDisplayDimensions();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, p.screenWidth, p.screenHeight);
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fillRect((int) p.startX, (int) p.startY, (int) p.innerWidth, (int) p.innerHeight);
		g2d.setColor(Color.BLACK);
		//drawTriangles(g2d);
		textureTriangles(g2d);
		//drawPoints(g2d);
	}
	
	public void loadImages() {
		try {
			image = ImageIO.read(new File("tex.png"));
		} catch (IOException e) {}
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
	
	//Tex coords are not sorted
	public void textureTriangles(Graphics2D g2d) {
		//The surface normal of a given triangle
		Double[] triangleVector;
		Double[] viewerVector;
		Double[] viewerToTriangleVector;
		Double angle;
		Double angle2;
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
			
			if(p.midPointDistances.get(a) > 0 && Math.abs(angle2) > Math.PI / 2) {
				traverse(p.triangles2d.get(a), g2d, a, angle);
			}
		}
	}
	
	public void traverse(Double[][] originalTriangle, Graphics2D g2d, int triangleNum, double angle) {	
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
		Double[][] triangle = originalTriangle.clone();
		
		//The starting and ending x values for each triangle "slice" (startX was already taken)
		int x1;
		int x2;
		
		for(int i = 0; i < 3; i++) {
			triangleY[i] = p.rotatePoint(p.triangles3d.get(triangleNum)[i], -p.viewerAngle[0], -p.viewerAngle[1])[1] - p.viewerPos[1];
		}	 

		//Setting p1 to the largest y-value point
		for(int i = 1; i < 3; i++) {
			if(triangle[i][1] > triangle[0][1]) {
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
					drawPoint(g2d, i, b, originalTriangle, triangleY, texCoords, angle);
				}
			} else if(x1 < x2) {
				for(int b = x1; b <= x2; b++) {
					drawPoint(g2d, i, b, originalTriangle, triangleY, texCoords, angle);
				}
			}
		}
		
		//Lower part of triangle, going from line 3 - 2
		for(int i = p2[1]; i > p3[1]; i--) {
			x1 = (int) Math.round(p2[0] + (i - p2[1]) / m3);
			x2 = (int) Math.round(p1[0] + (i - p1[1]) / m2);
			
			if(x1 > x2) {
				for(int b = x1; b >= x2; b--) {
					drawPoint(g2d, i, b, originalTriangle, triangleY, texCoords, angle);
				}
			} else if(x1 < x2) {
				for(int b = x1; b <= x2; b++) {
					drawPoint(g2d, i, b, originalTriangle, triangleY, texCoords, angle);
				}
			}
		}
		
	}
	
	public void drawPoint(Graphics2D g2d, int i, int b, Double[][] originalTriangle, Double[] triangleY, Double[][] texCoords, double angle) {
		int lightLevel;
		Double[] point;
		Double[] uv;
		point = new Double[] {(double) b, (double) i};
	
		uv = m.tex.interpolateCoords(triangleY, m.tex.calculateBaryCoords(originalTriangle, point), texCoords);
		uv[0] *= image.getWidth();
		uv[1] *= image.getHeight();
		
		if(uv[0] >= image.getWidth()) {
			uv[0] = (double) (image.getWidth() - 1);
		}else if(uv[0] <= 0) {
			uv[0] = 1.0;
		}
		if(uv[1] >= image.getHeight()) {
			uv[1] = (double) image.getHeight() - 1;
		}else if(uv[1] <= 0) {
			uv[1] = 1.0;
		}
		
		lightLevel = calculateLight(angle);
		g2d.setColor(new Color(image.getRGB((int)(double) (uv[0]), (int)(double) (uv[1]))));
		g2d.fillRect(b, i, 1, 1);
		
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

			
			if(p.midPointDistances.get(a) > 0 && Math.abs(angle2) < Math.PI / 2) {
				lightLevel = calculateLight(angle);
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
	
	public int calculateLight(Double angle) {
		int lightLevel = (int) Math.round(255 * ((angle / Math.PI)));
		return lightLevel;
		
	}

}
