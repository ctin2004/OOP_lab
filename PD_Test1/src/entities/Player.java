package entities;

import static utilz.Constants.ANI_SPEED;
import static utilz.Constants.GRAVITY;
import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import audio.AudioPlayer;
import gamestates.Playing;
import gamestates.iObservers;
import main.Game;
import utilz.LoadSave;


import entities.display.DisplayChar1;
import entities.display.iDisplay;


public class Player extends Entity implements iObservers {
	public BufferedImage[][] animations;
	private boolean moving = false, attacking = false;
	private boolean left, right, jump;
//	private float playerSpeed = 0.5f * Game.SCALE ;
	private int[][] lvlData;
	private float xDrawOffset = 21 * Game.SCALE;
	private float yDrawOffset = 4 * Game.SCALE;


	// Jumping / Gravity
	private float jumpSpeed = -2.55f * Game.SCALE; // fix jump height to pass the terrain, = jumpheight
	private float fallSpeedAfterCollision = 0.1f * Game.SCALE;

	// StatusBarUI
	private BufferedImage statusBarImg;

	private int statusBarWidth = (int) (192 * Game.SCALE);
	private int statusBarHeight = (int) (58 * Game.SCALE);
	private int statusBarX = (int) (10 * Game.SCALE);
	private int statusBarY = (int) (10 * Game.SCALE);

	// health bar
	private int healthBarWidth = (int) (150 * Game.SCALE);
	private int healthBarHeight = (int) (4 * Game.SCALE);
	private int healthBarXStart = (int) (34 * Game.SCALE);
	private int healthBarYStart = (int) (14 * Game.SCALE);
	private int healthWidth = healthBarWidth;

	// power bar
	private int powerBarWidth = (int) (104 * Game.SCALE); // width
	private int powerBarHeight = (int) (2 * Game.SCALE); // height
	private int powerBarXStart = (int) (44 * Game.SCALE); // start pos of X
	private int powerBarYStart = (int) (34 * Game.SCALE); // start pos of Y
	private int powerWidth = powerBarWidth; // will be 100%
	private int powerMaxValue = 200;
	private int powerValue = powerMaxValue;


	private int flipX = 0;
	private int flipW = 1;

	private boolean attackChecked;
	private Playing playing;

	private int tileY = 0;

	private boolean powerAttackActive; // during attack or not
	private int powerAttackTick; // +1 when powerAttackActive update, then stop the powerAttack
	private int powerGrowSpeed = 15; // slowly increase the player's power
	private int powerGrowTick;


	public Player(float x, float y, int width, int height, Playing playing) {
		super(x, y, width, height);
		loadAnimations();
		this.playing = playing;
		this.state = IDLE;
		this.maxHealth = 100;
		this.currentHealth = maxHealth;
		this.walkSpeed = 0.5f * Game.SCALE;

		// we have to change this bcs 28* Game.Scale 1.5 = decimal value -false
		initHitbox(20, 28);
		initAttackBox();

	}
	public void setSpawn(Point spawn) {
		this.x = spawn.x;
		this.y = spawn.y;
		hitbox.x = x;
		hitbox.y = y;
	}

	private void initAttackBox() {
		attackBox = new Rectangle2D.Float(x, y, (int) (20 * Game.SCALE), (int) (20 * Game.SCALE));
		resetAttackBox();
	}

	public void update() {
		updateHealthBar();
		updatePowerBar();

		//zero health: set the animation tick, index = 0
		if (currentHealth <= 0) {
			if(state != DEAD){
				state = DEAD;
				aniTick = 0;
				aniIndex = 0;
				playing.setPlayerDying(true);
				playing.getGame().getAudioPlayer().playEffect(AudioPlayer.DIE);
			}
			// at end of animation, set GameOver
			else if(aniIndex == GetSpriteAmount(DEAD) - 1 && aniTick >= ANI_SPEED - 1){ //index start from 0, -1 to not outbounds
				playing.setGameOver(true);
				playing.getGame().getAudioPlayer().stopSong();
				playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GAMEOVER);
			}else{
				updateAnimationTick();
			}
			return;
		}

		updateAttackBox();

		updatePos();
		if (moving) {
			checkPotiontouched();
			checkSpikesTouched();
			tileY = (int) (hitbox.y / Game.TILES_SIZE);
			if (powerAttackActive) {
				powerAttackTick++;
				if (powerAttackTick >= 35) {
					powerAttackTick = 0;
					powerAttackActive = false;
				}
			}
		}
		if (attacking || powerAttackActive)
			checkAttack();

