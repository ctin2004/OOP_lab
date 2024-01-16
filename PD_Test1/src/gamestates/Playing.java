package gamestates;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import entities.EnemyManager;
import entities.Player;
import levels.LevelManager;
import main.Game;
import objects.ObjectManager;
import ui.GameOverOverlay;
import ui.LevelCompletedOverlay;
import ui.PauseOverlay;
import utilz.LoadSave;
import static utilz.Constants.Environment.*;

public class Playing extends State implements Statemethods {
	private Player player;
	private LevelManager levelManager;
	private EnemyManager enemyManager;
	private ObjectManager objectManager;
	private PauseOverlay pauseOverlay;
	private GameOverOverlay gameOverOverlay;
	private LevelCompletedOverlay levelCompletedOverlay;
	private boolean paused = false;
	private boolean levelCompleted;
	private boolean playerDying;

	private int xLvlOffset;
	private int leftBorder = (int) (0.2 * Game.GAME_WIDTH);
	private int rightBorder = (int) (0.8 * Game.GAME_WIDTH);
	private int maxLvlOffsetX;

	private BufferedImage backgroundImg, bigCloud, smallCloud;
	private int smallCloudPos[];
	private Random rnd = new Random();

	private boolean gameOver;


	public Playing(Game game) {
		super(game);
		initClasses();

		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.PLAYING_BG_IMG);
		bigCloud = LoadSave.GetSpriteAtlas(LoadSave.BIG_CLOUDS);
		smallCloud= LoadSave.GetSpriteAtlas(LoadSave.SMALL_CLOUDS);
		smallCloudPos = new int[8];
		for (int i = 0; i < smallCloudPos.length; i++)
			smallCloudPos[i] = (int) (90 *Game.SCALE) + rnd.nextInt((int)(150* Game.SCALE));

