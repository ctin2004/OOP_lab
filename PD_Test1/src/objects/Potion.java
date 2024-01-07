package objects;

import main.Game;

public class Potion extends GameObjects {
    private float hoverOffset;
    private int maxHoverOffset, hoverDirection = 1; // go down at first
    public Potion(int x, int y, int objType) {
        super(x, y, objType);
        doAnimation = true; // potions animated as long as active
        initHitbox(7,14);

        xDrawOffset = (int) (Game.SCALE * 3);
        yDrawOffset = (int) (Game.SCALE * 2);

        maxHoverOffset = (int) (10 * Game.SCALE);
    }
    public void update() {
        updateAnimationTick();
        updateHover();
    }

    private void updateHover() {
        hoverOffset += (0.12f * Game.SCALE * hoverDirection);
        if (hoverOffset >= maxHoverOffset)
            hoverDirection = -1;
        else if (hoverOffset < 0)
            hoverDirection = 1;

        hitbox.y = y + hoverOffset; // y from GameObject
    }
}
