public class Light {
	
	private double brightness;
	private Double[] position;
	private Double[] angle;
	
	public Light(double brightness, double x, double y, double z, double angleX, double angleY, double angleZ) {
		position = new Double[] {x, y, z};
		angle = new Double[] {angleX, angleY, angleZ};
		this.brightness = brightness;
			
	}
	
	public double getBrightness() {
		return brightness;
		
	}
	
	public void setBrightness(double brightness) {
		this.brightness = brightness;
	}
	
	public Double[] getPosition() {
		return position;
				
	}
	
	public void setPosition(double x, double y, double z) {
		position[0] = x;
		position[1] = y;
		position[2] = z;
		
	}
	
	public void move(double x, double y, double z) {
		position[0] += x;
		position[1] += y;
		position[2] += z;
		
	}
	
	public Double[] getAngle() {
		return angle;
		
	}
	
	public void setAngle(double angleX, double angleY, double angleZ) {
		angle[0] = angleX;
		angle[1] = angleY;
		angle[2] = angleZ;
		
	}
	
	public void rotate(double angleX, double angleY, double angleZ) {
		angle[0] += angleX;
		angle[1] += angleY;
		angle[2] += angleZ;
		
	}
	
	
}
