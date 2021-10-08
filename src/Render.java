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
		p.calculateTriangleMidPoints();
		p.calculateMidPointDistances();
		p.sortLists();
		p.projectAll();
		
		for(int a = 0; a < p.triangles2d.size(); a++) {
			Double[] viewerVector = {-Math.sin(p.viewerAngle[0]), Math.cos(p.viewerAngle[0]), 0.0};
			Double[] triangleVector = p.calculateVector(p.getTriangles3d().get(a));
			
			if(p.midPointDistances.get(a) > 0 && Math.abs(p.calculateVectorAngle(triangleVector, viewerVector)) > Math.PI / 2) {
				//random = (int) Math.round(255.0 / (p.midPointDistances.get(a) + 1));
				//random = (int) Math.round(255 * Math.random());
				//random = (int) Math.round(127 + 127 * Math.sin(p.midPointDistances.get(a) * 10));
				//c = new Color(random, random, random);
				
				try {
					random = 3 * (int) Math.round(255.0 / (p.midPointDistances.get(a) + 1));
					c = new Color(random, random, random);
				} catch (Exception e) {
					c = new Color(255, 255, 255);
				}
				
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

}
