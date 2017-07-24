package singleListeners;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;

import ij.IJ;
import ij.gui.ImageCanvas;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;

import interactiveMT.Interactive_MTSingleChannel;
import interactiveMT.Interactive_MTSingleChannel.ValueChange;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import updateListeners.SingleMarkends;

public  class SingleMoveNextListener implements ActionListener {
	
	
final Interactive_MTSingleChannel parent;
	
	
	public SingleMoveNextListener(final Interactive_MTSingleChannel parent){
	
		this.parent = parent;
	}
	@Override
	public void actionPerformed(final ActionEvent arg0) {

		if (parent.thirdDimension > parent.thirdDimensionSize) {
			IJ.log("Max frame number exceeded, moving to last frame instead");
			parent.thirdDimension = parent.thirdDimensionSize;
			parent.CurrentView = util.CopyUtils.getCurrentView(parent.originalimg, parent.thirdDimension, parent.thirdDimensionSize);
			parent.CurrentPreprocessedView = util.CopyUtils.getCurrentPreView(parent.originalPreprocessedimg, parent.thirdDimension,
					parent.thirdDimensionSize);
		} else {

			parent.thirdDimension = parent.thirdDimensionsliderInit + 1;
			parent.CurrentView = util.CopyUtils.getCurrentView(parent.originalimg, parent.thirdDimension, parent.thirdDimensionSize);
			parent.CurrentPreprocessedView = util.CopyUtils.getCurrentPreView(parent.originalPreprocessedimg, parent.thirdDimension,
					parent.thirdDimensionSize);

		}

		parent.updatePreview(ValueChange.THIRDDIM);

		
		
		
		SingleMarkends newends = new SingleMarkends(parent);
		newends.markend();
	

	}
	
	
	
}