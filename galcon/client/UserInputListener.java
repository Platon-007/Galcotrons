package galcon.client;

import java.awt.event.*;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 20.04.2013
 * Time: 1:32:40
 * To change this template use File | Settings | File Templates.
 */
public class UserInputListener implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

	private ControlsModel controlsModel;

	private boolean isCtrl;

	public UserInputListener(ControlsModel controlsModel) {
		this.controlsModel = controlsModel;
	}

	public void mouseClicked(MouseEvent e) {
		//controlsModel.clickArea(e.getX(), e.getY(), (e.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0);
	}

	public void mousePressed(MouseEvent e) {
		controlsModel.startSelect(e.getX(), e.getY(), e.isControlDown());
	}

	public void mouseReleased(MouseEvent e) {
		controlsModel.stopSelection(e.getX(), e.getY(), e.isControlDown());
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mouseDragged(MouseEvent e) {
		controlsModel.moveSelection(e.getX(), e.getY(), isCtrl);
	}

	public void mouseMoved(MouseEvent e) {
		controlsModel.hoverAction(e.getX(), e.getY());
	}

	public void mouseWheelMoved(MouseWheelEvent e) {

		controlsModel.changePercentage(e.getWheelRotation()*-5);
	}

	public void keyTyped(KeyEvent e) {
		char code = e.getKeyChar();
		int percent = code - '0';
		if (percent >= 0 && percent <= 9) {
			if (percent == 0)
				percent = 10;
			controlsModel.setPercentage(percent*10);
		}
	}

	public void keyPressed(KeyEvent e) {
		/*e.isControlDown();
		if (e.getKeyCode() == KeyEvent.VK_CONTROL)
			isCtrl = true;*/
	}

	public void keyReleased(KeyEvent e) {
		/*if (e.getKeyCode() == KeyEvent.VK_CONTROL)
			isCtrl = false;*/
	}
}
