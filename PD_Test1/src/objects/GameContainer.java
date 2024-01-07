package objects;

import main.Game;

import static utilz.Constants.ObjectsConstants.*;

public class GameContainer extends GameObjects {
    public GameContainer(int x, int y, int objType) {
        super(x, y, objType);
        createHitbox();
    }

    private void createHitbox() {
        if (objType == BOX) {
            initHitbox(25, 18);

            xDrawOffset = (int) (Game.SCALE * 7);
            yDrawOffset = (int) (Game.SCALE * 12);
        } else {
            initHitbox(23, 25);

            xDrawOffset = (int) (Game.SCALE * 8);
            yDrawOffset = (int) (Game.SCALE * 5);
        }
        hitbox.y += yDrawOffset + (int) (Game.SCALE * 3); // to suit the statistics in our game
        hitbox.x += xDrawOffset/2;
    }
    public void update() {
        if (doAnimation)
            updateAnimationTick();
    }
}
