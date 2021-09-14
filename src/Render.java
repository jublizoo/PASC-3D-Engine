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
		p.projectAll();
		for(int i = 0; i < p.points2d.size(); i++) {
			if(p.points2d.get(i)[2] > p.zNear) {
				g2d.fillOval((int) Math.round(p.points2d.get(i)[0]), (int) Math.round(p.points2d.get(i)[1]), 5, 5);
			}
		}
		
	}
	
}
