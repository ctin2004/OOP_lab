package entities;

import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.CanMoveHere;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

import entities.display.DisplayChar1;
import entities.display.iDisplay;
import main.Game;
import utilz.LoadSave;

public class Player extends Entity {
	public BufferedImage[][] animations;
	private int aniTick, aniIndex, aniSpeed = 25;
	private int playerAction = IDLE;
	private boolean moving = false, attacking = false;
	private boolean left, up, right, down;
	private float playerSpeed = 2.0f;

	private iDisplay iDisplay;

	private int[][] lvlData;
	private float xOffSet = 21 * Game.SCALE; // smaller hitbox width for player
	private float yOffSet = 4 * Game.SCALE; // smaller hitbox height for player


	public Player(float x, float y, int width, int height) {
		super(x, y, width, height);
		loadAnimations();
		initHitbox(x, y, 20 * Game.SCALE, 28 * Game.SCALE );
	}

	public void update() {
		updatePos();
		updateAnimationTick();
		setAnimation();
	}

	public void render(Graphics g) {
		g.drawImage(animations[playerAction][aniIndex], (int) (hitbox.x - xOffSet),
				(int) (hitbox.y - yOffSet), width, height, null);
		drawHitbox(g);
	}

	private void updateAnimationTick() {
		aniTick++;
		if (aniTick >= aniSpeed) {
			aniTick = 0;
			aniIndex++;
			if (aniIndex >= GetSpriteAmount(playerAction)) {
				aniIndex = 0;
				attacking = false;
			}

		}

	}

	private void setAnimation() {
		int startAni = playerAction;

		if (moving)
			playerAction = RUNNING;
		else
			playerAction = IDLE;

		if (attacking)
			playerAction = ATTACK_1;

		if (startAni != playerAction)
			resetAniTick();
	}

	private void resetAniTick() {
		aniTick = 0;
		aniIndex = 0;
	}

	private void updatePos() {
		moving = false;

		if(!left && !right && !up && !down)
			return;

		float xSpeed = 0, ySpeed = 0;

		if (left && !right)
			xSpeed = -playerSpeed;
		else if (right && !left)
			xSpeed = playerSpeed;


		if (up && !down)
			ySpeed = -playerSpeed;
		else if (down && !up)
			ySpeed = playerSpeed;

//		if (CanMoveHere(x + xSpeed, y + ySpeed, width, height, lvlData)) {
//			this.x += xSpeed;
//			this.y += ySpeed;
//			moving = true;
//		}

		if (CanMoveHere(hitbox.x + xSpeed, hitbox.y + ySpeed, hitbox.width, hitbox.height, lvlData)) {
			hitbox.x += xSpeed;
			hitbox.y += ySpeed;
			moving = true;
		}
	}

	private void loadAnimations() {
		iDisplay = new DisplayChar1();
		iDisplay.loadAnimations(this);
	}
	public void loadLvlData(int[][] lvlData) {
		this.lvlData = lvlData;

	}

	public void resetDirBooleans() {
		left = false;
		right = false;
		up = false;
		down = false;
	}

	public void setAttacking(boolean attacking) {
		this.attacking = attacking;
	}

	public boolean isLeft() {
		return left;
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public boolean isUp() {
		return up;
	}

	public void setUp(boolean up) {
		this.up = up;
	}

	public boolean isRight() {
		return right;
	}

	public void setRight(boolean right) {
		this.right = right;
	}

	public boolean isDown() {
		return down;
	}

	public void setDown(boolean down) {
		this.down = down;
	}

}
