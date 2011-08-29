/* $Id$ */
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

import games.stendhal.client.OutfitStore;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.Outfits;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import marauroa.common.game.RPAction;

import org.apache.log4j.Logger;

public class OutfitDialog extends JDialog {

	/** the logger instance. */
	private static final Logger LOGGER = Logger.getLogger(OutfitDialog.class);

	private static final long serialVersionUID = 4628210176721975735L;

	private static final int PLAYER_WIDTH = 48;

	private static final int PLAYER_HEIGHT = 64;

	// to keep the sprites to show
	private final Sprite[] hairs;

	private final Sprite[] heads;

	private final Sprite[] bodies;

	private final Sprite[] clothes;

	// current selected parts index
	private int hairsIndex = 1;

	private int headsIndex;

	private int bodiesIndex;

	private int clothesIndex;

	// to handle the draws update
	private final Timer timer;

	// 0 for direction UP, 1 RIGHT, 2 DOWN and 3 LEFT
	private int direction = 2;

	private final StendhalClient client;

	private final SpriteStore store = SpriteStore.get();

	private final OutfitStore ostore = OutfitStore.get();
	
	private JButton jbtLeftBodies;

	private JButton jbtLeftClothes;

	private JButton jbtLeftHairs;

	private JButton jbtLeftHeads;

	private JButton jbtOK;

	private JButton jbtRightBodies;

	private JButton jbtRightClothes;

	private JButton jbtRightHairs;

	private JButton jbtRightHeads;

	private JLabel jlblBodies;

	private JLabel jlblClothes;

	private JLabel jlblFinalResult;

	private JLabel jlblHairs;

	private JLabel jlblHeads;

	private JSlider jsliderDirection;

	public OutfitDialog(final Frame parent, final String title, final int outfit) {
		this(parent, title, outfit, Outfits.HAIR_OUTFITS, Outfits.HEAD_OUTFITS, Outfits.BODY_OUTFITS,
				Outfits.CLOTHES_OUTFITS);
	}

	/**
	 * Creates new form SetOutfitGameDialog.
	 * @param parent
	 *
	 * @param title
	 *            a String with the title for the dialog
	 * @param outfit
	 *            the current outfit
	 * @param total_hairs
	 *            an integer with the total of sprites with hairs
	 * @param total_heads
	 *            an integer with the total of sprites with heads
	 * @param total_bodies
	 *            an integer with the total of sprites with bodies
	 * @param total_clothes
	 *            an integer with the total of sprites with clothes
	 */
	private OutfitDialog(final Frame parent, final String title, int outfit,
			final int total_hairs, final int total_heads, final int total_bodies,
			final int total_clothes) {
		super(parent, false);
		initComponents();
		applyStyle();
		setTitle(title);

		client = StendhalClient.get();

		// initializes the arrays
		// Plus 1 to add the sprite_empty.png that is always at 0
		hairs = new Sprite[total_hairs];
		heads = new Sprite[total_heads];
		bodies = new Sprite[total_bodies];
		// Plus 1 to add the sprite_empty.png that is always at 0
		clothes = new Sprite[total_clothes];

		// updates the draws every 2500 milliseconds
		timer = new Timer();
		timer.schedule(new AnimationTask(), 1000, 2500);

		// analyse current outfit
		bodiesIndex = outfit % 100;
		outfit = outfit / 100;
		clothesIndex = outfit % 100;
		outfit = outfit / 100;
		headsIndex = outfit % 100;
		outfit = outfit / 100;
		hairsIndex = outfit % 100;

		// reset special outfits
		if (hairsIndex >= hairs.length) {
			hairsIndex = 0;
		}
		if (headsIndex >= heads.length) {
			headsIndex = 0;
		}
		if (bodiesIndex >= bodies.length) {
			bodiesIndex = 0;
		}
		if (clothesIndex >= clothes.length) {
			clothesIndex = 0;
		}
		WindowUtils.closeOnEscape(this);
	}

	/**
	 * Cleans the previous draw.
	 *
	 * @param g
	 *            the Graphics where to clean
	 */
	private void clean(final Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(2, 2, PLAYER_WIDTH, PLAYER_HEIGHT);
	}

	/**
	 * Redraws the hair image from an outfit code.
	 *
	 * @param code
	 *            The index code.
	 * @param g
	 *            The graphics context.
	 */
	private void redrawHair(final int code, final Graphics g) {
		clean(g);
		drawHair(code, g);
	}

	/**
	 * Draws a hair images from an outfit code.
	 * @param code
	 * @param g
	 */
	private void drawHair(final int code, final Graphics g) {
		final Sprite sprite = store.getTile(ostore.getHairSprite(code), PLAYER_WIDTH,
				direction * PLAYER_HEIGHT, PLAYER_WIDTH, PLAYER_HEIGHT);

		sprite.draw(g, 2, 2);
	}

