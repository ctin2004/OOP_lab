package entities.display;

import entities.Player;
import utilz.LoadSave;

import java.awt.image.BufferedImage;

public class DisplayChar1 implements iDisplay {
    Player player;

    public void loadAnimations(Player player) {
        this.player = player;

        BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);

        player.animations = new BufferedImage[7][7];
        for (int j = 0; j < player.animations.length; j++)
            for (int i = 0; i < player.animations[j].length; i++)
                player.animations[j][i] = img.getSubimage(i * 64, j * 40, 64, 40);

    }
}
