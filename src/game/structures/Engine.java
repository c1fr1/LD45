package game.structures;

import engine.OpenGL.VAO;
import game.Shaders;
import game.Trash;
import game.entities.Player;
import game.views.MainView;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Engine {
	public int posX;
	public int posY;
	public int dir;
	private static VAO frame;
	private static VAO blades;

	public Engine(Player player) {
		Vector2f position = player.getRotated2DVector(0.0f, -1f, 1.5f).add(player.x, player.z);
		posX = (int) Math.floor((position.x) / 3f);
		posY = (int) Math.floor((position.y) / 3f);
		dir = player.getDirection();
	}

	public static void loadRes() {
		blades = new VAO("res/objects/eblades.obj");
		frame = new VAO("res/objects/eframe.obj");
	}
	public static void renderPreview(Player player) {
		Vector2f position = player.getRotated2DVector(0.0f, -1f, 1.5f).add(player.x, player.z);
		int x = (int) Math.floor((position.x) / 3f);
		int z = (int) Math.floor((position.y) / 3f);

		Vector3f color = new Vector3f();
		if (!MainView.main.plateCanBeSupported(x, z)) {
			Shaders.colorShader.setUniform(2, 0, new Vector3f(1f, 0f, 0f));
			color.x = 1;
		} else {
			if (player.scrap >= 3) {
				color.y = 1;
				color.z = 1;
			} else {
				color.y = 1;
				color.z = 1;
			}
		}

		Shaders.colorShader.setUniform(2, 0, color);

		float playerDir = player.getDirection() + 2;
		Shaders.colorShader.setUniform(0, 0, player.getCameraMatrix().translate(1.5f + x * 3f, 0.3f, 1.5f + z * 3f).scale(0.5f, 0.5f, 0.5f).rotateY(-playerDir * (float) Math.PI / 2f));

		frame.fullRender();
		color.x *= 0.5f;
		color.y *= 0.5f;
		color.z *= 0.5f;
		Shaders.colorShader.setUniform(2, 0, color);
		blades.fullRender();
	}

	public static void renderSet(ArrayList<Engine> engines, Player player) {
		frame.prepareRender();
		for (Engine e:engines) {
			float rdir = e.dir + 2;
			Shaders.colorShader.setUniform(0, 0, player.getCameraMatrix().translate(1.5f + e.posX * 3f, 0.3f, 1.5f + e.posY * 3f).scale(0.5f, 0.5f, 0.5f).rotateY(-rdir * (float) Math.PI / 2f));
			frame.drawTriangles();
		}
		frame.unbind();
		blades.prepareRender();
		Shaders.colorShader.setUniform(2, 0, new Vector3f(0.2f, 0.2f, 0.2f));
		for (Engine e:engines) {
			float rdir = e.dir + 2;
			Shaders.colorShader.setUniform(0, 0, player.getCameraMatrix().translate(1.5f + e.posX * 3f, 0.3f, 1.5f + e.posY * 3f).scale(0.5f, 0.5f, 0.5f).rotateY(-rdir * (float) Math.PI / 2f).rotateZ(10 * MainView.main.totalTime));
			frame.drawTriangles();
		}
	}
}
