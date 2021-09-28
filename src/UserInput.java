import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Scanner;

public class UserInput extends Thread implements KeyListener{
	//These values are passed in the constructor, and reference the object created in Main. We need these to change variables
	Projection p;
	MeshReader reader;
	Scanner scan;
	String textInput;
	//These variables determine how much the position and angle of the viewer change each tick.
	Double[] posChange = new Double[] {0.0, 0.0, 0.0};
	Double[] angleChange = new Double[] {0.0, 0.0};
	//movementSpeed determines how quickly the player moves, lookingSpeed determines how quickly the angle of the viewer changes.
	final double movementSpeed = 0.08;
	final double lookingSpeed = 0.02;
	//If each of the corresponding keys are pressed.
	boolean forward;
	boolean backward;
	boolean left;
	boolean right;
	//Direction the player is moving
	String currentDirection = "";
	
	public UserInput(Projection p, MeshReader reader) {
		this.p = p;
		this.reader = reader;
		scan = new Scanner(System.in);
		
	}
	
	public void run() {
		while(true) {
			textInput = scan.nextLine();
			reader.readFile(textInput);
			for(int a = 0; a < p.getTriangles3d().size(); a++) {
				for(int b = 0; b < 3; b++) {
					for(int c = 0; c < 3; c++) {
						System.out.println(p.getTriangles3d().get(a)[b][c]);
					}
				}
			}
			System.out.println(p.objIndexes.get(p.objIndexes.size() - 1)[0] + ", " + p.objIndexes.get(p.objIndexes.size() - 1)[1]);
		}
		
	}

	//If statements are to prevent each section of code from being re-activated before the key has been released.
	@Override
	public void keyPressed(KeyEvent e) {
		char key = e.getKeyChar();
		if(key == 'w') {
			if(!forward) {
				forward = true;
				currentDirection = "forward";
			}
		}else if(key == 's') {
			if(!backward) {
				backward = true;
				currentDirection = "backward";
			}
		}else if(key == 'd') {
			if(!right) {
				right = true;
				currentDirection = "right";
			}	
		}else if(key == 'a') {
			if(!left) {
				left = true;
				currentDirection = "left";
			}
		}else if(e.getKeyCode() == KeyEvent.VK_UP)	{
			angleChange[1] = -lookingSpeed;
		}else if(e.getKeyCode() == KeyEvent.VK_DOWN)	{
			angleChange[1] = lookingSpeed;
		}else if(e.getKeyCode() == KeyEvent.VK_RIGHT)	{
			angleChange[0] = -lookingSpeed;
		}else if(e.getKeyCode() == KeyEvent.VK_LEFT)	{
			angleChange[0] = lookingSpeed;
		}
		
	}

	/*
	 * If a direction has been "overridden" (for example, if you pressed forward and then right, without releasing
	 * forward), we do not want to stop the movement, because the key being released was no longer affecting the movement.
	 * Therefore, releasing the key should also not affect the movement. If it did, gameplay would be clunky and
	 * difficult. The nested "if" statements prevent this.
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		char key = e.getKeyChar();
		if(key == 'w') {
			forward = false;
			if(currentDirection.equals("forward")) {
				currentDirection = "none";
			}
		}else if(key == 's') {
			backward = false;
			if(currentDirection.equals("backward")) {
				currentDirection = "none";
			}
		}else if(key == 'd') {
			right = false;
			if(currentDirection.equals("right")) {
				currentDirection = "none";
			}
		}else if(key == 'a') {
			left = false;
			if(currentDirection.equals("left")) {
				currentDirection = "none";
			}		
		}else if(e.getKeyCode() == KeyEvent.VK_UP)		{
			angleChange[1] = 0.0;
		}else if(e.getKeyCode() == KeyEvent.VK_DOWN)	{
			angleChange[1] = 0.0;
		}else if(e.getKeyCode() == KeyEvent.VK_RIGHT)	{
			angleChange[0] = 0.0;
		}else if(e.getKeyCode() == KeyEvent.VK_LEFT)	{
			angleChange[0] = 0.0;
		}
		
	}
	
	/*
	 * The traditional angle model causes positive angles to result in positive sin values, which in our case should
	 * be negative. We rotated the angle model, which causes x-values to the left of 0 degrees to be positive, which
	 * is the inverse of the model we use.
	 */
	public void updatePosChange(){
		if(currentDirection.equals("forward")) {
			posChange[0] = -movementSpeed * Math.sin(p.viewerAngle[0]);
			posChange[1] = movementSpeed * Math.cos(p.viewerAngle[0]);
		}else if(currentDirection.equals("backward")) {
			posChange[0] = -movementSpeed * Math.sin(p.viewerAngle[0] + Math.PI);
			posChange[1] = movementSpeed * Math.cos(p.viewerAngle[0] + Math.PI);
		}else if(currentDirection.equals("right")) {
			posChange[0] = -movementSpeed * Math.sin(p.viewerAngle[0] - Math.PI / 2);
			posChange[1] = movementSpeed * Math.cos(p.viewerAngle[0] - Math.PI / 2);

		}else if(currentDirection.equals("left")) {
			posChange[0] = -movementSpeed * Math.sin(p.viewerAngle[0] + Math.PI / 2);
			posChange[1] = movementSpeed * Math.cos(p.viewerAngle[0] + Math.PI / 2);

		}else if(currentDirection.equals("none")){
			posChange[0] = 0.0;
			posChange[1] = 0.0;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
