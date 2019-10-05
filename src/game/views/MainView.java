package game.views;

import engine.*;
import engine.OpenGL.*;
import game.Shaders;
import game.UserControls;
import game.entities.Player;
import game.structures.Support;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.opengl.GL11C.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11C.glDisable;

public class MainView extends EnigView {
	public static MainView main;
	
	public float totalTime;

	private VAO islandVAO;

	private boolean isBuilding = true;
	private int buildingStruct = 0;

	private ArrayList<Support> supports = new ArrayList<>();

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
		player.updateMovement(window, deltaTime);

		if (window.mouseButtons[GLFW_MOUSE_BUTTON_LEFT] == 1) {
			addSupport();
		}
	}
	
	public void renderScene() {
		FBO.prepareDefaultRender();
		renderIsland();
		Support.renderPreview(player);
		renderSupports();
	}

	public void renderIsland() {
		Shaders.colorShader.enable();
		Shaders.colorShader.setUniform(0, 0, player.getCameraMatrix());
		Shaders.colorShader.setUniform(2, 0, new Vector3f(0.5f, 0.5f, 0.5f));
		islandVAO.fullRender();
	}

	public void renderSupports() {
		Shaders.colorShader.setUniform(2, 0, new Vector3f(0.7f, 0.7f, 0.75f));
		for (int i = 0; i < supports.size(); ++i) {
			supports.get(i).render(player.getCameraMatrix());
		}
	}

	public void addSupport() {
		Support s = new Support(player);
		if (!barExists(s.fromX, s.fromZ, s.toX, s.toZ)) {
			supports.add(s);
		}
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
		float ret = -100;
		float relX = x + 0.2f % 1f;
		float relZ = z + 0.2f % 1f;
		if (relX < 0) {
			relX += 1f;
		}
		if (relZ < 0) {
			relZ += 1f;
		}
		if (relX < 0.4f) {
			if (ret > 0.1)
		}
		if (relZ < 0.4f) {

		}
	}
}
