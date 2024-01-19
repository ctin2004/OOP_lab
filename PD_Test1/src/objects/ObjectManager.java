package objects;

import gamestates.Playing;
import gamestates.iObservers;
import levels.Level;
import main.Game;
import utilz.LoadSave;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import entities.Player;

import javax.security.auth.Subject;

import static utilz.Constants.ObjectsConstants.*;
import static utilz.Constants.Projectiles.CANNON_BALL_HEIGHT;
import static utilz.Constants.Projectiles.CANNON_BALL_WIDTH;
import static utilz.HelpMethods.CanCannonSeePlayer;
import static utilz.HelpMethods.IsProjectileHittingLevel;

public class ObjectManager implements iObservers {
    private Playing playing;
    private BufferedImage[][] potionImgs, containerImgs;
    private BufferedImage spikeImg;
    private BufferedImage[] cannonImgs;
    private BufferedImage cannonBallImg;
    private ArrayList<Potion> potions;
    private ArrayList<GameContainer> containers;
    private ArrayList<Spike> spikes;
    private ArrayList<Cannon> cannons;
    private ArrayList<Projectile> projectiles = new ArrayList<>();

    public ObjectManager(Playing playing) {
        this.playing = playing;
        loadImgs();
    }

    public void checkSpikesTouched(Player p) {
		for (Spike s : spikes)
			if (s.getHitbox().intersects(p.getHitbox()))
				p.kill();
	}
    public void checkObjectTouched(Rectangle2D.Float hitbox) {
        for (Potion p : potions) {
            if(p.isActive()) {
                if (hitbox.intersects(p.getHitbox())) {
                    p.setActive(false); // when touch the potion => not active anymore
                    applyEffectToPlayer(p);
                }
            }
        }

    }
//    public void applyEffectToPlayer(Potion p) {
//        if (p.getObjType() == RED_POTION) {
//            playing.getPlayer().changeHealth(RED_POTION_VALUE);
//        } else {
//            playing.getPlayer().changePower(BLUE_POTION_VALUE);
//        }
//
//    }

    public void applyEffectToPlayer(Potion p) {
        if (p.getObjType() == RED_POTION) {
            playing.getPlayer().changeHealth(RED_POTION_VALUE);
        } else {
            playing.setLevelCompleted(true);
        }

    }

    public void checkObjectHit(Rectangle2D.Float attackBox) {
        for (GameContainer c : containers) {
            if (c.isActive() && !c.doAnimation) {
                if(c.getHitbox().intersects(attackBox)) {
                    c.setAnimation(true); // when attacking => animation of being destroyed

                    int type = 0;
                    if (c.getObjType() == BARREL)
                        type = 1;

                    potions.add( new Potion((int) (c.getHitbox().x + c.getHitbox().width/2),
                            (int) (c.getHitbox().y - c.getHitbox().height/1.5),
                            type) );

                    return;
                }
            }
        }

    }


    public void loadObjects(Level newLevel) {
        potions = new ArrayList<>(newLevel.getPotions());
        containers = new ArrayList<>(newLevel.getContainers());
        spikes = newLevel.getSpikes(); // spike is static, be there always
        cannons = newLevel.getCannons(); // same for cannon
        projectiles.clear(); // everytime load new level, clear arraylist of projectiles
    }
    private void loadImgs() {
        // read the img of potion atlas
        BufferedImage potionSprite = LoadSave.GetSpriteAtlas(LoadSave.POTION_ATLAS);
        // a 2-d array to store the potions img
        potionImgs = new BufferedImage[2][7];

        for (int j = 0; j < potionImgs.length; j++) {
            for (int i = 0; i < potionImgs[j].length; i++) {
                // to store the img in PotionAtlas to the array of Potion
                potionImgs[j][i] = potionSprite.getSubimage(12*i, 16*j, 12, 16);
            }
        }

        BufferedImage containerSprite = LoadSave.GetSpriteAtlas(LoadSave.CONTAINER_ATLAS);
        containerImgs = new BufferedImage[2][8];
        for (int j = 0; j < containerImgs.length; j++) {
            for (int i = 0; i < containerImgs[j].length; i++) {
                // to store the img in ContainerAtlas to the array of Container
                containerImgs[j][i] = containerSprite.getSubimage(40*i, 30*j, 40, 30);
            }
        }

        spikeImg = LoadSave.GetSpriteAtlas(LoadSave.TRAP_ATLAS);
        
        cannonImgs = new BufferedImage[7];
        BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.CANNON_ATLAS);
        
        for(int i = 0; i < cannonImgs.length; i++){
            cannonImgs[i] = temp.getSubimage(i*40, 0, 40, 26);
        }

