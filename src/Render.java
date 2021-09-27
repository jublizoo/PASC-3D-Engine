import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

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
		drawPoints(g2d);
	}
	
	//TODO Check if each point is within the inner window
	public void drawPoints(Graphics2D g2d) {
		int x;
		int y;
		p.projectAll();
		p.calculateTriangleMidPoints();
		p.calculateMidPointDistances();
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
	
}
