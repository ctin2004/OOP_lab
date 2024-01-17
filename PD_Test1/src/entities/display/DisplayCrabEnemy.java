package entities.display;

import utilz.LoadSave;

import java.awt.image.BufferedImage;

import static utilz.Constants.EnemyConstants.CRABBY_HEIGHT_DEFAULT;
import static utilz.Constants.EnemyConstants.CRABBY_WIDTH_DEFAULT;

public class DisplayCrabEnemy implements iDisplay{
    @Override
    public BufferedImage[][] loadAnimations() {
        BufferedImage[][] crabbyArr;
        crabbyArr = new BufferedImage[5][9];
        BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.CRABBY_SPRITE);
        for (int j = 0; j < crabbyArr.length; j++) {
            for (int i = 0; i < crabbyArr[j].length; i++)
                crabbyArr[j][i] = temp.getSubimage(i * CRABBY_WIDTH_DEFAULT, j * CRABBY_HEIGHT_DEFAULT, CRABBY_WIDTH_DEFAULT, CRABBY_HEIGHT_DEFAULT);
        }
        return crabbyArr;
    }
}
