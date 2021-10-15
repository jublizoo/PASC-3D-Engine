import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import javax.swing.JPanel;

public class Render extends JPanel {
	
	Projection p;

	public Render() {
		p = new Projection();

	}

	protected void paintComponent(Graphics g) {
		//p.setDisplayDimensions();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, p.screenWidth, p.screenHeight);
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fillRect((int) p.startX, (int) p.startY, (int) p.innerWidth, (int) p.innerHeight);
		g2d.setColor(Color.BLACK);
		drawTriangles(g2d);
		//drawPoints(g2d);
		
	}
	
	//TODO Check if each point is within the inner window
	public void drawPoints(Graphics2D g2d) {
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
		 * calculation, the angle would change. This should not happen, because rotating the camera should
		 * not change what faces are displayed.
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
			triangleVector = p.calculateVector(p.getTriangles3d().get(a));
			angle = p.calculateVectorAngle(triangleVector, viewerVector);
			angle2 = p.calculateVectorAngle(viewerToTriangleVector, triangleVector);
			
			if(p.midPointDistances.get(a) > 0 && Math.abs(angle2) > Math.PI / 2) {
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
