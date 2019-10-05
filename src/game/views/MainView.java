package game.views;

import engine.*;
import engine.OpenGL.*;
import game.Shaders;
import game.UserControls;
import game.entities.Player;
import game.structures.Plate;
import game.structures.Support;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11C.glDisable;

public class MainView extends EnigView {
	public static MainView main;
	
	public float totalTime;

	private VAO islandVAO;

	private boolean isBuilding = true;
	private int buildingStruct = 0;

	private ArrayList<Support> supports = new ArrayList<>();
	private ArrayList<Plate> plates = new ArrayList<>();

	private game.entities.Player player;

	public MainView(EnigWindow window) {
		super(window);
		glDisable(GL_CULL_FACE);
		islandVAO = new VAO("res/objects/island.obj");
		player = new Player(window);
	}
	
	public void reset() {

	}
	
	public boolean loop() {

		manageScene();
		
		renderScene();
		
		if (UserControls.quit(window)) {
			return true;
		}
		return false;
	}
	
	public void manageScene() {
		totalTime += deltaTime;
		player.updateRotation(window, deltaTime);
		player.updateMovement(window, deltaTime, getHeight(player.x, player.z));

		if (UserControls.build(window)) {
			isBuilding = !isBuilding;
		}

		if (window.mouseButtons[GLFW_MOUSE_BUTTON_LEFT] == 1) {
			if (isBuilding) {
				if (buildingStruct == 0) {
					addSupport();
				} else if (buildingStruct == 1) {
					addPlate();
				}
			}
		}

		if (window.keys[GLFW_KEY_0] ==1) {
			buildingStruct = 0;
		}
		if (window.keys[GLFW_KEY_1] == 1) {
			buildingStruct = 1;
		}
	}
	
	public void renderScene() {
		FBO.prepareDefaultRender();
		renderIsland();
		renderStructures();
		renderPreviews();
	}

	public void renderIsland() {
		Shaders.colorShader.enable();
		Shaders.colorShader.setUniform(0, 0, player.getCameraMatrix());
		Shaders.colorShader.setUniform(2, 0, new Vector3f(0.5f, 0.5f, 0.5f));
		islandVAO.fullRender();
	}

	public void renderStructures() {
		renderSupports();
		Plate.renderSet(plates, player);
	}

	public void renderSupports() {
		Shaders.colorShader.setUniform(2, 0, new Vector3f(0.7f, 0.7f, 0.75f));
		for (int i = 0; i < supports.size(); ++i) {
			supports.get(i).render(player.getCameraMatrix());
		}
	}

	public void renderPreviews() {
		if (isBuilding) {
			if (buildingStruct == 0) {
				Support.renderPreview(player);
			}
			if (buildingStruct == 1) {
				Plate.renderPreview(player);
			}
		}
	}

	public void addSupport() {
		Support s = new Support(player);
		if (!barExists(s.fromX, s.fromZ, s.toX, s.toZ)) {
			supports.add(s);
		}
	}

	public void addPlate() {
		Plate plate = new Plate(player);
		if (!plateExists(plate.posX, plate.posZ)) {
			plates.add(plate);
		}
	}

	public boolean plateExists(int x, int z) {
		for (int i = 0; i < plates.size(); ++i) {
			Plate plate = plates.get(i);
			if (plate.posX == x && plate.posZ == z) {
				return true;
			}
		}
		return false;
	}

	public boolean barExists(int fromX, int fromZ, int toX, int toZ) {
		for (int i = 0; i < supports.size(); ++i) {
			Support s = supports.get(i);
			if (fromX == s.fromX && fromZ == s.fromZ && toX == s.toX && toZ == s.toZ) {
				return true;
			}
			if (fromX == s.toX && fromZ == s.toZ && toX == s.fromX && toZ == s.fromZ) {
				return true;
			}
		}
		return false;
	}

	public float getHeight(float x, float z) {
		float supportRadius = 0.2f;
		float supportDiameter = 0.4f;

		float ret = -100;
		float relX = (x + supportRadius) % 3f;
		float relZ = (z + supportRadius) % 3f;
		if (relX < 0) {
			relX += 3f;
		}
		if (relZ < 0) {
			relZ += 3f;
		}
		if (relX < supportDiameter) {
			if (barExists((int) Math.round(x / 3), (int) Math.floor(z / 3), (int) Math.round(x / 3), (int) Math.ceil(z / 3))) {
				ret = (float) Math.sqrt(1f - ((relX - supportRadius) * (relX - supportRadius)) * 5f) * supportRadius - supportRadius;
			}
		}
		if (relZ < supportDiameter) {
			if (barExists((int) Math.floor(x / 3), (int) Math.round(z / 3), (int) Math.ceil(x / 3), (int) Math.round(z / 3))) {
				float tret = (float) Math.sqrt(1f - ((relZ - supportRadius) * (relZ - supportRadius)) * 5f) * supportRadius - supportRadius;
				if (tret > ret) {
					ret = tret;
				}
			}
		}

		if (x * x + z * z < 1) {
			ret = 0;
		}
		
		if (plateExists((int) Math.floor((player.x) / 3f), (int) Math.floor((player.z) / 3f))) {
			if (ret < -0.2f) {
				ret = -0.2f;
			}
		}

		return ret;
	}
}
