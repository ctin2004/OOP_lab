package gamestates;

import main.Game;
import ui.MenuButton;

import java.awt.event.MouseEvent;

public class State {

	protected Game game;

	public State(Game game) {
		this.game = game;
	}
	// this method is used to check whether we click in the button or not
	public boolean isIn (MouseEvent e, MenuButton mb){
		return mb.getBounds().contains(e.getX(),e.getY());
		// return true if we click right in the button, otherwise is false
	}
	public Game getGame() {
		return game;
	}
}
