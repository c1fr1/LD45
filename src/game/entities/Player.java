package game.entities;

import engine.Entities.Camera;
import engine.OpenGL.EnigWindow;
import engine.Platform.Ray3f;
import game.UserControls;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static java.lang.Math.PI;

public class Player extends Camera {
	public Vector3f velocity = new Vector3f();
	
	public Player(EnigWindow window) {
		super((float) PI * 0.25f, 0.01f, 9e9f, window);
	}
	
	public void updateMovement(EnigWindow window, float timeStep, float height) {
		Vector2f movement = new Vector2f();
		if (UserControls.forward(window)) {
			movement.add(getRotated2DVector(0f, -1f, 1f));
		}
		if (UserControls.backward(window)) {
			movement.add(getRotated2DVector(0f, 1f, 1f));
		}
		if (UserControls.left(window)) {
			movement.add(getRotated2DVector(-1f, 0f, 1f));
		}
		if (UserControls.right(window)) {
			movement.add(getRotated2DVector(1f, 0f, 1f));
		}
		if (UserControls.up(window) && y < height + 0.01f) {
			velocity.add(0, 7, 0);
		}
		if(movement.lengthSquared() > 0.2f) {
			movement.normalize(2*timeStep);
			x += movement.x;
			z += movement.y;
		}
		add(velocity.mul(timeStep, new Vector3f()));
		velocity.y -= 0.4f;
		if (y <= height) {
			y = height;
			velocity.y = 0;
		}
	}
	
	public Ray3f getRay() {
		return new Ray3f(this, getRotatedVector(0, 0, -1f, 1f));
	}
	
	public void updateRotation(EnigWindow window, float timeStep) {
		yaw(timeStep * -(float) window.cursorXOffset / 10f);
		pitch(timeStep * -(float) window.cursorYOffset / 10f);
		if (pitch < -PI / 2) {
			pitch = (float)(-PI / 2);
		}
		if (pitch > PI / 2) {
			pitch = (float)(PI / 2);
		}
		//wSystem.out.println(yaw);
		updateRotations();
	}

	@Override
	public Matrix4f getCameraMatrix() {
		return super.getCameraMatrix().translate(0, -0.5f, 0);
	}

	public int getDirection() {
		float dir = (float) (yaw + PI / 4) % (float)(PI * 2);
		if (dir < 0) {
			dir += (float) (PI * 2);
		}
		return (int) (dir / (PI / 2));
	}

	public int getXPos() {
		return (int) Math.floor((x + 1.5) / 3);
	}

	public int getZPos() {
		return (int) Math.floor((z + 1.5) / 3);
	}
}
