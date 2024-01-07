package objects;

import gamestates.Playing;
import levels.Level;
import utilz.LoadSave;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static utilz.Constants.ObjectsConstants.*;
import static utilz.Constants.ObjectsConstants.CONTAINER_HEIGHT;

public class ObjectManager {
    private Playing playing;
    private BufferedImage[][] potionImgs, containerImgs;
    private ArrayList<Potion> potions;
    private ArrayList<GameContainer> containers;

    public ObjectManager(Playing playing) {
        this.playing = playing;
        loadImgs();
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
    public void applyEffectToPlayer(Potion p) {
        if (p.getObjType() == RED_POTION) {
            playing.getPlayer().changeHealth(RED_POTION_VALUE);
        } else {
            playing.getPlayer().changePower(BLUE_POTION_VALUE);
        }

    }
    public void checkObjectHit(Rectangle2D.Float attackBox) {
        for (GameContainer c : containers) {
            if (c.isActive()) {
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
        potions = newLevel.getPotions();
        containers = newLevel.getContainers();
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
    }
    public void update() {
        for (Potion p : potions) {
            if (p.isActive())
                p.update();
        }

        for (GameContainer c: containers) {
            if (c.isActive()) {
                c.update();
            }
        }
    }
    public void resetAllObjects() {
        for (Potion p: potions)
            p.reset();
        for (GameContainer c: containers)
            c.reset();
    }
    public void draw(Graphics g, int xLvlOffset) {
        drawPotions(g, xLvlOffset);
        drawContainers(g, xLvlOffset);
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
