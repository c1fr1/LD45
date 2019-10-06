package game.structures;

import engine.OpenGL.VAO;
import game.Shaders;
import game.entities.Player;
import game.views.MainView;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Support {
	private static VAO supportVAO;

	public int fromX;
	public int fromZ;

	public int toX;
	public int toZ;

	public Support(Player p) {
		fromX = p.getXPos();
		fromZ = p.getZPos();
		int dir = p.getDirection();
		if (dir == 0) {
			toX = fromX;
			toZ = fromZ - 1;
		} else if (dir == 1) {
			toX = fromX + 1;
			toZ = fromZ;
		} else if (dir == 2) {
			toX = fromX;
			toZ = fromZ + 1;
		} else {
			toX = fromX - 1;
			toZ = fromZ;
		}
	}

	public void render(Matrix4f cam) {
		Vector3f from = new Vector3f(fromX, 0f, fromZ);
		if (fromX - toX == 1) {
			Shaders.colorShader.setUniform(0, 0, cam.translate(fromX * 3f, -0.2f, fromZ * 3f).rotateZ((float) Math.PI / 2f).scale(1, 3, 1));
		} else if (fromX - toX == -1) {
			Shaders.colorShader.setUniform(0, 0, cam.translate(fromX * 3f, -0.2f, fromZ * 3f).rotateZ((float) -Math.PI / 2f).scale(1, 3, 1));
		} else if (fromZ - toZ == 1) {
			Shaders.colorShader.setUniform(0, 0, cam.translate(fromX * 3f, -0.2f, fromZ * 3f).rotateX((float) -Math.PI / 2f).scale(1, 3, 1));
		} else {
			Shaders.colorShader.setUniform(0, 0, cam.translate(fromX * 3f, -0.2f, fromZ * 3f).rotateX((float) Math.PI / 2f).scale(1, 3, 1));
		}
		supportVAO.fullRender();
	}

	public static void renderPreview(Player player) {
		Matrix4f cam = player.getCameraMatrix();
		int dir = player.getDirection();
		Support s = new Support(player);
		if (MainView.main.supportCanBeSupported(s.fromX, s.fromZ, s.toX, s.toZ)) {
			if (player.scrap > 1) {
				Shaders.colorShader.setUniform(2, 0, new Vector3f(0f, 1f, 1f));
			} else {
				Shaders.colorShader.setUniform(2, 0, new Vector3f(1f, 1f, 0f));
			}
		} else {
			Shaders.colorShader.setUniform(2, 0, new Vector3f(1f, 0f, 0f));
		}
		if (dir == 0) {
			Shaders.colorShader.setUniform(0, 0, cam.translate(player.getXPos() * 3f, -0.2f, player.getZPos() * 3f).rotateX((float) -Math.PI / 2f).scale(1, 3, 1));
		} else if (dir == 2) {
			Shaders.colorShader.setUniform(0, 0, cam.translate(player.getXPos() * 3f, -0.2f, player.getZPos() * 3f).rotateX((float) Math.PI / 2f).scale(1, 3, 1));
		} else if (dir == 1) {
			Shaders.colorShader.setUniform(0, 0, cam.translate(player.getXPos() * 3f, -0.2f, player.getZPos() * 3f).rotateZ((float) -Math.PI / 2f).scale(1, 3, 1));
		} else {
			Shaders.colorShader.setUniform(0, 0, cam.translate(player.getXPos() * 3f, -0.2f, player.getZPos() * 3f).rotateZ((float) Math.PI / 2f).scale(1, 3, 1));
		}
		supportVAO.fullRender();
	}

	public static void loadRes() {
		supportVAO = new VAO("res/objects/support.obj");
	}
}
