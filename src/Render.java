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
		g2d.setColor(Color.LIGHT_GRAY);
		//g2d.fillRect((int) p.startX, (int) p.startY, (int) p.innerWidth, (int) p.innerHeight);
		g2d.setColor(Color.BLACK);
		g2d.fillOval((int)(-p.viewerPos[0] + p.startX + p.innerWidth / 2), (int)(-p.viewerPos[1] + p.startY + p.innerHeight / 2), 5, 5);
	}
	

}
