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
	boolean addFile = false;
	
	public Main() {
		
		timer = new Timer(5, this);
		render = new Render(this);
		reader = new MeshReader(render.p);
		reader.readFile("cube.txt");
		for(int i = 0; i < render.p.triangleUvs.size(); i++) {
			for(int b = 0; b < 3; b++) {
				System.out.println(render.p.triangleUvs.get(i)[b][0] + ", " + render.p.triangleUvs.get(i)[b][1]);
			}
		}
		
		in = new UserInput(render.p, this);
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setTitle("3D Display");
		frame.add(render);
		frame.addKeyListener(in);
		frame.setVisible(true);
		timer.start();
		in.start();
		
	}
	
	public static void main(String[] args) {
		Main m = new Main();
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(addFile) {
			reader.readFile(fileName);
			addFile = false;
		}
		render.p.setDisplayDimensions(frame.getContentPane().getWidth(), frame.getContentPane().getHeight());
		render.p.resetParameters();
		in.updatePosChange();
		render.p.viewerPos[1] += in.posChange[1];
		render.p.viewerPos[0] += in.posChange[0];
		//System.out.println(render.p.viewerAngle[0]);
		render.p.viewerAngle[0] += in.angleChange[0];
		render.p.viewerAngle[1] += in.angleChange[1];
		render.repaint();
		
	}
	
}
