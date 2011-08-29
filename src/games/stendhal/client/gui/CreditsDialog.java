/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

/**
 * Displays a credits dialog box.
 */
public class CreditsDialog extends JDialog {

	private static final long serialVersionUID = 4312205320503928411L;

	private static final Logger logger = Logger.getLogger(CreditsDialog.class);

	private final Frame owner;

	private ScrollerPanel sp;

	private final JPanel buttonPane = new JPanel();

	private final JButton closeButton = new JButton("Close");

	private final Color backgroundColor = Color.white;

	private final Font textFont = new Font("SansSerif", Font.BOLD, 12);

	private final Color textColor = new Color(85, 85, 85);

	/**
	 * Creates a new credits dialog.
	 * 
	 * @param owner
	 *            owner window
	 */
	public CreditsDialog(final Frame owner) {
		super(owner, true);
		this.owner = owner;
		initGUI();
		logger.debug("about dialog initialized");
		eventHandling();
		logger.debug("about dialog event handling ready");

		this.setTitle("Stendhal Credits");
		// required on Compiz
		this.pack(); 
		if (owner != null) {
			this.setLocation(owner.getX() + 25, owner.getY() + 25);
			this.setSize(owner.getSize());
		} else {
			this.setLocationByPlatform(true);
			this.setSize(600, 420);
		}
		WindowUtils.closeOnEscape(this);
		this.setVisible(true);
	}

	private void initGUI() {
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				owner.setEnabled(true);
				dispose();
			}
		});
		owner.setEnabled(false);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().setBackground(backgroundColor);

		// read the credits from an external file because code format gets it
		// unreadable if inlined
		final List<String> creditsList = readCredits();
		sp = new ScrollerPanel(creditsList, textFont, 0, textColor,
				backgroundColor, 20);

		buttonPane.setOpaque(false);
		buttonPane.add(closeButton);

		this.getContentPane().add(sp, BorderLayout.CENTER);
		this.getContentPane().add(buttonPane, BorderLayout.SOUTH);
	}

	/**
	 * Reads the credits from credits.text.
	 * 
	 * @return list of lines
	 */
	private List<String> readCredits() {
		final URL url = this.getClass().getClassLoader().getResource(
				"games/stendhal/client/gui/credits.txt");
		final List<String> res = new LinkedList<String>();
		try {
			final BufferedReader br = new BufferedReader(new InputStreamReader(
					url.openStream()));
			try {
				String line = br.readLine();
				while (line != null) {
					res.add(line);
					line = br.readLine();
				}
			} finally {
				br.close();
			}
		} catch (final IOException e) {
			res.add(0, "credits.txt not found");
		}
		return res;
	}

	/**
	 * Sets up the listeners an event handling.
	 */
	private void eventHandling() {
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				exit();
			}
		});
		closeButton.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent e) {
				exit();
			}
		});
	}

	/**
	 * Exits Credits Dialog.
	 */
	protected void exit() {
		sp.stop();
		this.setVisible(false);
		owner.setEnabled(true);
		this.dispose();
		logger.debug("about dialog closed");
	}

}
