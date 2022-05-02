import java.util.ArrayList;

public class Scene {
	ArrayList<Mesh> meshes;
	ArrayList<Light> lights;
	//Viewer
	
	public Scene() {
		meshes = new ArrayList<Mesh>();
		lights = new ArrayList<Light>();
		
	}
	
	public ArrayList<Mesh> getMeshes() {
		return meshes;
		
	}
	
	public void addMesh(Mesh mesh) {
		meshes.add(mesh);
		
	}
	
	public ArrayList<Light> getLights(){
		return lights;
		
	}
	
	public void addLight(Light light) {
		lights.add(light);
	}
}
