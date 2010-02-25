package mobac.gui.panels;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import mobac.data.gpx.gpx11.RteType;
import mobac.data.gpx.gpx11.TrkType;
import mobac.data.gpx.gpx11.TrksegType;
import mobac.data.gpx.gpx11.WptType;
import mobac.gui.actions.GpxAddPoint;
import mobac.gui.actions.GpxClear;
import mobac.gui.actions.GpxLoad;
import mobac.gui.actions.GpxNew;
import mobac.gui.actions.GpxSave;
import mobac.gui.components.GpxEntry;
import mobac.gui.components.GpxRootEntry;
import mobac.gui.components.JCollapsiblePanel;
import mobac.gui.components.RteEntry;
import mobac.gui.components.TrkEntry;
import mobac.gui.components.TrksegEntry;
import mobac.gui.components.WptEntry;
import mobac.gui.mapview.GpxLayer;
import mobac.gui.mapview.PreviewMap;
import mobac.utilities.GBC;

public class JGpxPanel extends JCollapsiblePanel {

	private static final long serialVersionUID = 1L;

	private JTree tree;
	private DefaultMutableTreeNode rootNode;
	private DefaultTreeModel model;
	private ArrayList<String> openedFiles;

	private PreviewMap previewMap;

	public JGpxPanel(PreviewMap previewMap) {
		super("Gpx", new GridBagLayout());

		this.previewMap = previewMap;

		JButton newGpx = new JButton("New Gpx");
		newGpx.addActionListener(new GpxNew(this));

		JButton loadGpx = new JButton("Load Gpx");
		loadGpx.addActionListener(new GpxLoad(this));

		JButton saveGpx = new JButton("Save Gpx");
		saveGpx.addActionListener(new GpxSave(this));

		JButton clearGpx = new JButton("Clear List");
		clearGpx.addActionListener(new GpxClear(this));

		JButton addPointGpx = new JButton("Add wpt");
		addPointGpx.addActionListener(new GpxAddPoint(this));

		rootNode = new DefaultMutableTreeNode("loaded gpx files...");
		tree = new JTree(rootNode);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		JScrollPane treeView = new JScrollPane(tree);
		treeView.setPreferredSize(new Dimension(100, 100));
		model = (DefaultTreeModel) tree.getModel();

		openedFiles = new ArrayList<String>();
		
		GBC eol = GBC.eol().fill(GBC.HORIZONTAL);
		GBC std = GBC.std().fill(GBC.HORIZONTAL);
		addContent(treeView, eol);
		addContent(clearGpx, std);
		addContent(addPointGpx, eol);
		addContent(newGpx, std);
		addContent(loadGpx, std);
		addContent(saveGpx, eol);
	}

	/**
	 * adds a layer for a new gpx file on the map and adds its structure to the
	 * treeview
	 * 
	 */
	public GpxRootEntry addGpxLayer(GpxLayer layer) {
		layer.setPanel(this);
		GpxRootEntry gpxEntry = new GpxRootEntry(layer);
		DefaultMutableTreeNode gpxNode = new DefaultMutableTreeNode(gpxEntry);
		model.insertNodeInto(gpxNode, rootNode, rootNode.getChildCount());
		TreePath path = new TreePath(gpxNode.getPath());
		tree.scrollPathToVisible(new TreePath(path));
		tree.setSelectionPath(path);

		addRtes(layer, gpxNode);
		addTrks(layer, gpxNode);
		addWpts(layer, gpxNode);

		openedFiles.add(layer.getFile().getAbsolutePath());
		
		previewMap.mapLayers.add(layer);
		return gpxEntry;
	}

	/**
	 * @param layer
	 * @param gpxNode
	 * @param model
	 */
	private void addWpts(GpxLayer layer, DefaultMutableTreeNode gpxNode) {
		List<WptType> wpts = layer.getGpx().getWpt();
		for (WptType wpt : wpts) {
			WptEntry wptEntry = new WptEntry(wpt, layer);
			DefaultMutableTreeNode wptNode = new DefaultMutableTreeNode(wptEntry);
			model.insertNodeInto(wptNode, gpxNode, gpxNode.getChildCount());
		}
	}

