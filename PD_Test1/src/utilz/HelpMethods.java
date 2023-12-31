package utilz;

import java.awt.geom.Rectangle2D;
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
        int maxWidth = lvlData[0].length * Game.TILES_SIZE;
        if (x < 0 || x >= maxWidth)
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


    // add new methods

    //calculate next x-pos for player next to a wall
    public static float GetEntityXPosNextToWall(Rectangle2D.Float hitbox, float xSpeed) {
        int currentTile = (int) (hitbox.x / Game.TILES_SIZE);
        // check x-axis speed
        if (xSpeed > 0) {
            // Right
            int tileXPos = currentTile * Game.TILES_SIZE;
            // distance btw the right edge of the tile to the right of the hitbox
            int xOffset = (int) (Game.TILES_SIZE - hitbox.width);
            return tileXPos + xOffset - 1; //-1: adjust to fit the edge
        } else
            // Left or other direction, return x-coordinate of the left side
            return currentTile * Game.TILES_SIZE;
    }

    public static float GetEntityYPosUnderRoofOrAboveFloor(Rectangle2D.Float hitbox, float airSpeed) {
        int currentTile = (int) (hitbox.y / Game.TILES_SIZE);
        if (airSpeed > 0) {
            // Falling - touching floor
            int tileYPos = currentTile * Game.TILES_SIZE;
            int yOffset = (int) (Game.TILES_SIZE - hitbox.height);
            return tileYPos + yOffset - 1; //-1: adjust to fit the ground
        } else
            // Jumping
            return currentTile * Game.TILES_SIZE;

    }

    public static boolean IsEntityOnFloor(Rectangle2D.Float hitbox, int[][] lvlData) {
        // Check the pixel below bottomleft and bottomright
        if (!IsSolid(hitbox.x, hitbox.y + hitbox.height + 1, lvlData))
            if (!IsSolid(hitbox.x + hitbox.width, hitbox.y + hitbox.height + 1, lvlData))
                return false;

        return true;

    }
}
