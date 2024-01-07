package objects;

import main.Game;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static utilz.Constants.ANI_SPEED;
import static utilz.Constants.ObjectsConstants.*;
import static utilz.Constants.ObjectsConstants.GetSpriteAmount;

public class GameObjects {
    protected int x, y, objType;
    protected Rectangle2D.Float hitbox;
    protected boolean doAnimation, active = true;
    protected int aniTick, aniIndex;
    protected int xDrawOffset, yDrawOffset;
    public GameObjects (int x, int y, int objType) {
        this.x = x;
        this.y = y;
        this.objType = objType;
    }
    protected void initHitbox(int width, int height) {
        hitbox = new Rectangle2D.Float(x, y, (int) (width * Game.SCALE),(int) (height * Game.SCALE));
    }
    public void drawHitbox(Graphics g, int xLvlOffset) {
        // For debugging the hitbox
        g.setColor(Color.PINK);
        g.drawRect((int) hitbox.x - xLvlOffset, (int) hitbox.y, (int) hitbox.width, (int) hitbox.height);
    }
    protected void updateAnimationTick() {
        aniTick++;
        if (aniTick >= ANI_SPEED) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= GetSpriteAmount(objType)) {
                aniIndex = 0;

                if (objType == BARREL || objType == BOX ) {
                    doAnimation = false;
                    active = false;
                }
            }
        }
    }
    public void reset() {
        aniTick = 0;
        aniIndex = 0;
        active = true;
        if (objType == BARREL || objType == BOX ) {
            doAnimation = false; // containers
        } else
            doAnimation = true; // potions
    }


    public int getxDrawOffset() {
        return xDrawOffset;
    }
    public int getyDrawOffset() {
        return yDrawOffset;
    }
    public Rectangle2D.Float getHitbox() {
        return hitbox;
    }
    public int getObjType() {
        return objType;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public void setAnimation(boolean doAni) {
        this.doAnimation = doAni;
    }
    public boolean isActive() {
        return active;
    }
    public int getAniIndex() {
        return aniIndex;
    }

}