		updateAnimationTick();
		setAnimation();
	}

	private void checkSpikesTouched() {
		playing.checkSpikesTouched(this);
	}
	private void checkPotiontouched() {
		playing.checkPotionTouched(hitbox);
	}

	private void checkAttack() {
		if (attackChecked || aniIndex != 1)
			return;
		attackChecked = true;

		if(powerAttackActive)
			attackChecked = false; // every update check a new attack
		playing.checkEnemyHit(attackBox);
		playing.checkObjectHit(attackBox);
		playing.getGame().getAudioPlayer().playAttackSound();

	}

	private void updateAttackBox() {
		if(right && left) {
			if (flipW == 1) {
				attackBox.x = hitbox.x + hitbox.width + (int) (Game.SCALE * 10);
			} else {
				attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 1);
			}
		} else if (right || (powerAttackActive && flipW == 1))
			attackBox.x = hitbox.x + hitbox.width + (int) (Game.SCALE * 1);
		else if (left || (powerAttackActive && flipW == -1))
			attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 1);

		attackBox.y = hitbox.y + (Game.SCALE * 10);
	}

	private void updateHealthBar() {
		healthWidth = (int) ((currentHealth / (float) maxHealth) * healthBarWidth);
	}

	private void updatePowerBar() {
		powerWidth = (int) ((powerValue / (float) powerMaxValue) * powerBarWidth);

		powerGrowTick++;
		if (powerGrowTick >= powerGrowSpeed) {
			powerGrowTick = 0;
			changePower(1);
		}
	}

	public void render(Graphics g, int lvlOffset) {
		g.drawImage(animations[state][aniIndex], (int) (hitbox.x - xDrawOffset) - lvlOffset + flipX, (int) (hitbox.y - yDrawOffset), width * flipW, height, null);
		//drawHitbox(g, lvlOffset);
		//drawAttackBox(g, lvlOffset);
		drawUI(g);
	}

	private void drawUI(Graphics g) {
		// Background UI
		g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);

		// Health bar
		g.setColor(Color.red);
		g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);
		
		// Power bar
		g.setColor(Color.yellow);
		g.fillRect(powerBarXStart + statusBarX, powerBarYStart + statusBarY, powerWidth /*change by time */, powerBarHeight);

	}


	private void updateAnimationTick() {
		aniTick++;
		if (aniTick >= ANI_SPEED) {
			aniTick = 0;
			aniIndex++;
			if (aniIndex >= GetSpriteAmount(state)) {
				aniIndex = 0;
				attacking = false;
				attackChecked = false;
			}

		}

	}

	private void setAnimation() {
		int startAni = state;

		if (moving)
			state = RUNNING;
		else
			state = IDLE;

		if (inAir) {
			/*player now in air & move upward: jump */
			if (airSpeed < 0)
				state = JUMP;

			/*player now in air & move downward: fall */
			else
				state = FALLING;
		}

		if(powerAttackActive){
			state = ATTACK;
			aniIndex = 1;
			aniTick = 0;
			return;
		}

		if (attacking) {
			state = ATTACK;
			if (startAni != ATTACK) {
				aniIndex = 1;
				aniTick = 0;
				return;
			}
		}
		if (startAni != state)
			resetAniTick();
	}

	private void resetAniTick() {
		aniTick = 0;
		aniIndex = 0;
	}

	private void updatePos() { // player now not move in y-axis
		moving = false;

		if (jump)
			jump();

		if (!inAir)
			if (!powerAttackActive)
				if ((!left && !right) || (right && left))
					return;

		float xSpeed = 0; 
		// deleted ySpeed

		if (left && !right) {
			xSpeed -= walkSpeed;
			flipX = width;
			flipW = -1;
		}
		if (right && !left) {
			xSpeed += walkSpeed;
			flipX = 0;
			flipW = 1;
		}
		if(powerAttackActive){
			if(!left && !right || left && right){ // not move to left or right, none pressed button || both left and right
				if(flipW == -1) // face to left side
					xSpeed = -walkSpeed;
				else // face to the right side
					xSpeed = walkSpeed;

			}
			xSpeed *=3;
		}

		if (!inAir)
			if (!IsEntityOnFloor(hitbox, lvlData))
				inAir = true;

		if (inAir && !powerAttackActive) {
			if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
				hitbox.y += airSpeed * 0.5f;
				airSpeed += GRAVITY * 0.5f;
				updateXPos(xSpeed);
			} else {
				hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
				if (airSpeed > 0)
					resetInAir();
				else
					airSpeed = fallSpeedAfterCollision;
				updateXPos(xSpeed);
			}

		} else
			updateXPos(xSpeed);
		moving = true;
	}

	private void jump() {
		if (inAir)
			return;
		playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
		inAir = true;
		airSpeed = jumpSpeed;

	}

	private void resetInAir() {
		inAir = false;
		airSpeed = 0;

	}

	private void updateXPos(float xSpeed) {
		if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
			hitbox.x += xSpeed;
		} else {
			// hitting a wall
			hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
			if (powerAttackActive) {
				powerAttackActive = false;
				powerAttackTick = 0; // stay in powerAttack certain time
			}
		}

	}

	public void changeHealth(int redPotionValue) {
		currentHealth += redPotionValue;

		if (currentHealth <= 0)
			currentHealth = 0;
		else if (currentHealth >= maxHealth)
			currentHealth = maxHealth;
	}

	public void kill() {
		currentHealth = 0; // die
    }
	public void changePower(int value) {
		powerValue += value;
		if(powerValue >= powerMaxValue)
			powerValue = powerMaxValue;
		else if(powerValue <= 0)
			powerValue = 0;
	}

	private void loadAnimations() {
		iDisplay displayChar1 = new DisplayChar1();
		this.animations = displayChar1.loadAnimations();

		statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);

	}
	public void loadLvlData(int[][] lvlData) {
		this.lvlData = lvlData;
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;

	}

	public void resetDirBooleans() {
		left = false;
		right = false;
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


	public boolean isRight() {
		return right;
	}

	public void setRight(boolean right) {
		this.right = right;
	}


	public void setJump(boolean jump) {
		this.jump = jump;
	}

	@Override
	public void resetAll() {
		resetDirBooleans();
		inAir = false;
		attacking = false;
		moving = false;
		jump = false; // fix bug: dead while still jumping => new game, still jumping
		airSpeed = 0f; // fix bug: dead while still jumping => new game, still jumping
		state = IDLE;
		currentHealth = maxHealth;

		hitbox.x = x;
		hitbox.y = y;

		resetAttackBox();
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}
	private void resetAttackBox() {
		if (flipW == 1) {
			attackBox.x = hitbox.x + hitbox.width + (int) (Game.SCALE * 10);
		} else {
			attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 1);
		}
	}
    
	public int getTileY(){
		return tileY;
	}

    public void powerAttack() {
        if(powerAttackActive)
			return;
		if(powerValue >= 60) {
			powerAttackActive = true;
			changePower(-60);
		}
    }

}
