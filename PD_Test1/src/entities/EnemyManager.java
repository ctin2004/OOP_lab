package entities;

import entities.display.DisplayCrabEnemy;
import entities.display.iDisplay;
import gamestates.Playing;
import gamestates.iObservers;
import levels.Level;
import utilz.LoadSave;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static utilz.Constants.EnemyConstants.*;

public class EnemyManager implements iObservers {

	private Playing playing;
	private BufferedImage[][] crabbyArr;
	private ArrayList<Crabby> crabbies = new ArrayList<>();

	public EnemyManager(Playing playing) {
		this.playing = playing;
		loadEnemyImgs();
	}

	public void loadEnemies(Level level) {
		crabbies = level.getCrabs();
		System.out.println("size of crabs: " + crabbies.size());
	}

	public void update(int[][] lvlData, Player player) {
		boolean isAnyActive = false;
		for (Crabby c : crabbies)
			if (c.isActive()) {
				c.update(lvlData, player);
				isAnyActive = true;
			}
		if (!isAnyActive)
			playing.setLevelCompleted(true);
	}

	public void draw(Graphics g, int xLvlOffset) {
		drawCrabs(g, xLvlOffset);
	}

	private void drawCrabs(Graphics g, int xLvlOffset) {
		for (Crabby c : crabbies) {
			if (c.isActive()) {
				g.drawImage(crabbyArr[c.getEnemyState()][c.getAniIndex()], (int) c.getHitbox().x - xLvlOffset - CRABBY_DRAWOFFSET_X + c.flipX(), (int) c.getHitbox().y - CRABBY_DRAWOFFSET_Y,
						CRABBY_WIDTH * c.flipW(), CRABBY_HEIGHT, null);
				//c.drawHitbox(g, xLvlOffset);
				//c.drawAttackBox(g, xLvlOffset);

			}
		}
	}

	public void checkEnemyHit(Rectangle2D.Float attackBox) {
		for (Crabby c : crabbies)
			if (c.getCurrentHealth() > 0 )

				if (c.isActive())
					if (attackBox.intersects(c.getHitbox())) {
						c.hurt(10);
						return;
					}
	}
	private void loadEnemyImgs() {
		iDisplay displayCrabEnemy = new DisplayCrabEnemy();
		crabbyArr = displayCrabEnemy.loadAnimations();
	}

	@Override
	public void resetAll() {
		for (Crabby c : crabbies)
			c.resetEnemy();
	}
}
