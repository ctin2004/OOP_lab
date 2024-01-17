package entities.display;

import entities.Player;
import utilz.LoadSave;

import java.awt.image.BufferedImage;

public class DisplayChar1 implements iDisplay {


    public BufferedImage[][] loadAnimations() {
        BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);

        BufferedImage[][] animations;
        animations = new BufferedImage[7][7];
        for (int j = 0; j < animations.length; j++) {
            for (int i = 0; i < animations[j].length; i++)
                animations[j][i] = img.getSubimage(i * 64, j * 40, 64, 40);
        }
        return animations;
    }
}
