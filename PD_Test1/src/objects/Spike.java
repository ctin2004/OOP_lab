package objects;

import main.Game;

public class Spike extends GameObjects {

    public Spike(int x, int y, int objType) {
        super(x, y, objType);

        initHitbox(32,16); //spike height = 1/2 size
        xDrawOffset = 0;
        yDrawOffset = (int) (Game.SCALE * 16);
        hitbox.y += yDrawOffset;
        
    }
    
}
