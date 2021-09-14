import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.Timer;

public class Main implements ActionListener {
	
	UserInput in;
	Timer timer;
	Render render;
	JFrame frame;
	
	
	public Main() {
		timer = new Timer(10, this);
		render = new Render();
		in = new UserInput(render.p);
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setVisible(true);
		frame.add(render);
		frame.addKeyListener(in);
		render.p.createCube();
		timer.start();
		
	}
	
	public static void main(String[] args) {
		Main m = new Main();
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		render.p.setDisplayDimensions(frame.getContentPane().getWidth(), frame.getContentPane().getHeight());
		render.p.resetParameters();
		in.updatePosChange();
		render.p.viewerPos[1] += in.posChange[1];
		render.p.viewerPos[0] += in.posChange[0];
		render.p.viewerAngle[0] += in.angleChange[0];
		render.repaint();
		
	}
	
	
}
