package app.treedrawer;

import tree.BST;
import tree.BSTNode;
import tree.RBTNode;
import util.FontLoader;
import values.NumberOrString;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;

public abstract class TreeDrawer {
	public static Font font = FontLoader.load("JBMono.ttf").deriveFont(12f);
	private static final Graphics dummyGraphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).getGraphics();

	public static int getNodePadding () {
		return font.getSize() / 2;
	}

	protected static Dimension getRenderedSize (String text) {
		Rectangle2D size = dummyGraphics.getFontMetrics(font).getStringBounds(text, dummyGraphics);
		return new Dimension((int) size.getWidth(), (int) size.getHeight());
	}

	protected abstract <T extends Comparable<T>> int[][] calculatePositions (BSTNode<T>[][] levels, int height, int windowWidth, Graphics2D graphics);

	public final <T extends Comparable<T>> void drawTree (BST<T> bst, int windowWidth, Graphics2D graphics) {
		if (bst == null || bst.getRoot() == null) return;

		final int height = bst.countLevels();
		final BSTNode<T>[][] levels = new BSTNode[height][];
		levels[0] = new BSTNode[] { bst.getRoot() };

		for (int h = 1; h < height; h++) {
			int size = 1 << h;
			levels[h] = new BSTNode[size];
			for (int i = 0; i * 2 < size; i++) {
				var n = levels[h - 1][i];
				if (n != null) {
					levels[h][i * 2] = n.getLeftChild();
					levels[h][i * 2 + 1] = n.getRightChild();
				}
			}
		}

		int[][] x = calculatePositions(levels, height, windowWidth, graphics);
		int[][] y = new int[height][];
		for (int h = 0; h < height; h++) {
			y[h] = new int[1 << h];
			Arrays.fill(y[h],
				(h * 6 + 1) * getNodePadding() + 40 + getRenderedSize("|").height / 2
			);
		}

		graphics.setFont(font);

		for (int h = height - 1; h >= 0; h--) {
			for (int i = 0; i < levels[h].length; i++) {
				var node = levels[h][i];

				if (node != null) {
					String text = node.getValue().toString();
					var d = getRenderedSize(text);
					int X = x[h][i], Y = y[h][i];

					var fm = graphics.getFontMetrics();

					graphics.setColor(node instanceof RBTNode ? Color.BLUE : Color.BLACK);
					if (node.getParent() != null && h > 0) {
						graphics.drawLine(X, Y, x[h - 1][i / 2], y[h - 1][i / 2]);
					}

					graphics.setColor(Color.WHITE);
					graphics.fillRect(X - d.width / 2 - getNodePadding(), Y - d.height / 2 - getNodePadding(), d.width + getNodePadding() * 2, d.height + getNodePadding() * 2);

					graphics.setColor(node instanceof RBTNode && RBTNode.getColor((RBTNode<T>) node) == RBTNode.Color.RED ? Color.RED : Color.BLACK);
					graphics.drawRect(X - d.width / 2 - getNodePadding(), Y - d.height / 2 - getNodePadding(), d.width + getNodePadding() * 2, d.height + getNodePadding() * 2);
					if (node.getValue() instanceof NumberOrString && ((NumberOrString) node.getValue()).isString()) {
						graphics.setColor(Color.GREEN.darker());
					}
					graphics.drawString(text, X - d.width / 2, Y - fm.getHeight() / 2 + fm.getAscent());
				}
			}
		}
	}
}

