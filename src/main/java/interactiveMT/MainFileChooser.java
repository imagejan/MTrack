package interactiveMT;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import ij.ImagePlus;
import ij.io.Opener;
import listeners.FireTrigger;
import listeners.FirepreTrigger;
import listeners.SelfFirepreTrigger;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import preProcessing.FlatFieldCorrection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.*;

public class MainFileChooser extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean wasDone = false;
	boolean isFinished = false;
	JButton Track;
	JButton Measure;
	JButton Measurebatch;
	JButton Kymo;
	JButton Done;
	public int selectedindex;
	File[] AllMovies;
	public JFileChooser chooserA;
	public String choosertitleA;
	public RandomAccessibleInterval<FloatType> originalimg;
	public RandomAccessibleInterval<FloatType> originalPreprocessedimg;
	public RandomAccessibleInterval<FloatType> ProgramPreprocessedimg;
	public JFileChooser chooserB;
	public String choosertitleB;
	public double[] calibration = new double[2];
	float frametosec;
	public JProgressBar jpb;
	JFileChooser chooserC;
	String choosertitleC;
	public double[] psf = new double[2];
	private JLabel inputLabelX, inputLabelY, inputLabelT;
	public JTextField inputFieldX;
	public JTextField inputFieldY;
	private JTextField inputFieldT;
	JPanel panelCont = new JPanel();
	public JPanel panelIntro = new JPanel();

	boolean loadpre = true;
	public boolean Simplemode = true;
	boolean Advancedmode = false;
	boolean Kymomode = false;
	boolean Loadpreimage = false;
	boolean Generatepre = false;
	boolean Batchmoderun = false;

	public FloatType minval = new FloatType(0);
	public FloatType maxval = new FloatType(1);
	private static final Insets insets = new Insets(10, 0, 0, 0);

	private JPanel Modechoice = new JPanel();
	private JPanel Microscope = new JPanel();
	private JPanel Start = new JPanel();
	public JFrame frame = new JFrame("Welcome to MTV Tracker ");

	/* Instantiation */
	public GridBagLayout layout = new GridBagLayout();
	public GridBagConstraints c = new GridBagConstraints();

	public MainFileChooser() {

		
		jpb = new JProgressBar();
		
		panelCont.add(panelIntro, "1");

		c.insets = new Insets(5, 5, 5, 5);

		panelIntro.setLayout(layout);
		Modechoice.setLayout(layout);
		Microscope.setLayout(layout);
		Start.setLayout(layout);

		CheckboxGroup mode = new CheckboxGroup();

		final Checkbox Batchmode = new Checkbox("Run in Batch Mode", mode, Batchmoderun);
		final Checkbox Simple = new Checkbox("Run in Simple mode ", mode, Simplemode);
		final Checkbox Advanced = new Checkbox("Run in Advanced mode ", mode, Advancedmode);

		final Label LoadtrackText = new Label(
				"Pre-processed movies ease object recognition, load yours else let MTV tracker do it");
		final Label LoadMeasureText = new Label("Choose image format and load : ");

		final Checkbox loadrun = new Checkbox("Load Pre-processed movie and begin tracking module", Loadpreimage);
		final Checkbox justrun = new Checkbox("Generate Pre-processed movie and begin tracking module", Generatepre);

		Border border = new CompoundBorder(new TitledBorder("Program run modes"), new EmptyBorder(c.insets));
		Border microborder = new CompoundBorder(new TitledBorder("Microscope Parameters"), new EmptyBorder(c.insets));
		Border runborder = new CompoundBorder(new TitledBorder("Preprocessing Options (Select only one)"), new EmptyBorder(c.insets));

		LoadtrackText.setBackground(new Color(1, 0, 1));
		LoadtrackText.setForeground(new Color(255, 255, 255));

		LoadMeasureText.setBackground(new Color(1, 0, 1));
		LoadMeasureText.setForeground(new Color(255, 255, 255));

		Track = new JButton("Load pre-processed movie");
		Measure = new JButton("Open Un-preprocessed movie");
		Kymo = new JButton("Open Kymograph for the MT");
		Done = new JButton("Done");
		inputLabelX = new JLabel("Enter Sigma (X and Y) of PSF (in pixels): ");
		inputFieldX = new JTextField(5);

		inputFieldX.setText("2");

		inputLabelY = new JLabel("Enter SigmaY of PSF (px): ");
		inputFieldY = new JTextField(5);
		inputFieldY.setText("2");

		String[] Imagetype = { "Two channel image as hyperstack", "Concated seed image followed by time-lapse images",
				"Single channel time-lapse images" };
		JComboBox<String> ChooseImage = new JComboBox<String>(Imagetype);

		// inputLabelT = new JLabel("Enter time frame to second conversion: ");
		// inputFieldT = new TextField();
		// inputFieldT.setColumns(2);

		/* Location */

		c.anchor = GridBagConstraints.CENTER;

		c.weightx = 0;
		c.weighty = 0;

		c.gridy = 1;
		c.gridx = 0;

		Modechoice.add(Batchmode, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		Modechoice.add(Simple, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));
		Modechoice.add(Advanced, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Modechoice.setBorder(border);
		panelIntro.add(Modechoice);

		Microscope.add(inputLabelX, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.NORTH,
				GridBagConstraints.RELATIVE, insets, 0, 0));
		Microscope.add(inputFieldX, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.RELATIVE, insets, 0, 0));

		Microscope.add(inputFieldY, new GridBagConstraints(1, 1, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.RELATIVE, insets, 0, 0));

		Microscope.setBorder(microborder);

		panelIntro.add(Microscope);

		panelIntro.add(LoadMeasureText, new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		panelIntro.add(ChooseImage, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		panelIntro.add(LoadtrackText, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Start.add(loadrun, new GridBagConstraints(0, 7, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Start.add(justrun, new GridBagConstraints(0, 8, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		Start.setBorder(runborder);

		panelIntro.add(Start, new GridBagConstraints(0, 9, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 10), 0, 0));

		panelIntro.setVisible(true);
		loadrun.addItemListener(new FirepreTrigger(this));
		justrun.addItemListener(new SelfFirepreTrigger(this));
		Batchmode.addItemListener(new RuninBatchListener(frame));
		Simple.addItemListener(new RunSimpleListener());
		Advanced.addItemListener(new RunAdvancedListener());
		ChooseImage.addActionListener(new FireTrigger(this, ChooseImage));
		frame.addWindowListener(new FrameListener(frame));
		frame.add(panelCont, BorderLayout.CENTER);
		frame.add(jpb, BorderLayout.PAGE_END);
		frame.pack();

		frame.setVisible(true);
	}

	protected class RuninBatchListener implements ItemListener {

		final Frame parent;

		public RuninBatchListener(Frame parent) {

			this.parent = parent;

		}

		@Override
		public void itemStateChanged(ItemEvent e) {

			close(parent);

			panelIntro.removeAll();

			/* Instantiation */
			final GridBagLayout layout = new GridBagLayout();
			final GridBagConstraints c = new GridBagConstraints();

			panelIntro.setLayout(layout);

			final JFrame frame = new JFrame("Welcome to MTV Tracker (Batch Mode)");
			Batchmoderun = true;
			Kymomode = false;
			Simplemode = true;
			Done = new JButton("Start batch processing");

			final Label LoadDirectoryText = new Label("Using Fiji Prefs we execute the program for all tif files");

			LoadDirectoryText.setBackground(new Color(1, 0, 1));
			LoadDirectoryText.setForeground(new Color(255, 255, 255));

			Measurebatch = new JButton("Select directory of tif files to process");

			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1;
			c.weighty = 1.5;

			++c.gridy;
			c.insets = new Insets(10, 10, 10, 0);
			panelIntro.add(LoadDirectoryText, c);

			++c.gridy;
			c.insets = new Insets(10, 10, 10, 0);
			panelIntro.add(Measurebatch, c);

			Measurebatch.addActionListener(new MeasurebatchListener(frame));
			Done.addActionListener(new DoneButtonListener(frame, true));
			panelIntro.revalidate();
			panelIntro.repaint();
			frame.addWindowListener(new FrameListener(frame));
			frame.add(panelCont, BorderLayout.CENTER);

			frame.setSize(getPreferredSizeSmall());
			frame.setVisible(true);

		}

	}

	protected class MeasurebatchListener implements ActionListener {

		final Frame parent;

		public MeasurebatchListener(Frame parent) {

			this.parent = parent;

		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			chooserB = new JFileChooser();
			if (chooserA != null)
				chooserB.setCurrentDirectory(chooserA.getCurrentDirectory());
			else
				chooserB.setCurrentDirectory(new java.io.File("."));
			chooserB.setDialogTitle(choosertitleB);
			chooserB.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

			chooserB.showOpenDialog(parent);

			AllMovies = chooserB.getSelectedFile().listFiles();

			new BatchMode(AllMovies, new Interactive_MTDoubleChannel(), AllMovies[0]).run(null);
		}

	}

	protected class FrameListener extends WindowAdapter {
		final Frame parent;

		public FrameListener(Frame parent) {
			super();
			this.parent = parent;
		}

		@Override
		public void windowClosing(WindowEvent e) {
			close(parent);
		}
	}

	public RandomAccessibleInterval<FloatType> Preprocess(RandomAccessibleInterval<FloatType> originalimg) {

		final FlatFieldCorrection flatfilter = new FlatFieldCorrection(originalimg, 1);
		flatfilter.process();
		RandomAccessibleInterval<FloatType> ProgramPreprocessedimg = flatfilter.getResult();
		return ProgramPreprocessedimg;

	}

	public RandomAccessibleInterval<FloatType> Preprocess(IntervalView<FloatType> originalimg) {

		final FlatFieldCorrection flatfilter = new FlatFieldCorrection(originalimg, 1);
		flatfilter.process();
		RandomAccessibleInterval<FloatType> ProgramPreprocessedimg = flatfilter.getResult();
		return ProgramPreprocessedimg;

	}

	protected class RunSimpleListener implements ItemListener {

		@Override
		public void itemStateChanged(final ItemEvent arg0) {

			if (arg0.getStateChange() == ItemEvent.DESELECTED) {
				Simplemode = false;
				Advancedmode = true;
			} else if (arg0.getStateChange() == ItemEvent.SELECTED) {
				Simplemode = true;
				Advancedmode = false;

			}

		}

	}

	protected class RunAdvancedListener implements ItemListener {

		@Override
		public void itemStateChanged(final ItemEvent arg0) {

			if (arg0.getStateChange() == ItemEvent.DESELECTED) {
				Advancedmode = false;
				Simplemode = true;
			} else if (arg0.getStateChange() == ItemEvent.SELECTED) {
				Advancedmode = true;
				Simplemode = false;
			}

		}

	}

	protected class OpenTrackListener implements ActionListener {

		final Frame parent;

		public OpenTrackListener(Frame parent) {

			this.parent = parent;

		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			chooserA = new JFileChooser();
			if (chooserB != null)
				chooserA.setCurrentDirectory(chooserB.getCurrentDirectory());
			else
				chooserA.setCurrentDirectory(new java.io.File("."));
			chooserA.setDialogTitle(choosertitleA);
			chooserA.setFileSelectionMode(JFileChooser.FILES_ONLY);

			if (chooserA.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
				System.out.println("getCurrentDirectory(): " + chooserA.getCurrentDirectory());
				System.out.println("getSelectedFile() : " + chooserA.getSelectedFile());
			} else {
				System.out.println("No Selection ");
				chooserA = null;
			}

		}

	}

	protected class MeasureListener implements ActionListener {

		final Frame parent;

		public MeasureListener(Frame parent) {

			this.parent = parent;

		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			chooserB = new JFileChooser();
			if (chooserA != null)
				chooserB.setCurrentDirectory(chooserA.getCurrentDirectory());
			else
				chooserB.setCurrentDirectory(new java.io.File("."));
			chooserB.setDialogTitle(choosertitleB);
			chooserB.setFileSelectionMode(JFileChooser.FILES_ONLY);

			if (chooserB.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
				System.out.println("getCurrentDirectory(): " + chooserB.getCurrentDirectory());
				System.out.println("getSelectedFile() : " + chooserB.getSelectedFile());
			} else {
				System.out.println("No Selection ");
				chooserB = null;
			}

		}

	}

	protected class DoneButtonListener implements ActionListener {
		final Frame parent;
		final boolean Done;

		public DoneButtonListener(Frame parent, final boolean Done) {
			this.parent = parent;
			this.Done = Done;
		}

		@Override
		public void actionPerformed(final ActionEvent arg0) {

			wasDone = Done;

			if (Batchmoderun) {

				new BatchMode(AllMovies, new Interactive_MTDoubleChannel(), AllMovies[0]).run(null);

			} else {

				// Preprocessed image
				ImagePlus impA = null;
				if (chooserA != null) {

					impA = new Opener().openImage(chooserA.getSelectedFile().getPath());

				}
				// Actual image
				ImagePlus impB = new Opener().openImage(chooserB.getSelectedFile().getPath());

				if (impA != null)
					assert (impA.getDimensions() == impB.getDimensions());

				int nChannels = impB.getNChannels();
				int nSlices = impB.getStackSize();
				int nFrames = impB.getNFrames();

				// Stupid user did not know they had slices instead of frames
				if (nFrames == 1 && nSlices > 1) {

					switch (JOptionPane.showConfirmDialog(null,
							"It appears this image has 1 timepoint but " + nSlices + " slices.\n"
									+ "Do you want to swap Z and T?",
							"Z/T swapped?", JOptionPane.YES_NO_CANCEL_OPTION)) {

					case JOptionPane.YES_OPTION:
						impB.setDimensions(nChannels, nFrames, nSlices);
						if (impA != null)
							impA.setDimensions(nChannels, nFrames, nSlices);
						break;
					case JOptionPane.CANCEL_OPTION:
						return;

					}

				}

				// Tracking is done with imageA measurment is performed on
				// imageB
				calibration[0] = impB.getCalibration().pixelWidth;
				calibration[1] = impB.getCalibration().pixelHeight;
				psf[0] = Float.parseFloat(inputFieldX.getText());
				psf[1] = Float.parseFloat(inputFieldY.getText());
				new Normalize();

				RandomAccessibleInterval<FloatType> originalimg = ImageJFunctions.convertFloat(impB);

				final FloatType type = originalimg.randomAccess().get().createVariable();
				final ImgFactory<FloatType> factory = Util.getArrayOrCellImgFactory(originalimg, type);
				RandomAccessibleInterval<FloatType> originalPreprocessedimg = factory.create(originalimg, type);

				if (impA != null)
					originalPreprocessedimg = ImageJFunctions.convertFloat(impA);
				else
					originalPreprocessedimg = null;
				// Normalize image intnesity
				Normalize.normalize(Views.iterable(originalimg), minval, maxval);

				if (nChannels > 1) {

					switch (JOptionPane.showConfirmDialog(null,
							"Image has " + nChannels + "Channels, Is seed image in channel 1?\n", " ",
							JOptionPane.YES_NO_CANCEL_OPTION)) {

					case JOptionPane.YES_OPTION:
						// Do concetation
						RandomAccessibleInterval<FloatType> seedimgStack = Views.hyperSlice(originalimg, 2, 0);

						RandomAccessibleInterval<FloatType> dynamicimgStack = Views.hyperSlice(originalimg, 2, 1);

						long[] dim = { dynamicimgStack.dimension(0), dynamicimgStack.dimension(1),
								dynamicimgStack.dimension(2) };
						RandomAccessibleInterval<FloatType> totalimg = factory.create(dim, type);
						RandomAccessibleInterval<FloatType> pretotalimg = factory.create(dim, type);
						final long nz = dynamicimgStack.dimension(2);

						IntervalView<FloatType> slice = Views.hyperSlice(seedimgStack, 2, 0);
						IntervalView<FloatType> outputSlice = Views.hyperSlice(totalimg, 2, 0);

						processSlice(slice, outputSlice);
						for (long z = 1; z < nz; z++) {
							slice = Views.hyperSlice(dynamicimgStack, 2, z);
							outputSlice = Views.hyperSlice(totalimg, 2, z);

							processSlice(slice, outputSlice);
						}
						IntervalView<FloatType> preoutputSlice;
						if (originalPreprocessedimg != null) {

							RandomAccessibleInterval<FloatType> dynamicgpreimgStack = Views
									.hyperSlice(originalPreprocessedimg, 2, 1);

							preoutputSlice = Views.hyperSlice(pretotalimg, 2, 0);

							RandomAccessibleInterval<FloatType> seedimgpreStack = Views
									.hyperSlice(originalPreprocessedimg, 2, 0);

							IntervalView<FloatType> preslice = Views.hyperSlice(seedimgpreStack, 2, 0);

							processSlice(preslice, preoutputSlice);

							for (long z = 1; z < nz; z++) {

								preslice = Views.hyperSlice(dynamicgpreimgStack, 2, z);
								preoutputSlice = Views.hyperSlice(pretotalimg, 2, z);

								processSlice(preslice, preoutputSlice);

							}
							Normalize.normalize(Views.iterable(totalimg), minval, maxval);
							ImageJFunctions.show(pretotalimg).setTitle("Preprocessed Movie");
						}

						else {

							preoutputSlice = (IntervalView<FloatType>) Preprocess(outputSlice);
							Normalize.normalize(Views.iterable(pretotalimg), minval, maxval);
							ImageJFunctions.show(pretotalimg).setTitle("Preprocessed Movie");
						}

						Normalize.normalize(Views.iterable(totalimg), minval, maxval);

						if (Simplemode)
							new Interactive_MTDoubleChannelBasic(new Interactive_MTDoubleChannel(totalimg, pretotalimg,
									psf, calibration, chooserB.getSelectedFile())).run(null);
						else
							new Interactive_MTDoubleChannel(totalimg, pretotalimg, psf, calibration,
									chooserB.getSelectedFile()).run(null);

						break;

					case JOptionPane.NO_OPTION:
						// Do concetation
						seedimgStack = Views.hyperSlice(originalimg, 2, 1);

						dynamicimgStack = Views.hyperSlice(originalimg, 2, 0);

						long[] dimsec = { dynamicimgStack.dimension(0), dynamicimgStack.dimension(1),
								dynamicimgStack.dimension(2) };

						totalimg = factory.create(dimsec, type);
						pretotalimg = factory.create(dimsec, type);
						long nzsec = dynamicimgStack.dimension(2);

						slice = Views.hyperSlice(seedimgStack, 2, 0);
						outputSlice = Views.hyperSlice(totalimg, 2, 0);

						processSlice(slice, outputSlice);
						for (long z = 1; z < nzsec; z++) {
							slice = Views.hyperSlice(dynamicimgStack, 2, z);
							outputSlice = Views.hyperSlice(totalimg, 2, z);

							processSlice(slice, outputSlice);
						}
						if (originalPreprocessedimg != null) {

							RandomAccessibleInterval<FloatType> dynamicgpreimgStack = Views
									.hyperSlice(originalPreprocessedimg, 2, 0);

							preoutputSlice = Views.hyperSlice(pretotalimg, 2, 0);

							RandomAccessibleInterval<FloatType> seedimgpreStack = Views
									.hyperSlice(originalPreprocessedimg, 2, 1);

							IntervalView<FloatType> preslice = Views.hyperSlice(seedimgpreStack, 2, 0);

							processSlice(preslice, preoutputSlice);

							for (long z = 1; z < nzsec; z++) {

								preslice = Views.hyperSlice(dynamicgpreimgStack, 2, z);
								preoutputSlice = Views.hyperSlice(pretotalimg, 2, z);

								processSlice(preslice, preoutputSlice);

							}
							Normalize.normalize(Views.iterable(pretotalimg), minval, maxval);
							ImageJFunctions.show(pretotalimg).setTitle("Preprocessed Movie");
						}

						else {

							preoutputSlice = (IntervalView<FloatType>) Preprocess(outputSlice);
							Normalize.normalize(Views.iterable(pretotalimg), minval, maxval);
							ImageJFunctions.show(pretotalimg).setTitle("Preprocessed Movie");
						}

						Normalize.normalize(Views.iterable(totalimg), minval, maxval);

						if (Simplemode)
							new Interactive_MTDoubleChannelBasic(new Interactive_MTDoubleChannel(totalimg, pretotalimg,
									psf, calibration, chooserB.getSelectedFile())).run(null);
						else
							new Interactive_MTDoubleChannel(totalimg, pretotalimg, psf, calibration,
									chooserB.getSelectedFile()).run(null);

						break;

					case JOptionPane.CANCEL_OPTION:

						return;

					}
				}

				else {
					if (impA != null)
						originalPreprocessedimg = ImageJFunctions.convertFloat(impA);
					else

						originalPreprocessedimg = Preprocess(originalimg);
					Normalize.normalize(Views.iterable(originalPreprocessedimg), minval, maxval);
					switch (JOptionPane.showConfirmDialog(null, "Is this a double channel image?", "",
							JOptionPane.YES_NO_CANCEL_OPTION)) {

					case JOptionPane.YES_OPTION:
						// Put constructor for double channel
						ImageJFunctions.show(originalPreprocessedimg).setTitle("Preprocessed Movie");
						if (Simplemode)
							new Interactive_MTDoubleChannelBasic(new Interactive_MTDoubleChannel(originalimg,
									originalPreprocessedimg, psf, calibration, chooserB.getSelectedFile())).run(null);
						else
							new Interactive_MTDoubleChannel(originalimg, originalPreprocessedimg, psf, calibration,
									chooserB.getSelectedFile()).run(null);
						break;

					case JOptionPane.NO_OPTION:
						// Put constructor for single channel
						ImageJFunctions.show(originalPreprocessedimg).setTitle("Preprocessed Movie");
						if (Simplemode)

							new Interactive_MTSingleChannelBasic(new Interactive_MTSingleChannel(originalimg,
									originalPreprocessedimg, psf, calibration, chooserB.getSelectedFile())).run(null);
						else
							new Interactive_MTSingleChannel(originalimg, originalPreprocessedimg, psf, calibration,
									chooserB.getSelectedFile()).run(null);

						break;

					case JOptionPane.CANCEL_OPTION:

						return;

					}

				}

			}

			// frametosec = Float.parseFloat(inputFieldT.getText());

			close(parent);

		}
	}

	public void processSlice(RandomAccessibleInterval<FloatType> slice, IterableInterval<FloatType> outputSlice) {

		final Cursor<FloatType> cursor = outputSlice.localizingCursor();
		final RandomAccess<FloatType> ranac = slice.randomAccess();

		while (cursor.hasNext()) {

			cursor.fwd();

			ranac.setPosition(cursor);

			cursor.get().set(ranac.get());

		}

	}

	protected final void close(final Frame parent) {
		if (parent != null)
			parent.dispose();

		isFinished = true;
	}

	public Dimension getPreferredSize() {
		return new Dimension(800, 300);
	}

	public Dimension getPreferredSizeSmall() {
		return new Dimension(500, 200);
	}

}