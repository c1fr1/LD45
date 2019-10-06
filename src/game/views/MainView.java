package game.views;

import engine.*;
import engine.OpenGL.*;
import game.Main;
import game.Shaders;
import game.Trash;
import game.UserControls;
import game.entities.Player;
import game.structures.Engine;
import game.structures.Plate;
import game.structures.Support;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;

public class MainView extends EnigView {
	public static MainView main;
	
	public float totalTime;

	private VAO islandVAO;
	private VAO scrapBar;
	private VAO rislandVAO;
	private VAO rislandBoiVAO;

	private boolean isBuilding = false;
	private int buildingStruct = 0;
	private float timeTillNextTrash = 0;
	private float buildingSize = 5;

	private Texture islandTex;
	private Texture scrapTex;
	private Texture rislandTex;

	private Texture lossTexture;

	private ArrayList<Support> supports = new ArrayList<>();
	private ArrayList<Plate> plates = new ArrayList<>();
	private ArrayList<Engine> engines = new ArrayList<>();

	private ArrayList<Trash> trash = new ArrayList<>();

	private game.entities.Player player;

	public Vector3f targetRaftVelocity = new Vector3f();
	public Vector3f raftVelocity = new Vector3f();
	public Vector3f raftPosition = new Vector3f();

	boolean haslost = true;

	public MainView(EnigWindow window) {
		super(window);
		glDisable(GL_CULL_FACE);
		islandVAO = new VAO("res/objects/island.obj");
		rislandVAO = new VAO("res/objects/risland.obj");
		rislandBoiVAO = new VAO("res/objects/rislandBoi.obj");
		scrapBar = new VAO(0, 0, 50, 5);

		islandTex = new Texture("res/textures/island.png");
		scrapTex = new Texture("res/textures/scrap.png");
		rislandTex = new Texture("res/textures/rislandTex.png");
		lossTexture = new Texture("res/textures/youwin.png");
		player = new Player(window);

		glClearColor(0.529f, 0.808f, 0.922f, 1.0f);
	}
	
	public void reset() {
		player.x = 0;
		player.y = 0;
		player.z = 0;
		supports.clear();
		plates.clear();
		trash.clear();
		engines.clear();
		raftPosition = new Vector3f();
		raftVelocity = new Vector3f();
		targetRaftVelocity = new Vector3f();
		player.scrap = 0;
		isBuilding = false;
	}
	
	public boolean loop() {
		if (haslost) {
			manageScene();
			renderScene();
		} else {
			Shaders.textureShader.enable();
			Shaders.textureShader.setUniform(0, 0, new Matrix4f());
			lossTexture.bind();
			Main.screenObj.fullRender();
		}
		
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

		raftPosition.add(raftVelocity.mul(deltaTime, new Vector3f()));
		raftVelocity.add(new Vector3f(targetRaftVelocity).sub(raftVelocity).mul(0.1f));
		if (player.x + raftPosition.x >= 45) {
			haslost = false;
		}
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
				} else if (buildingStruct == 2) {
					addEngine();
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
		if (window.keys[GLFW_KEY_2] == 1) {
			buildingStruct = 2;
			isBuilding = true;
		}
		if (player.y < -50) {
			reset();
		}
	}