        cannonBallImg = LoadSave.GetSpriteAtlas(LoadSave.CANNON_BALL);


    }
    public void update(int[][] lvlData, Player player) {
        for (Potion p : potions) {
            if (p.isActive())
                p.update();
        }

        for (GameContainer c: containers) {
            if (c.isActive()) {
                c.update();
            }
        }

        updateCannons(lvlData, player); 
        updateProjectiles(lvlData, player);
    }
    private void updateProjectiles(int[][] lvlData, Player player) {
		for (Projectile p : projectiles)
			if (p.isActive()) {
				p.updatePos();
				if (p.getHitbox().intersects(player.getHitbox())) {
					player.changeHealth(-25);
					p.setActive(false);
				} else if (IsProjectileHittingLevel(p, lvlData))
					p.setActive(false);
			}
	}

    private boolean isPlayerInRange(Cannon c, Player player) {
		int absValue = (int) Math.abs(player.getHitbox().x - c.getHitbox().x);
		return absValue <= Game.TILES_SIZE * 5;
	}

	private boolean isPlayerInfrontOfCannon(Cannon c, Player player) {
		if (c.getObjType() == CANNON_LEFT) {
			if (c.getHitbox().x > player.getHitbox().x)
				return true;

		} else if (c.getHitbox().x < player.getHitbox().x)
			return true;
		return false;
	}
    private void updateCannons(int[][] lvlData, Player player) {
		for (Cannon c : cannons) {
			if (!c.doAnimation)
				if (c.getTileY() == player.getTileY())
					if (isPlayerInRange(c, player))
						if (isPlayerInfrontOfCannon(c, player))
							if (CanCannonSeePlayer(lvlData, player.getHitbox(), c.getHitbox(), c.getTileY()))
								c.setAnimation(true);

			c.update();
            // set the cannon be in the ready state to shoot, 1st index of animation
			if (c.getAniIndex() == 4 && c.getAniTick() == 0)
				shootCannon(c);
		}
	}

	private void shootCannon(Cannon c) {
		int dir = 1;
		if (c.getObjType() == CANNON_LEFT)
			dir = -1;

		projectiles.add(new Projectile((int) c.getHitbox().x, (int) c.getHitbox().y, dir));

	}

    /* if the cannon is not animating
     * tileY is same
     * ifPlayer is in range
     * is player infront of cannon 
     * in the seight 
     * shoot 
     */
    

    public void resetAll() {
        loadObjects(playing.getLevelManager().getCurrentLevel());
        for (Potion p: potions)
            p.reset();
        for (GameContainer c: containers)
            c.reset();
        for (Cannon c : cannons)
			c.reset();
    }


    public void draw(Graphics g, int xLvlOffset) {
        drawPotions(g, xLvlOffset);
        drawContainers(g, xLvlOffset);
        drawTraps(g, xLvlOffset);
        drawCannons(g, xLvlOffset);
        drawProjectiles(g, xLvlOffset);
    }

    private void drawProjectiles(Graphics g, int xLvlOffset) {
		for (Projectile p : projectiles)
			if (p.isActive())
				g.drawImage(cannonBallImg, (int) (p.getHitbox().x - xLvlOffset), (int) (p.getHitbox().y), CANNON_BALL_WIDTH, CANNON_BALL_HEIGHT, null);

	}

    private void drawCannons(Graphics g, int xLvlOffset) {
        for (Cannon c : cannons) {
			int x = (int) (c.getHitbox().x - xLvlOffset);
			int width = CANNON_WIDTH;

			if (c.getObjType() == CANNON_RIGHT) {
				x += width;
				width *= -1;
			}

			g.drawImage(cannonImgs[c.getAniIndex()], x, (int) (c.getHitbox().y), width, CANNON_HEIGHT, null);
		}
    }

    private void drawTraps(Graphics g, int xLvlOffset) {
		for (Spike s : spikes)
			g.drawImage(spikeImg, (int) (s.getHitbox().x - xLvlOffset), (int) (s.getHitbox().y - s.getyDrawOffset()), SPIKE_WIDTH, SPIKE_HEIGHT, null);

	}
    private void drawPotions(Graphics g, int xLvlOffset) {
        for (Potion p : potions) {

            if (p.isActive()) {

                int type = 0;
                if(p.getObjType() == RED_POTION)
                    type = 1;

                g.drawImage(potionImgs[type][p.getAniIndex()],
                        (int) (p.getHitbox().x - p.getxDrawOffset() - xLvlOffset),
                        (int) (p.getHitbox().y - p.getyDrawOffset()),
                        POTION_WIDTH,
                        POTION_HEIGHT,
                        null);
            }
        }
    }

    private void drawContainers(Graphics g, int xLvlOffset) {
        for (GameContainer c : containers) {

            if (c.isActive()) {

                int type = 0; // BOX
                if (c.getObjType() == BARREL)
                    type = 1;

                g.drawImage(containerImgs[type][c.getAniIndex()],
                        (int) (c.getHitbox().x - c.getxDrawOffset() - xLvlOffset),
                        (int) (c.getHitbox().y - c.getyDrawOffset()),
                        CONTAINER_WIDTH,
                        CONTAINER_HEIGHT,
                        null);
            }
        }

    }


}
