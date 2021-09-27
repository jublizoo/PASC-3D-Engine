
public class Display {
	
	int screenWidth;
	int screenHeight;
	boolean useWidth;
	double aspectRatio = 16.0 / 9.0;
	double innerWidth;
	double innerHeight;
	double startX;
	double startY;

	public void resetParameters() {
		/* If the aspect ratio of the window is smaller, then the width of the window must be proportionally smaller
		 * because the width is the numerator of the ratio. When the width is smaller, using the height of the display
		 * will result in the sides of the internal window being cut off. Therefore, we should use the width.
		 */
		try {
			useWidth = screenWidth / screenHeight < aspectRatio;
		} catch(Exception e) {
			useWidth = false;
		}
		
		if (useWidth) {
			innerWidth = (double) screenWidth;
			innerHeight = innerWidth / aspectRatio;
		} else {
			innerHeight = (double) screenHeight;
			innerWidth = innerHeight * aspectRatio;
		}
		startX = screenWidth / 2 - innerWidth / 2;
		startY = screenHeight / 2 - innerHeight / 2;
		
	}
	
	public void setDisplayDimensions(int width, int height) {
		screenWidth = width;
		screenHeight = height;
	}
	
}