	/**
	 * Redraws the head image from an outfit code.
	 *
	 * @param code
	 *            The index code.
	 * @param g
	 *            The graphics context.
	 */
	private void redrawHead(final int code, final Graphics g) {
		clean(g);
		drawHead(code, g);
	}

	/**
	 * Draws a head from the outfit code.
	 * @param code
	 * @param g
	 */
	private void drawHead(final int code, final Graphics g) {
		final Sprite sprite = store.getTile(ostore.getHeadSprite(code), PLAYER_WIDTH,
				direction * PLAYER_HEIGHT, PLAYER_WIDTH, PLAYER_HEIGHT);

		sprite.draw(g, 2, 2);
	}

	/**
	 * Redraws the hair image from an outfit code.
	 *
	 * @param code
	 *            The index code.
	 * @param g
	 *            The graphics context.
	 */
	private void redrawDress(final int code, final Graphics g) {
		clean(g);
		drawDress(code, g);
	}

	/**
	 * Draws a dress from the outfit code.
	 * @param code
	 * @param g
	 */
	private void drawDress(final int code, final Graphics g) {
		final Sprite sprite = store.getTile(ostore.getDressSprite(code),
				PLAYER_WIDTH, direction * PLAYER_HEIGHT, PLAYER_WIDTH,
				PLAYER_HEIGHT);

		sprite.draw(g, 2, 2);
	}

	/**
	 * Redraws the hair image from an outfit code.
	 *
	 * @param code
	 *            The index code.
	 * @param g
	 *            The graphics context.
	 */
	private void redrawBase(final int code, final Graphics g) {
		clean(g);
		drawBase(code, g);
	}

	/**
	 * Draws a base from an outfit code.
	 * @param code
	 * @param g
	 */
	private void drawBase(final int code, final Graphics g) {
		final Sprite sprite = store.getTile(ostore.getBaseSprite(code), PLAYER_WIDTH,
				direction * PLAYER_HEIGHT, PLAYER_WIDTH, PLAYER_HEIGHT);

		sprite.draw(g, 2, 2);
	}

	/**
	 * Redraw the final player.
	 *
	 * @param g
	 *            The graphics context.
	 */
	private void redrawFinalPlayer(final Graphics g) {
		clean(g);
		drawFinalPlayer(g);
	}

	/**
	 * Draws final player.
	 * @param g
	 */
	private void drawFinalPlayer(final Graphics g) {
		drawBase(bodiesIndex, g);
		drawDress(clothesIndex, g);
		drawHead(headsIndex, g);
		drawHair(hairsIndex, g);
	}