		calcLevelOffset();
		loadStartLevel();
	}

	private void loadStartLevel() {

		enemyManager.loadEnemies(levelManager.getCurrentLevel());
		objectManager.loadObjects(levelManager.getCurrentLevel());
	}
	public void loadNextLevel() {
		resetAll();
		levelManager.loadNextLevel();
		player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
	}
	private void calcLevelOffset() {
		maxLvlOffsetX = levelManager.getCurrentLevel().getLevelOffset();
	}

	private void initClasses() {
		levelManager = new LevelManager(game);
		enemyManager = new EnemyManager(this);
		objectManager = new ObjectManager(this);
		player = new Player(200, 200, (int) (64 * Game.SCALE), (int) (40 * Game.SCALE), this);
		player.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
		pauseOverlay = new PauseOverlay(this);
		gameOverOverlay = new GameOverOverlay(this);
		levelCompletedOverlay = new LevelCompletedOverlay(this);

	}

	@Override
	public void update() {
		if (paused)
			pauseOverlay.update();
		else if (levelCompleted)
			levelCompletedOverlay.update();
		else if(gameOver)
			gameOverOverlay.update();
		else if(playerDying){
			player.update();
		}
		else {
			levelManager.update();
			objectManager.update(levelManager.getCurrentLevel().getLevelData(), player);
			player.update();
			enemyManager.update(levelManager.getCurrentLevel().getLevelData(), player);
			checkCloseToBorder();
		}

		if (!paused && !gameOver && !levelCompleted) {
			levelManager.update();
			player.update();
			enemyManager.update(levelManager.getCurrentLevel().getLevelData(), player);
			checkCloseToBorder();
		} else {
			pauseOverlay.update();
		}

	}

	private void checkCloseToBorder() {
		int playerX = (int) player.getHitbox().x;
		int diff = playerX - xLvlOffset;

		if (diff > rightBorder)
			xLvlOffset += diff - rightBorder;
		else if (diff < leftBorder)
			xLvlOffset += diff - leftBorder;

		if (xLvlOffset > maxLvlOffsetX)
			xLvlOffset = maxLvlOffsetX;
		else if (xLvlOffset < 0)
			xLvlOffset = 0;
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(backgroundImg,0,0,Game.GAME_WIDTH,Game.GAME_HEIGHT,null);
		drawClouds(g);
		
		levelManager.draw(g,xLvlOffset);
		player.render(g,xLvlOffset);
		enemyManager.draw(g, xLvlOffset);
		objectManager.draw(g, xLvlOffset);

		if (paused){
			g.setColor(new Color(0, 0, 0, 150));
			g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
			pauseOverlay.draw(g);
		} else if (gameOver)
			gameOverOverlay.draw(g);
		else if (levelCompleted)
			levelCompletedOverlay.draw(g);
	}

	private void drawClouds(Graphics g) {
		for (int i =0; i < 3; i++) {
			g.drawImage(bigCloud, i * BIG_CLOUD_WIDTH - (int) (xLvlOffset * 0.3), (int) (204 * Game.SCALE), BIG_CLOUD_WIDTH, BIG_CLOUD_HEIGHT, null);
		}

		for (int i =0; i < smallCloudPos.length; i++)
			g.drawImage(smallCloud,SMALL_CLOUD_WIDTH * 4 * i - (int) (xLvlOffset * 0.7),smallCloudPos[i], SMALL_CLOUD_WIDTH,SMALL_CLOUD_HEIGHT,null);

	}

	public void resetAll() {
		gameOver = false;
		paused = false;
		levelCompleted = false;
		playerDying = false; // player alive in new game after die
		player.resetAll();
		enemyManager.resetAllEnemies();
		objectManager.resetAllObjects();
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}

	public void checkEnemyHit(Rectangle2D.Float attackBox) {
		enemyManager.checkEnemyHit(attackBox);
	}
	public void checkPotionTouched(Rectangle2D.Float hitbox) {
		objectManager.checkObjectTouched(hitbox);
	}
	public void checkObjectHit(Rectangle2D.Float attackBox) {
		objectManager.checkObjectHit(attackBox);
	}
	public void checkSpikesTouched(Player p) {
		objectManager.checkSpikesTouched(p);
    }


	@Override
	public void mouseClicked(MouseEvent e) {
		if (!gameOver) {
			if (e.getButton() == MouseEvent.BUTTON1)
				player.setAttacking(true);
			else if(e.getButton() == MouseEvent.BUTTON3)
				player.powerAttack();
		}
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (gameOver)
			gameOverOverlay.keyPressed(e);
		else
			switch (e.getKeyCode()) {
			case KeyEvent.VK_A:
				player.setLeft(true);
				break;
			case KeyEvent.VK_D:
				player.setRight(true);
				break;
			case KeyEvent.VK_SPACE:
				player.setJump(true);
				break;
			case KeyEvent.VK_K:
				player.setJump(true);
				break;
				//move back to menu
			case KeyEvent.VK_BACK_SPACE:
				paused = !paused;
				break;
			case KeyEvent.VK_J:
				player.setAttacking(true);
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (!gameOver)
			switch (e.getKeyCode()) {
				case KeyEvent.VK_A:
					player.setLeft(false);
					break;
				case KeyEvent.VK_D:
					player.setRight(false);
					break;
				case KeyEvent.VK_SPACE:
					player.setJump(false);
					break;
				case KeyEvent.VK_K:
					player.setJump(false);
					break;
//				case KeyEvent.VK_J:
//					player.setAttacking(false);
//					break;
			}
	}
	public void mouseDragged(MouseEvent e) {
		if (!gameOver)
			if (paused)
				pauseOverlay.mouseDragged(e);
	}
	@Override
	public void mousePressed(MouseEvent e) {
		if (!gameOver) {
			if (paused)
				pauseOverlay.mousePressed(e);
			else if (levelCompleted)
				levelCompletedOverlay.mousePressed(e);
		}else{
			gameOverOverlay.mousePressed(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (!gameOver) {
			if (paused)
				pauseOverlay.mouseReleased(e);
			else if (levelCompleted)
				levelCompletedOverlay.mouseReleased(e);
		}else{
			gameOverOverlay.mouseReleased(e);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (!gameOver)
			if (paused)
				pauseOverlay.mouseMoved(e);
			else if (levelCompleted)
				levelCompletedOverlay.mouseMoved(e);
		else{
			gameOverOverlay.mouseMoved(e);
		}
	}
	public void unpauseGame() {
		paused = false;
	}
	public void windowFocusLost() {
		player.resetDirBooleans();
	}
	public void setLevelCompleted(boolean completed) {
		this.levelCompleted = completed;
		if(levelCompleted)
			game.getAudioPlayer().lvlCompleted();
	}
	public void setMaxLevelOffset(int lvlOffset) {
		this.maxLvlOffsetX = lvlOffset;
	}

	public Player getPlayer() {
		return player;
	}
	public EnemyManager getEnemyManager() {
		return enemyManager;
	}
	public ObjectManager getObjectManager() {
		return objectManager;
	}
	public LevelManager getLevelManager(){
		return levelManager;
	}

    public void setPlayerDying(boolean playerDying) {
		this.playerDying = playerDying;
    }

}
