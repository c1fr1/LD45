package game.structures;

import engine.OpenGL.Shader;
import engine.OpenGL.Texture;
import engine.OpenGL.VAO;
import game.Shaders;
import game.entities.Player;
import game.views.MainView;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Plate {
	public int posX;
	public int posZ;
	private static VAO plateVAO;
	public static Texture plateTex;

	public Plate(Player player) {
		Vector2f position = player.getRotated2DVector(0.0f, -1f, 1.5f).add(player.x, player.z);
		posX = (int) Math.floor((position.x) / 3f);
		posZ = (int) Math.floor((position.y) / 3f);
	}
	public static void renderSet(ArrayList<Plate> set, Player player) {
		Shaders.textureShader.enable();
		plateVAO.prepareRender();
		plateTex.bind();
		for (int i = 0; i < set.size(); ++i) {
			float x = set.get(i).posX;
			float z = set.get(i).posZ;
			Shaders.textureShader.setUniform(0, 0, player.getCameraMatrix().translate(1.5f + x * 3f, 0.0f, 1.5f + z * 3f).scale(0.5f, 1f, 0.5f));
			plateVAO.drawTriangles();
		}
		plateVAO.unbind();
	}

	public static void loadRes() {
		plateVAO = new VAO("res/objects/plate.obj");
		plateTex = new Texture("res/textures/plateTXT.png");
	}

	public static void renderPreview(Player player) {
		Vector2f position = player.getRotated2DVector(0.0f, -1f, 1.5f).add(player.x, player.z);
		int x = (int) Math.floor((position.x) / 3f);
		int z = (int) Math.floor((position.y) / 3f);

		if (MainView.main.plateCanBeSupported(x, z)) {
			Shaders.colorShader.setUniform(2, 0, new Vector3f(0f, 1f, 1f));
 		} else {
			Shaders.colorShader.setUniform(2, 0, new Vector3f(1f, 0f, 0f));
		}

		Shaders.colorShader.setUniform(0, 0, player.getCameraMatrix().translate(1.5f + x * 3f, 0.0f, 1.5f + z * 3f).scale(0.5f, 1.01f, 0.5f));
		plateVAO.fullRender();
	}
}
