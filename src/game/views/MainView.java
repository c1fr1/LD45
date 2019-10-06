package game.views;

import engine.*;
import engine.OpenGL.*;
import game.Shaders;
import game.Trash;
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
	private VAO scrapBar;

	private boolean isBuilding = false;
	private int buildingStruct = 0;
	private float timeTillNextTrash = 0;
	private float buildingSize = 5;

	private Texture islandTex;

	private ArrayList<Support> supports = new ArrayList<>();
	private ArrayList<Plate> plates = new ArrayList<>();

	private ArrayList<Trash> trash = new ArrayList<>();

	private game.entities.Player player;

	public MainView(EnigWindow window) {
		super(window);
		glDisable(GL_CULL_FACE);
		islandVAO = new VAO("res/objects/island.obj");
		scrapBar = new VAO(0, 0, 100, 10);

		islandTex = new Texture("res/textures/island.png");
		player = new Player(window);
	}
	
	public void reset() {
		player.x = 0;
		player.y = 0;
		player.z = 0;
		supports.clear();
		plates.clear();
		trash.clear();
		isBuilding = false;
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

		manageBuilding();
		manageTrash();
	}

	public void manageBuilding() {

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
			isBuilding = true;
		}
		if (window.keys[GLFW_KEY_1] == 1) {
			buildingStruct = 1;
			isBuilding = true;
		}
		if (player.y < -50) {
			reset();
		}
	}

	public void manageTrash() {
		if (timeTillNextTrash < 0) {
			timeTillNextTrash = 0.5f + (float) Math.random() * 2f;
			trash.add(new Trash(new Vector3f(), buildingSize + 2));
		} else {
			timeTillNextTrash -= deltaTime;
		}

		for (int i = 0; i < trash.size(); ++i) {
			Trash t = trash.get(i);
			t.updatePosition(deltaTime);
			if (t.y < -30) {
				trash.remove(i);
				--i;
			} else if (t.distanceSquared(player) < 1) {
				player.scrap += Math.random() * 2 + 1;
				trash.remove(i);
				--i;
			}
		}
	}
	
	public void renderScene() {
		FBO.prepareDefaultRender();
		renderIsland();
		renderPreviews();
		renderStructures();
		Trash.renderSet(player, trash);
	}

	public void renderIsland() {
		Shaders.textureShader.enable();
		Shaders.textureShader.setUniform(0, 0, player.getCameraMatrix());
		islandTex.bind();
		islandVAO.fullRender();
		Shaders.colorShader.enable();
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
		if (player.scrap > 1) {
			Support s = new Support(player);
			if (!barExists(s.fromX, s.fromZ, s.toX, s.toZ)) {
				if (supportCanBeSupported(s.fromX, s.fromZ, s.toX, s.toZ)) {
					supports.add(s);
					--player.scrap;
				}
			}
		}
	}

	public void addPlate() {
		if (player.scrap > 3) {
			Plate plate = new Plate(player);
			if (!plateExists(plate.posX, plate.posZ)) {
				if (plateCanBeSupported(plate.posX, plate.posZ)) {
					plates.add(plate);
					player.scrap -= 3;
				}
			}
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

	public boolean plateCanBeSupported(int x, int z) {
		int numNextTo = 0;
		for (int i = 0; i < supports.size(); ++i) {
			if (numNextTo == 2) {
				return true;
			}
			Support s = supports.get(i);
			if (s.fromX == x && s.fromZ == z && s.toX == x + 1 && s.toZ == z) {
				++numNextTo;
				continue;
			}
			if (s.toX == x && s.toZ == z && s.fromX == x + 1 && s.fromZ == z) {
				++numNextTo;
				continue;
			}
			if (s.fromX == x && s.fromZ == z && s.toX == x && s.toZ == z + 1) {
				++numNextTo;
				continue;
			}
			if (s.toX == x && s.toZ == z && s.fromX == x && s.fromZ == z + 1) {
				++numNextTo;
				continue;
			}
			if (s.fromX == x + 1 && s.fromZ == z && s.toX == x + 1 && s.toZ == z + 1) {
				++numNextTo;
				continue;
			}
			if (s.toX == x + 1 && s.toZ == z && s.fromX == x + 1 && s.fromZ == z + 1) {
				++numNextTo;
				continue;
			}
			if (s.fromX == x && s.fromZ == z + 1 && s.toX == x + 1 && s.toZ == z + 1) {
				++numNextTo;
				continue;
			}
			if (s.toX == x && s.toZ == z + 1 && s.fromX == x + 1 && s.fromZ == z + 1) {
				++numNextTo;
				continue;
			}
		}
		if (numNextTo >= 2) {
			return true;
		} else {
			return false;
		}
	}

	public boolean supportCanBeSupported(int x0, int z0, int x1, int z1) {
		if (x0 == 0 && z0 == 0) {
			return true;
		}
		if (x1 == 0 && z1 == 0) {
			return true;
		}
		for (int i = 0; i < supports.size(); ++i) {
			Support s = supports.get(i);
			if (s.fromX == x0 && s.fromZ == z0) {
				return true;
			}
			if (s.fromX == x1  && s.fromZ == z1) {
				return true;
			}
			if (s.toX == x0 && s.toZ == z0) {
				return true;
			}
			if (s.toX == x1  && s.toZ == z1) {
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

		if (plateExists((int) Math.floor((x) / 3f), (int) Math.floor((z) / 3f))) {
			if (ret < -0.2f) {
				ret = -0.2f;
			}
		}

		return ret;
	}
}
