import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.Timer;

public class Main implements ActionListener {
	
	UserInput in;
	Timer timer;
	Render render;
	JFrame frame;
	MeshReader reader;
	String fileName;
	
	boolean cont = true;
	
	
	public Main() {
		timer = new Timer(10, this);
		render = new Render();
		reader = new MeshReader(render.p);
		in = new UserInput(render.p, reader);
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setVisible(true);
		frame.add(render);
		frame.addKeyListener(in);
		timer.start();
		in.start();
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
		render.p.viewerAngle[1] += in.angleChange[1];
		render.repaint();
		

	}
	
}