	/**
	 * @param layer
	 * @param gpxNode
	 * @param model
	 */
	private void addTrks(GpxLayer layer, DefaultMutableTreeNode gpxNode) {
		// tracks
		List<TrkType> trks = layer.getGpx().getTrk();
		for (TrkType trk : trks) {
			TrkEntry trkEntry = new TrkEntry(trk, layer);
			DefaultMutableTreeNode trkNode = new DefaultMutableTreeNode(trkEntry);
			model.insertNodeInto(trkNode, gpxNode, gpxNode.getChildCount());
			// trkseg
			List<TrksegType> trksegs = trk.getTrkseg();
			int counter = 1;
			for (TrksegType trkseg : trksegs) {
				TrksegEntry trksegEntry = new TrksegEntry(trkseg, counter, layer);
				DefaultMutableTreeNode trksegNode = new DefaultMutableTreeNode(trksegEntry);
				model.insertNodeInto(trksegNode, trkNode, trkNode.getChildCount());
				counter++;

				// add trkpts
				List<WptType> trkpts = trkseg.getTrkpt();
				for (WptType trkpt : trkpts) {
					WptEntry trkptEntry = new WptEntry(trkpt, layer);
					DefaultMutableTreeNode trkptNode = new DefaultMutableTreeNode(trkptEntry);
					model.insertNodeInto(trkptNode, trksegNode, trksegNode.getChildCount());
				}
			}
		}
	}

	/**
	 * adds routes and route points to the tree view
	 * 
	 * @param layer
	 * @param gpxNode
	 */
	private void addRtes(GpxLayer layer, DefaultMutableTreeNode gpxNode) {
		List<RteType> rtes = layer.getGpx().getRte();
		for (RteType rte : rtes) {
			RteEntry rteEntry = new RteEntry(rte, layer);
			DefaultMutableTreeNode rteNode = new DefaultMutableTreeNode(rteEntry);
			model.insertNodeInto(rteNode, gpxNode, gpxNode.getChildCount());
			// add rtepts
			List<WptType> rtepts = rte.getRtept();
			for (WptType rtept : rtepts) {
				WptEntry rteptEntry = new WptEntry(rtept, layer);
				DefaultMutableTreeNode rteptNode = new DefaultMutableTreeNode(rteptEntry);
				model.insertNodeInto(rteptNode, rteNode, rteNode.getChildCount());
			}
		}
	}

	/**
	 * Updates the tree view to show the newly added waypoint.
	 * 
	 * @param wpt
	 *            - new waypoint
	 * @param gpxEntry
	 *            - parent entry in the tree
	 */
	public void addWpt(WptType wpt, GpxEntry gpxEntry) {
		WptEntry wptEntry = new WptEntry(wpt, gpxEntry.getLayer());
		DefaultMutableTreeNode wptNode = new DefaultMutableTreeNode(wptEntry);
		model.insertNodeInto(wptNode, gpxEntry.getNode(), gpxEntry.getNode().getChildCount());
	}

	public GpxEntry getSelectedEntry() {
		TreePath selection = tree.getSelectionPath();
		if (selection == null) {
			return null;
		}
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selection
				.getLastPathComponent();

		GpxEntry gpxEntry = null;
		try {
			gpxEntry = (GpxEntry) selectedNode.getUserObject();
			gpxEntry.setNode(selectedNode);
		} catch (ClassCastException e) {
		}
		return gpxEntry;
	}

	public boolean isFileOpen(String path) {
		return openedFiles.contains(path);
	}
	
	/**
	 * Resets the tree view. Used by GpxClear.
	 * 
	 */
	public void resetModel() {
		rootNode = new DefaultMutableTreeNode("loaded gpx files...");
		model.setRoot(rootNode);
		openedFiles = new ArrayList<String>();
	}
}
