package utilz;

import main.Game;

public class HelpMethods {
    public static Boolean CanMoveHere(float x, float y, float width, float height, int[][] lvlData) {
        //check top-left
        if (!IsSolid(x, y, lvlData))
            // check bottom-right
            if (!IsSolid(x+width, y+height, lvlData))
                // check top-right
                if (!IsSolid(x+width, y, lvlData))
                    // check bottom-left
                    if (!IsSolid(x, y+height, lvlData))
                        return true;
        return false;
    }
    public static Boolean IsSolid(float x, float y, int[][] lvlData) {
        if ( x < 0 || x >= Game.GAME_WIDTH)
            return true;

        if ( y < 0 || y >= Game.GAME_HEIGHT)
            return true;


        float xIndex = x / Game.TILES_SIZE;
        float yIndex = y / Game.TILES_SIZE;

        int value = lvlData[(int) yIndex] [(int) xIndex];

        if (value >= 48 || value <= 0 || value != 11)
            return true;

        return false;
    }
}
