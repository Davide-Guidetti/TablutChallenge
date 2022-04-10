package it.unibo.ai.didattica.competition.tablut.tester;

import javax.swing.WindowConstants;

public class TestGui {
	private TestGuiFrame frame;

	public TestGui(int game) {
		super();
		frame = new TestGuiFrame(game);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(600, 195);
		frame.setTitle("Tester");
		frame.setVisible(true);
	}


}