	private void initComponents() {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent evt) {
				formWindowClosing(evt);
			}
		});

		JComponent content = (JComponent) getContentPane();
		content.setLayout(null);

		jbtOK = new JButton("OK");
		jbtOK.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtOKActionPerformed(evt);
			}
		});

		content.add(jbtOK);
		jbtOK.setBounds(190, 220, 80, 30);

		
		jbtLeftHairs = new JButton("<");
		jbtLeftHairs.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtLeftHairsActionPerformed(evt);
			}
		});

		content.add(jbtLeftHairs);
		jbtLeftHairs.setBounds(10, 20, 45, 30);

		jbtRightHairs = new JButton(">");
		jbtRightHairs.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtRightHairsActionPerformed(evt);
			}
		});

		content.add(jbtRightHairs);
		jbtRightHairs.setBounds(120, 20, 45, 30);

		jbtLeftHeads = new JButton("<");
		jbtLeftHeads.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtLeftHeadsActionPerformed(evt);
			}
		});

		content.add(jbtLeftHeads);
		jbtLeftHeads.setBounds(10, 100, 45, 30);

		jbtRightHeads = new JButton(">");
		jbtRightHeads.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtRightHeadsActionPerformed(evt);
			}
		});

		content.add(jbtRightHeads);
		jbtRightHeads.setBounds(120, 100, 45, 30);

		jbtLeftBodies = new JButton("<");
		jbtLeftBodies.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtLeftBodiesActionPerformed(evt);
			}
		});

		content.add(jbtLeftBodies);
		jbtLeftBodies.setBounds(10, 180, 45, 30);

		jbtRightBodies = new JButton(">");
		jbtRightBodies.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtRightBodiesActionPerformed(evt);
			}
		});

		content.add(jbtRightBodies);
		jbtRightBodies.setBounds(120, 180, 45, 30);

		jbtLeftClothes = new JButton("<");
		jbtLeftClothes.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtLeftClothesActionPerformed(evt);
			}
		});

		content.add(jbtLeftClothes);
		jbtLeftClothes.setBounds(10, 260, 45, 30);

		jbtRightClothes = new JButton(">");
		jbtRightClothes.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent evt) {
				jbtRightClothesActionPerformed(evt);
			}
		});

		content.add(jbtRightClothes);
		jbtRightClothes.setBounds(120, 260, 45, 30);

		jlblHairs = new JLabel();
		jlblHairs.setFont(new Font("Dialog", 0, 10));
		jlblHairs.setHorizontalAlignment(SwingConstants.CENTER);
		jlblHairs.setText("loading...");
		jlblHairs.setOpaque(true);
		content.add(jlblHairs);
		jlblHairs.setBounds(60, 10, 52, 68);

		jlblHeads = new JLabel();
		jlblHeads.setFont(new Font("Dialog", 0, 10));
		jlblHeads.setHorizontalAlignment(SwingConstants.CENTER);
		jlblHeads.setText("loading...");
		jlblHeads.setOpaque(true);
		content.add(jlblHeads);
		jlblHeads.setBounds(60, 90, 52, 68);

		jlblBodies = new JLabel();
		jlblBodies.setFont(new Font("Dialog", 0, 10));
		jlblBodies.setHorizontalAlignment(SwingConstants.CENTER);
		jlblBodies.setText("loading...");
		jlblBodies.setOpaque(true);
		content.add(jlblBodies);
		jlblBodies.setBounds(60, 170, 52, 68);

		jlblClothes = new JLabel();
		jlblClothes.setFont(new Font("Dialog", 0, 10));
		jlblClothes.setHorizontalAlignment(SwingConstants.CENTER);
		jlblClothes.setText("loading...");
		jlblClothes.setOpaque(true);
		content.add(jlblClothes);
		jlblClothes.setBounds(60, 250, 52, 68);

		jlblFinalResult = new JLabel();
		jlblFinalResult.setFont(new Font("Dialog", 0, 10));
		jlblFinalResult.setHorizontalAlignment(SwingConstants.CENTER);
		jlblFinalResult.setText("loading...");
		jlblFinalResult.setOpaque(true);
		content.add(jlblFinalResult);
		jlblFinalResult.setBounds(205, 90, 52, 68);

		jsliderDirection = new JSlider();
		jsliderDirection.setMaximum(3);
		jsliderDirection.setSnapToTicks(true);
		jsliderDirection.setValue(2);
		jsliderDirection.setInverted(true);
		jsliderDirection.addChangeListener(new ChangeListener() {

			public void stateChanged(final ChangeEvent evt) {
				jsliderDirectionStateChanged(evt);
			}
		});

		content.add(jsliderDirection);
		jsliderDirection.setBounds(190, 170, 80, 27);

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 288) / 2, (screenSize.height - 361) / 2,
				288, 361);
	}



	/** this is called every time the user moves the slider.
	 * @param evt */
	private void jsliderDirectionStateChanged(final ChangeEvent evt) {
		direction = jsliderDirection.getValue();

		redrawFinalPlayer(jlblFinalResult.getGraphics());
		redrawHair(hairsIndex, jlblHairs.getGraphics());
		redrawHead(headsIndex, jlblHeads.getGraphics());
		redrawBase(bodiesIndex, jlblBodies.getGraphics());
		redrawDress(clothesIndex, jlblClothes.getGraphics());
	}

	/** when user closes this window.
	 * @param evt */
	private void formWindowClosing(final WindowEvent evt) {
		timer.cancel();
		this.dispose();
	}

	/** Clothes Right button.
	 * @param evt */
	private void jbtRightClothesActionPerformed(final ActionEvent evt) {
		if (clothesIndex < clothes.length - 1) {
			clothesIndex++;
		} else {
			clothesIndex = 0;
		}

		redrawDress(clothesIndex, jlblClothes.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	}

	/** Clothes Left button.
	 * @param evt */
	private void jbtLeftClothesActionPerformed(final ActionEvent evt) {
		if (clothesIndex > 0) {
			clothesIndex--;
		} else {
			clothesIndex = clothes.length - 1;
		}

		redrawDress(clothesIndex, jlblClothes.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	}

	/** Bodies Right button.
	 * @param evt */
	private void jbtRightBodiesActionPerformed(final ActionEvent evt) {
		if (bodiesIndex < bodies.length - 1) {
			bodiesIndex++;
		} else {
			bodiesIndex = 0;
		}

		redrawBase(bodiesIndex, jlblBodies.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	}

	/** Bodies Left button.
	 * @param evt */
	private void jbtLeftBodiesActionPerformed(final ActionEvent evt) { 
		if (bodiesIndex > 0) {
			bodiesIndex--;
		} else {
			bodiesIndex = bodies.length - 1;
		}

		redrawBase(bodiesIndex, jlblBodies.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	}

	/** Heads Right button.
	 * @param evt */
	private void jbtRightHeadsActionPerformed(final ActionEvent evt) { 
		if (headsIndex < heads.length - 1) {
			headsIndex++;
		} else {
			headsIndex = 0;
		}

		redrawHead(headsIndex, jlblHeads.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	}

	/** Heads Left button.
	 * @param evt */
	private void jbtLeftHeadsActionPerformed(final ActionEvent evt) { 
		if (headsIndex > 0) {
			headsIndex--;
		} else {
			headsIndex = heads.length - 1;
		}

		redrawHead(headsIndex, jlblHeads.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	} 

	/** Hairs Right button.
	 * @param evt */
	private void jbtRightHairsActionPerformed(final ActionEvent evt) { 
		if (hairsIndex < hairs.length - 1) {
			hairsIndex++;
		} else {
			hairsIndex = 0;
		}

		redrawHair(hairsIndex, jlblHairs.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	} 

	/** Hairs Left button.
	 * @param evt */
	private void jbtLeftHairsActionPerformed(final ActionEvent evt) { 
		if (hairsIndex > 0) {
			hairsIndex--;
		} else {
			hairsIndex = hairs.length - 1;
		}

		redrawHair(hairsIndex, jlblHairs.getGraphics());
		redrawFinalPlayer(jlblFinalResult.getGraphics());
	}

	/** Button OK action.
	 * @param evt */
	private void jbtOKActionPerformed(final ActionEvent evt) { 
		sendAction();

		timer.cancel();
		this.dispose();
	} 

	private void sendAction() {
		if (client == null) {
			/** If running standalone, just print the outfit */
			System.out.println("OUTFIT is: "
					+ (bodiesIndex + clothesIndex * 100 + headsIndex * 100
							* 100 + hairsIndex * 100 * 100 * 100));
			return;
		}

		final RPAction rpaction = new RPAction();
		rpaction.put("type", "outfit");
		rpaction.put("value", bodiesIndex + clothesIndex * 100 + headsIndex
				* 100 * 100 + hairsIndex * 100 * 100 * 100);
		client.send(rpaction);
	}



	/**
	 * Private class that handles the update (repaint) of jLabels.
	 */
	private class AnimationTask extends TimerTask {

		@Override
		public void run() {
			// draws single parts
			redrawHair(hairsIndex, jlblHairs.getGraphics());
			redrawHead(headsIndex, jlblHeads.getGraphics());
			redrawBase(bodiesIndex, jlblBodies.getGraphics());
			redrawDress(clothesIndex, jlblClothes.getGraphics());

			redrawFinalPlayer(jlblFinalResult.getGraphics());
		}
	}


	private void generateAllOutfits(final String baseDir) {
		/** TEST METHOD: DON'T NO USE */
		for (bodiesIndex = 0; bodiesIndex < bodies.length; bodiesIndex++) {
			for (clothesIndex = 0; clothesIndex < clothes.length; clothesIndex++) {
				for (headsIndex = 0; headsIndex < heads.length; headsIndex++) {
					for (hairsIndex = 0; hairsIndex < hairs.length; hairsIndex++) {
						final String name = Integer.toString(bodiesIndex
								+ clothesIndex * 100 + headsIndex * 100 * 100
								+ hairsIndex * 100 * 100 * 100);
						final File file = new File(baseDir + "outfits/" + name
								+ ".png");

						// for performance reasons only write new files.
						if (!file.exists()) {
							System.out.println("Creating " + name + ".png");
							final Image image = new BufferedImage(PLAYER_WIDTH,
									PLAYER_HEIGHT, BufferedImage.TYPE_INT_ARGB);
							drawFinalPlayer(getGraphics());
							try {
								ImageIO.write((RenderedImage) image, "png",
										file);
							} catch (final Exception e) {
								LOGGER.error(e, e);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Apply Stendhal style to all components.
	 */
	private void applyStyle() {
		Style style = StyleUtil.getStyle();
		if (style != null) {
			// Labels (Images). Making all JLabels bordered would be undesired
			jlblBodies.setBorder(style.getBorderDown());
			jlblClothes.setBorder(style.getBorderDown());
			jlblFinalResult.setBorder(style.getBorderDown());
			jlblHairs.setBorder(style.getBorderDown());
			jlblHeads.setBorder(style.getBorderDown());
		}
	}

	public static void main(final String[] args) {
		String baseDir = "";
		if (args.length > 0) {
			baseDir = args[0] + "/";
		}

		final OutfitDialog f = new OutfitDialog(null, "Stendhal - Choose outfit", 0);
		// show is required now, because getGraphics() returns null otherwise
		f.setVisible(true);
		f.generateAllOutfits(baseDir);
	}
}
