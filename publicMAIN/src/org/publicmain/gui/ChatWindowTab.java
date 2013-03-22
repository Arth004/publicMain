/**
 * 
 */
package org.publicmain.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * @author ABerthold
 *
 */
public class ChatWindowTab extends JPanel implements MouseListener{
	private JLabel lblTitle;
	private JLabel lblClose;
	private JLabel lblIcon;
	private ImageIcon tabCloseImgIcon;
	private JTabbedPane parent;
	private ChatWindow owner;
	private volatile Blinker blinker;

	public ChatWindowTab(String title, JTabbedPane parent, ChatWindow owner){
		// JPanel f�r Tabbeschriftung erzeugen und durchsichtig machen:
		this.parent=parent;
		this.owner=owner;
		((FlowLayout) this.getLayout()).setHgap(5);
		this.setOpaque(false);

		// TitelLabel f�r Tabbeschriftung erzeugen:
		lblTitle = new JLabel(title);
		// MouseListener zu JLabel (lblTitle) hinzuf�gen:
		lblTitle.addMouseListener(this);

		// ImageIcon f�r Schlie�enLabel erstellen:
		tabCloseImgIcon = new ImageIcon(getClass().getResource("TabCloseBlack.png"));
		// Schlie�enLabel f�r Tabbeschriftung erzeugen und gestalten:
		lblClose = new JLabel(tabCloseImgIcon);
		// Observer f�r das Image auf das lblClose setzen:
		tabCloseImgIcon.setImageObserver(lblClose);
		// MouseListener f�r Schlie�enlabel (lblClose) hinzuf�gen:
		lblClose.addMouseListener(this);

		lblIcon = new JLabel();
		if(owner.isPrivate()){
			lblIcon.setIcon(new ImageIcon(getClass().getResource("private.png")));
		} else {
			lblIcon.setIcon(new ImageIcon(getClass().getResource("gruppe.png")));
		}
		
		// TitelLabel (lblTitle) + Schlie�enLabel (btnClose) zum Tab (pnlTab) hinzuf�gen:
		this.add(lblIcon);
		this.add(lblTitle);
		this.add(lblClose);

		// den neuen Tab an die Stelle von index setzen:
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == lblTitle) {
			if (e.getModifiersEx() == 512) {
				GUI.getGUI().delChat(owner);
			} else {
				this.parent.setSelectedComponent(owner);
				stopBlink();
			}
		} else {
			GUI.getGUI().delChat(owner);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (e.getSource() == lblClose){
			tabCloseImgIcon.setImage(new ImageIcon(getClass().getResource("TabCloseOrange.png")).getImage());
		} else {
			JLabel source = (JLabel) e.getSource();
			source.setForeground(new Color(255, 130, 13));
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (e.getSource() == lblClose){
			tabCloseImgIcon.setImage(new ImageIcon(getClass().getResource("TabCloseBlack.png")).getImage());
		} else {
			JLabel source = (JLabel) e.getSource();
			source.setForeground(Color.BLACK);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	}

	private void blink() {
		if (lblTitle.getForeground() == Color.BLACK){
			lblTitle.setForeground(new Color(255, 130, 13));
		} else {
			lblTitle.setForeground(Color.BLACK);
		}
		if (parent.indexOfComponent(owner) == parent.getSelectedIndex()){
			this.stopBlink();
		}
	}

	public synchronized void startBlink() {
		if(blinker==null){
			blinker=new Blinker(500);
			blinker.start();
		}
	}
	
	public void stopBlink(){
		if(blinker!=null){
			blinker.stopit();
			blinker=null;
			lblTitle.setForeground(Color.BLACK);
		}
	}
	
	class Blinker extends Thread {
		int delay;
		volatile boolean active;
		
		public Blinker(int delay) {
			this.delay = delay;
			active = false;
		}
		
		@Override
		public void run() {
			active = true;
			while (active) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
				}
				blink();
			}
		}
		
		public void stopit() {
			active = false;
		}
		
	}
}
	
	