	public void manageTrash() {
		if (timeTillNextTrash < 0) {
			timeTillNextTrash = (0.5f + (float) Math.random() * 5f) / buildingSize;
			trash.add(new Trash(new Vector3f(), buildingSize));
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
				player.scrap += Math.random() * 2.0 + 1;
				trash.remove(i);
				--i;
			}
		}
	}
	//AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
	public void renderScene() {
		FBO.prepareDefaultRender();
		renderIsland();
		renderPreviews();
		renderStructures();
		Trash.renderSet(player, trash);
		renderScrapBar();
		renderRisland();
	}

	public void renderRisland() {
		Shaders.textureShader.enable();
		Shaders.textureShader.setUniform(0, 0, player.getCameraMatrix().translate(50 - raftPosition.x, 0f, 0f - raftPosition.z).scale(20f));
		rislandTex.bind();
		rislandVAO.fullRender();
		Shaders.colorShader.enable();
		Shaders.colorShader.setUniform(0, 0, player.getCameraMatrix().translate(50f - raftPosition.x, 0f, 0f - raftPosition.z).scale(20f));
		Shaders.colorShader.setUniform(2, 0, new Vector3f(1f, 1f, 1f));
		rislandBoiVAO.fullRender();
	}

	public void renderScrapBar() {
		Shaders.scrapShader.enable();
		Shaders.scrapShader.setUniform(0, 0, new Matrix4f(Main.squareCam).translate(-50 * window.getAspectRatio(), 45, 0));
		Shaders.scrapShader.setUniform(2, 0, (float) player.scrap / 20f);

		if (isBuilding) {
			if (buildingStruct == 0) {
				Shaders.scrapShader.setUniform(2, 1, (float) (player.scrap - 1) / 20f);
			} else if (buildingStruct == 1) {
				Shaders.scrapShader.setUniform(2, 1, (float) (player.scrap - 3) / 20f);
			} else if (buildingStruct == 2) {
				Shaders.scrapShader.setUniform(2, 1, (float) (player.scrap - 5) / 20f);
			}
		} else {
			Shaders.scrapShader.setUniform(2, 1, (float) player.scrap / 20f);
		}

		scrapTex.bind();
		scrapBar.fullRender();
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
		Engine.renderSet(engines, player);
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
			} else if (buildingStruct == 1) {
				Plate.renderPreview(player);
			} else if (buildingStruct == 2) {
				Engine.renderPreview(player);
			}
		}
	}

	public void addSupport() {
		if (player.scrap >= 1) {
			Support s = new Support(player);
			if (!barExists(s.fromX, s.fromZ, s.toX, s.toZ)) {
				if (supportCanBeSupported(s.fromX, s.fromZ, s.toX, s.toZ)) {
					supports.add(s);
					--player.scrap;
					buildingSize += 0.5f;
				}
			}
		}
	}

	public void addPlate() {
		if (player.scrap >= 3) {
			Plate plate = new Plate(player);
			if (!plateExists(plate.posX, plate.posZ)) {
				if (plateCanBeSupported(plate.posX, plate.posZ)) {
					plates.add(plate);
					player.scrap -= 3;
					buildingSize += 0.5f;
				}
			}
		}
	}

	public void addEngine() {
		if (player.scrap >= 5) {
			Engine engine = new Engine(player);
			if (plateExists(engine.posX, engine.posY)) {
				engines.add(engine);
				player.scrap -= 5;
				if (engine.dir == 0) {
					targetRaftVelocity.z -= 1f;
				} else if (engine.dir == 1) {
					targetRaftVelocity.x += 1f;
				} else if (engine.dir == 2) {
					targetRaftVelocity.z += 1f;
				} else {
					targetRaftVelocity.x -= 1f;
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

	public void impact(float x, float z) {
		int xpos = (int)Math.floor(x / 3);
		int zpos = (int)Math.floor(z / 3);
		boolean plateRemoved = false;
		for (int i = 0; i < plates.size(); ++i) {
			if (plates.get(i).posX == xpos && plates.get(i).posZ == zpos) {
				plates.get(i).hp -= 1;
				if (plates.get(i).hp == 0) {
					plates.remove(i);
					plateRemoved = true;
					break;
				}
			}
		}
		if(plateRemoved) {
			for (int i = 0; i < engines.size(); ++i) {
				if (engines.get(i).posX == xpos && engines.get(i).posY == zpos) {
					int dir = engines.get(i).dir;
					if (dir == 0) {
						targetRaftVelocity.z += 1;
					} else if (dir == 1) {
						targetRaftVelocity.x -= 1;
					} else if (dir == 2) {
						targetRaftVelocity.z -= 1;
					} else if (dir == 3) {
						targetRaftVelocity.x += 1;
					}
					engines.remove(i);
					return;
				}
			}
		}
	}
}
