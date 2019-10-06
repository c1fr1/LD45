package game;

import engine.OpenGL.Shader;
import engine.OpenGL.Texture;
import engine.OpenGL.VAO;
import game.entities.Player;
import game.structures.Plate;
import game.views.MainView;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;

public class Trash extends Vector3f {
	private Vector3f velocity;

	private static VAO trashVAO;
	private static Texture noiseTexture;


	public Trash(Vector3f target, float radius) {
		Vector3f targetPosition = new Vector3f((float) Math.random() * radius * 2 - 1, (float)Math.random() * radius * 2 - 1, (float)Math.random() * radius * 2 - 1);
		x = 51;
		y = 42;
		z = 11.828f;
		while (targetPosition.lengthSquared() > radius * radius) {
			targetPosition = new Vector3f((float) Math.random() * radius * 2 - 1, (float)Math.random() * radius * 2 - 1, (float)Math.random() * radius * 2 - 1);
		}
		targetPosition.add(target);
		float t = (float) Math.sqrt((y - targetPosition.y) / 10f);
		velocity = new Vector3f((targetPosition.x - x) / t, 0, (targetPosition.z - z) / t);
	}

	public void updatePosition(float timeStep) {
		add(velocity.mul(timeStep, new Vector3f()));
		velocity.y -= 20 * timeStep;
		y += 10 * timeStep * timeStep;
		float height = MainView.main.getHeight(x, z);
		if (y < height && height - y < 0.5 - velocity.y * timeStep) {
			velocity.y *= -0.5;
			velocity.x *= 0.9f;
			velocity.z *= 0.9f;
			y = height;
		}
	}

	public static void renderSet(Player player, ArrayList<Trash> set) {
		Shaders.textureShader.enable();
		noiseTexture.bind();
		trashVAO.prepareRender();
		for (int i = 0; i < set.size(); ++i) {
			Shaders.textureShader.setUniform(0, 0, player.getCameraMatrix().translate(set.get(i)).scale(0.5f));
			trashVAO.drawTriangles();
		}
		trashVAO.unbind();
	}

	public static void loadRes() {
		trashVAO = new VAO("res/objects/trash.obj");
		noiseTexture = new Texture("res/textures/trashNoise.png");
	}
}
