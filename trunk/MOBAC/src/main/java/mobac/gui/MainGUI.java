/*******************************************************************************
 * Copyright (c) MOBAC developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mobac.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.xml.bind.JAXBException;

import mobac.exceptions.AtlasTestException;
import mobac.exceptions.InvalidNameException;
import mobac.gui.actions.ShowHelpAction;
import mobac.gui.atlastree.JAtlasTree;
import mobac.gui.components.FilledLayeredPane;
import mobac.gui.components.JAtlasNameField;
import mobac.gui.components.JCollapsiblePanel;
import mobac.gui.mapview.GridZoom;
import mobac.gui.mapview.MapEventListener;
import mobac.gui.mapview.PreviewMap;
import mobac.gui.panels.JCoordinatesPanel;
import mobac.gui.panels.JGpxPanel;
import mobac.gui.panels.JProfilesPanel;
import mobac.gui.panels.JTileImageParametersPanel;
import mobac.gui.panels.JTileStoreCoveragePanel;
import mobac.mapsources.MapSourcesManager;
import mobac.mapsources.MapSourcesUpdater;
import mobac.program.AtlasThread;
import mobac.program.ProgramInfo;
import mobac.program.interfaces.AtlasInterface;
import mobac.program.model.AtlasOutputFormat;
import mobac.program.model.Layer;
import mobac.program.model.MapSelection;
import mobac.program.model.MercatorPixelCoordinate;
import mobac.program.model.Profile;
import mobac.program.model.SelectedZoomLevels;
import mobac.program.model.Settings;
import mobac.program.model.TileImageParameters;
import mobac.program.tilestore.TileStore;
import mobac.utilities.GBC;
import mobac.utilities.GUIExceptionHandler;
import mobac.utilities.Utilities;

import org.apache.log4j.Logger;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

public class MainGUI extends JFrame implements MapEventListener {

	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(MainGUI.class);

	private static Color labelBackgroundColor = new Color(0, 0, 0, 127);
	private static Color labelForegroundColor = Color.white;

	private static MainGUI mainGUI = null;
	public static final ArrayList<Image> MOBAC_ICONS = new ArrayList<Image>(3);

	protected JAtlasTree jAtlasTree;
	public PreviewMap previewMap;

	private JLabel zoomLevelText;
	private JComboBox gridZoomCombo;
	private JSlider zoomSlider;
	private JComboBox mapSourceCombo;
	private JButton helpButton;
	private JButton settingsButton;
	private JAtlasNameField atlasNameTextField;
	private JComboBox atlasOutputFormatCombo;
	private JButton createAtlasButton;
	private JPanel zoomLevelPanel;
	private JCheckBox[] cbZoom = new JCheckBox[0];
	private JLabel amountOfTilesLabel;

	private JCoordinatesPanel coordinatesPanel;
	private JProfilesPanel profilesPanel;
	private JTileImageParametersPanel tileImageParametersPanel;
	private JTileStoreCoveragePanel tileStoreCoveragePanel;

	private JPanel mapControlPanel = new JPanel(new BorderLayout());
	private JPanel leftPanel = new JPanel(new GridBagLayout());
	private JPanel leftPanelContent = null;

	private MercatorPixelCoordinate mapSelectionMax = null;
	private MercatorPixelCoordinate mapSelectionMin = null;

	public static void createMainGui() {
		if (mainGUI != null)
			return;
		mainGUI = new MainGUI();
		mainGUI.setVisible(true);
		log.trace("MainGUI now visible");
	}

	public static MainGUI getMainGUI() {
		return mainGUI;
	}

	private MainGUI() {
		super();
		MOBAC_ICONS.add(Utilities.loadResourceImageIcon("mobac48.png").getImage());
		MOBAC_ICONS.add(Utilities.loadResourceImageIcon("mobac32.png").getImage());
		MOBAC_ICONS.add(Utilities.loadResourceImageIcon("mobac16.png").getImage());
		setIconImages(MOBAC_ICONS);
		GUIExceptionHandler.registerForCurrentThread();
		setTitle(ProgramInfo.getCompleteTitle());

		log.trace("Creating main dialog - " + getTitle());
		setResizable(true);
		Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
		setMinimumSize(new Dimension(Math.min(800, dScreen.width), Math.min(590, dScreen.height)));
		setSize(getMinimumSize());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowDestroyer());
		addComponentListener(new MainWindowListener());

		previewMap = new PreviewMap();
		previewMap.addMapEventListener(this);

		createControls();
		calculateNrOfTilesToDownload();
		setLayout(new BorderLayout());
		add(leftPanel, BorderLayout.WEST);
		JLayeredPane layeredPane = new FilledLayeredPane();
		layeredPane.add(previewMap, Integer.valueOf(0));
		layeredPane.add(mapControlPanel, Integer.valueOf(1));
		add(layeredPane, BorderLayout.CENTER);

		updatePanels();
		loadSettings();
		profilesPanel.initialize();
		mapSourceChanged(previewMap.getMapSource());
		updateZoomLevelCheckBoxes();
		updateGridSizeCombo();
		tileImageParametersPanel.updateControlsState();
		zoomChanged(previewMap.getZoom());
		gridZoomChanged(previewMap.getGridZoom());
		previewMap.updateMapSelection();
		previewMap.grabFocus();
		MapSourcesUpdater.automaticMapsourcesOnlineUpdate(true);
	}

	private void createControls() {

		// zoom slider
		zoomSlider = new JSlider(JMapViewer.MIN_ZOOM, previewMap.getMapSource().getMaxZoom());
		zoomSlider.setOrientation(JSlider.HORIZONTAL);
		zoomSlider.setMinimumSize(new Dimension(50, 10));
		zoomSlider.setSize(50, zoomSlider.getPreferredSize().height);
		zoomSlider.addChangeListener(new ZoomSliderListener());
		zoomSlider.setOpaque(false);

		// zoom level text
		zoomLevelText = new JLabel(" 00 ");
		zoomLevelText.setOpaque(true);
		zoomLevelText.setBackground(labelBackgroundColor);
		zoomLevelText.setForeground(labelForegroundColor);
		zoomLevelText.setToolTipText("The current zoom level");

		// grid zoom combo
		gridZoomCombo = new JComboBox();
		gridZoomCombo.setEditable(false);
		gridZoomCombo.addActionListener(new GridZoomComboListener());
		gridZoomCombo.setToolTipText("Projects a grid of the specified zoom level over the map");

		// map source combo
		mapSourceCombo = new JComboBox(MapSourcesManager.getInstance().getEnabledMapSources());
		mapSourceCombo.setMaximumRowCount(20);
		mapSourceCombo.addActionListener(new MapSourceComboListener());
		mapSourceCombo.setToolTipText("Select map source");

		// help button
		helpButton = new JButton("Help");
		helpButton.addActionListener(new ShowHelpAction());
		helpButton.setToolTipText("Display some help information");

		// settings button
		settingsButton = new JButton("Settings");
		settingsButton.addActionListener(new SettingsButtonListener());
		settingsButton.setToolTipText("Open the preferences dialogue panel.");

		// atlas output format
		atlasOutputFormatCombo = new JComboBox(AtlasOutputFormat.values());
		atlasOutputFormatCombo.setMaximumRowCount(15);
		atlasOutputFormatCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyAtlasOutputFormat();
			}
		});

		// atlas name text field
		atlasNameTextField = new JAtlasNameField();
		atlasNameTextField.setColumns(12);
		atlasNameTextField.setActionCommand("atlasNameTextField");
		atlasNameTextField.setToolTipText("Enter a name for the atlas here");

		// create atlas button
		createAtlasButton = new JButton("Create atlas");
		createAtlasButton.addActionListener(new CreateAtlasButtonListener());
		createAtlasButton.setToolTipText("Create the atlas");

		// zoom level check boxes
		zoomLevelPanel = new JPanel();
		zoomLevelPanel.setBorder(BorderFactory.createEmptyBorder());
		zoomLevelPanel.setOpaque(false);

		// amount of tiles to download
		amountOfTilesLabel = new JLabel();
		amountOfTilesLabel.setToolTipText("Total amount of tiles to download");
		amountOfTilesLabel.setOpaque(true);
		amountOfTilesLabel.setBackground(labelBackgroundColor);
		amountOfTilesLabel.setForeground(labelForegroundColor);

		jAtlasTree = new JAtlasTree(previewMap);

		coordinatesPanel = new JCoordinatesPanel();
		tileImageParametersPanel = new JTileImageParametersPanel();
		profilesPanel = new JProfilesPanel(jAtlasTree);
		profilesPanel.getLoadButton().addActionListener(new LoadProfileListener());
		tileStoreCoveragePanel = new JTileStoreCoveragePanel(previewMap);
	}

	private void updateLeftPanel() {
		leftPanel.removeAll();

		coordinatesPanel.addButtonActionListener(new ApplySelectionButtonListener());

		JCollapsiblePanel mapSourcePanel = new JCollapsiblePanel("Map source", new GridBagLayout());
		mapSourcePanel.addContent(mapSourceCombo, GBC.std().insets(2, 2, 2, 2).fill());

		JCollapsiblePanel zoomLevelsPanel = new JCollapsiblePanel("Zoom Levels", new GridBagLayout());
		zoomLevelsPanel.addContent(zoomLevelPanel, GBC.eol().insets(2, 4, 2, 0));
		zoomLevelsPanel.addContent(amountOfTilesLabel, GBC.std().anchor(GBC.WEST).insets(0, 5, 0, 2));

		GBC gbc_std = GBC.std().insets(5, 2, 5, 3);
		GBC gbc_eol = GBC.eol().insets(5, 2, 5, 3);

		JCollapsiblePanel atlasContentPanel = new JCollapsiblePanel("Atlas Content", new GridBagLayout());
		JScrollPane treeScrollPane = new JScrollPane(jAtlasTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jAtlasTree.getTreeModel().addTreeModelListener(new AtlasListener());

		treeScrollPane.setMinimumSize(new Dimension(100, 150));
		treeScrollPane.setPreferredSize(new Dimension(100, 200));
		treeScrollPane.setAutoscrolls(true);
		atlasContentPanel.addContent(treeScrollPane, GBC.eol().fill().insets(0, 1, 0, 0));
		JButton clearAtlas = new JButton("Clear");
		atlasContentPanel.addContent(clearAtlas, GBC.std());
		clearAtlas.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				jAtlasTree.clearAtlas();
				previewMap.repaint();
				applyAtlasOutputFormat();
			}
		});
		JButton addLayers = new JButton("Add selection");
		atlasContentPanel.addContent(addLayers, GBC.eol());
		addLayers.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				addSelectedAutoCutMultiMapLayers();
			}
		});
		atlasContentPanel.addContent(new JLabel("Name: "), gbc_std);
		atlasContentPanel.addContent(atlasNameTextField, gbc_eol.fill(GBC.HORIZONTAL));

		JCollapsiblePanel atlasNamePanel = new JCollapsiblePanel("Atlas settings", new GridBagLayout());
		atlasNamePanel.addContent(new JLabel("Format: "), gbc_std);
		atlasNamePanel.addContent(atlasOutputFormatCombo, gbc_eol);

		gbc_eol = GBC.eol().insets(5, 2, 5, 2).fill(GBC.HORIZONTAL);

		JCollapsiblePanel gpxPanel = new JGpxPanel(previewMap);

		leftPanelContent = new JPanel(new GridBagLayout());
		leftPanelContent.add(coordinatesPanel, gbc_eol);
		leftPanelContent.add(mapSourcePanel, gbc_eol);
		leftPanelContent.add(zoomLevelsPanel, gbc_eol);
		leftPanelContent.add(tileImageParametersPanel, gbc_eol);
		leftPanelContent.add(atlasContentPanel, gbc_eol);

		leftPanelContent.add(atlasNamePanel, gbc_eol);
		leftPanelContent.add(profilesPanel, gbc_eol);
		leftPanelContent.add(createAtlasButton, gbc_eol);
		leftPanelContent.add(settingsButton, gbc_eol);
		leftPanelContent.add(tileStoreCoveragePanel, gbc_eol);
		leftPanelContent.add(gpxPanel, gbc_eol);
		leftPanelContent.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));

		JScrollPane scrollPane = new JScrollPane(leftPanelContent);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		// Set the scroll pane width large enough so that the
		// scroll bar has enough space to appear right to it
		Dimension d = scrollPane.getPreferredSize();
		d.width += 5 + scrollPane.getVerticalScrollBar().getWidth();
		// scrollPane.setPreferredSize(d);
		scrollPane.setMinimumSize(d);
		leftPanel.add(scrollPane, GBC.std().fill());
		// leftPanel.add(leftPanelContent, GBC.std().fill());
	}

	private JPanel updateMapControlsPanel() {
		mapControlPanel.removeAll();
		mapControlPanel.setOpaque(false);

		// zoom label
		JLabel zoomLabel = new JLabel(" Zoom: ");
		zoomLabel.setOpaque(true);
		zoomLabel.setBackground(labelBackgroundColor);
		zoomLabel.setForeground(labelForegroundColor);

		// top panel
		JPanel topControls = new JPanel(new GridBagLayout());
		topControls.setOpaque(false);
		topControls.add(zoomLabel, GBC.std().insets(5, 5, 0, 0));
		topControls.add(zoomSlider, GBC.std().insets(0, 5, 0, 0));
		topControls.add(zoomLevelText, GBC.std().insets(0, 5, 0, 0));
		topControls.add(gridZoomCombo, GBC.std().insets(10, 5, 0, 0));
		topControls.add(Box.createHorizontalGlue(), GBC.std().fillH());
		topControls.add(helpButton, GBC.std().insets(10, 5, 5, 0));
		mapControlPanel.add(topControls, BorderLayout.NORTH);

		// bottom panel
		// JPanel bottomControls = new JPanel(new GridBagLayout());
		// bottomControls.setOpaque(false);
		// bottomControls.add(Box.createHorizontalGlue(),
		// GBC.std().fill(GBC.HORIZONTAL));
		// mapControlPanel.add(bottomControls, BorderLayout.SOUTH);

		return mapControlPanel;
	}

	private void updatePanels() {
		updateMapControlsPanel();
		updateLeftPanel();
		leftPanel.setVisible(true);
		calculateNrOfTilesToDownload();
		updateZoomLevelCheckBoxes();
		previewMap.grabFocus();
	}

	public void updateMapSourcesList() {
		MapSource ms = (MapSource) mapSourceCombo.getSelectedItem();
		mapSourceCombo.setModel(new DefaultComboBoxModel(MapSourcesManager.getInstance().getEnabledMapSources()));
		mapSourceCombo.setSelectedItem(ms);
		MapSource ms2 = (MapSource) mapSourceCombo.getSelectedItem();
		if (!ms.equals(ms2))
			previewMap.setMapSource(ms2);
	}

	private void loadSettings() {
		Settings settings = Settings.getInstance();
		atlasNameTextField.setText(settings.elementName);
		atlasOutputFormatCombo.setSelectedItem(settings.getAtlasOutputFormat());
		previewMap.settingsLoad();
		coordinatesPanel.setNumberFormat(settings.coordinateNumberFormat);

		tileImageParametersPanel.loadSettings();
		// mapSourceCombo
		// .setSelectedItem(MapSourcesManager.getSourceByName(settings.
		// mapviewMapSource));

		setSize(settings.mainWindow.size);
		Point windowLocation = settings.mainWindow.position;
		if (windowLocation.x == -1 && windowLocation.y == -1) {
			setLocationRelativeTo(null);
		} else {
			setLocation(windowLocation);
		}
		if (settings.mainWindow.maximized)
			setExtendedState(Frame.MAXIMIZED_BOTH);

		if (leftPanelContent != null) {
			for (Component c : leftPanelContent.getComponents()) {
				if (c instanceof JCollapsiblePanel) {
					JCollapsiblePanel cp = (JCollapsiblePanel) c;
					String name = cp.getName();
					if (name != null && settings.mainWindow.collapsedPanels.contains(name))
						cp.setCollapsed(true);
				}
			}
		}
	}

	private void saveSettings() {
		try {
			Settings s = Settings.getInstance();
			previewMap.settingsSave();
			s.mapviewMapSource = previewMap.getMapSource().getName();

			s.elementName = atlasNameTextField.getText();
			s.setAtlasOutputFormat((AtlasOutputFormat) atlasOutputFormatCombo.getSelectedItem());
			s.coordinateNumberFormat = coordinatesPanel.getNumberFormat();

			tileImageParametersPanel.saveSettings();
			boolean maximized = (getExtendedState() & Frame.MAXIMIZED_BOTH) != 0;
			s.mainWindow.maximized = maximized;
			if (!maximized) {
				s.mainWindow.size = getSize();
				s.mainWindow.position = getLocation();
			}
			s.mainWindow.collapsedPanels.clear();
			if (leftPanelContent != null) {
				for (Component c : leftPanelContent.getComponents()) {
					if (c instanceof JCollapsiblePanel) {
						JCollapsiblePanel cp = (JCollapsiblePanel) c;
						if (cp.isCollapsed())
							s.mainWindow.collapsedPanels.add(cp.getName());
					}
				}
			}
			checkAndSaveSettings();
		} catch (Exception e) {
			GUIExceptionHandler.showExceptionDialog(e);
			JOptionPane.showMessageDialog(null, "Error on writing program settings to \"settings.xml\"", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void checkAndSaveSettings() throws JAXBException {
		if (Settings.checkSettingsFileModified()) {
			int x = JOptionPane.showConfirmDialog(this,
					"The settings.xml files has been changed by another application.\n"
							+ "Do you want to overwrite these changes?\n"
							+ "All changes made by the other application will be lost!", "Overwrite changes?",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (x != JOptionPane.YES_OPTION)
				return;
		}
		Settings.save();

	}

	public String getUserText() {
		return atlasNameTextField.getText();
	}

	public void refreshPreviewMap() {
		previewMap.refreshMap();
	}

	private class ZoomSliderListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			previewMap.setZoom(zoomSlider.getValue());
		}
	}

	private class GridZoomComboListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (!gridZoomCombo.isEnabled())
				return;
			GridZoom g = (GridZoom) gridZoomCombo.getSelectedItem();
			if (g == null)
				return;
			log.debug("Selected grid zoom combo box item has changed: " + g.getZoom());
			previewMap.setGridZoom(g.getZoom());
			repaint();
			previewMap.updateMapSelection();
		}
	}

	private void updateGridSizeCombo() {
		int maxZoom = previewMap.getMapSource().getMaxZoom();
		int minZoom = previewMap.getMapSource().getMinZoom();
		GridZoom lastGridZoom = (GridZoom) gridZoomCombo.getSelectedItem();
		gridZoomCombo.setEnabled(false);
		gridZoomCombo.removeAllItems();
		gridZoomCombo.setMaximumRowCount(maxZoom - minZoom + 2);
		gridZoomCombo.addItem(new GridZoom(-1) {

			@Override
			public String toString() {
				return "Grid disabled";
			}

		});
		for (int i = maxZoom; i >= minZoom; i--) {
			gridZoomCombo.addItem(new GridZoom(i));
		}
		if (lastGridZoom != null)
			gridZoomCombo.setSelectedItem(lastGridZoom);
		gridZoomCombo.setEnabled(true);
	}

	private class ApplySelectionButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			setSelectionByEnteredCoordinates();
		}
	}

	private class MapSourceComboListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MapSource mapSource = (MapSource) mapSourceCombo.getSelectedItem();
			if (mapSource == null) {
				mapSourceCombo.setSelectedIndex(0);
				mapSource = (MapSource) mapSourceCombo.getSelectedItem();
			}
			previewMap.setMapSource(mapSource);
			zoomSlider.setMinimum(previewMap.getMapSource().getMinZoom());
			zoomSlider.setMaximum(previewMap.getMapSource().getMaxZoom());
			updateGridSizeCombo();
			updateZoomLevelCheckBoxes();
		}
	}

	private class LoadProfileListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Profile profile = profilesPanel.getSelectedProfile();
			profilesPanel.getDeleteButton().setEnabled(profile != null);
			if (profile == null)
				return;

			jAtlasTree.load(profile);
			previewMap.repaint();
			atlasOutputFormatCombo.setSelectedItem(jAtlasTree.getAtlas().getOutputFormat());
		}
	}

	private class SettingsButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			SettingsGUI.showSettingsDialog(MainGUI.this);
		}
	}

	private class CreateAtlasButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (!jAtlasTree.testAtlasContentValid())
				return;
			if (jAtlasTree.getAtlas().calculateTilesToDownload() > 3000000) {
				JOptionPane.showMessageDialog(null, "Mobile Atlas Creator has detected that you are trying to\n"
						+ "download an extra ordinary large atlas " + "with a very high number of tiles.\n"
						+ "Please reduce the selected areas " + "on high zoom levels and try again.",
						"Atlas download prohibited", JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				// We have to work on a deep clone otherwise the user would be
				// able to modify settings of maps, layers and the atlas itself
				// while the AtlasThread works on that atlas reference
				AtlasInterface atlasToCreate = jAtlasTree.getAtlas().deepClone();
				Thread atlasThread = new AtlasThread(atlasToCreate);
				atlasThread.start();
			} catch (AtlasTestException e) {
				JOptionPane.showMessageDialog(null, "<html>" + e.getMessage() + "</html>",
						"Map incompatible with atlas format", JOptionPane.ERROR_MESSAGE);

			} catch (Exception e) {
				log.error("", e);
				GUIExceptionHandler.processException(e);
			}
		}
	}

	private void updateZoomLevelCheckBoxes() {
		MapSource tileSource = previewMap.getMapSource();
		int zoomLevels = tileSource.getMaxZoom() - tileSource.getMinZoom() + 1;
		JCheckBox oldZoomLevelCheckBoxes[] = cbZoom;
		cbZoom = new JCheckBox[zoomLevels];
		zoomLevelPanel.removeAll();

		zoomLevelPanel.setLayout(new GridLayout(0, 10, 1, 2));
		ZoomLevelCheckBoxListener cbl = new ZoomLevelCheckBoxListener();

		for (int i = cbZoom.length - 1; i >= 0; i--) {
			int cbz = i + tileSource.getMinZoom();
			JCheckBox cb = new JCheckBox();
			cb.setPreferredSize(new Dimension(22, 11));
			cb.setMinimumSize(cb.getPreferredSize());
			cb.setOpaque(false);
			cb.setFocusable(false);
			if (i < oldZoomLevelCheckBoxes.length)
				cb.setSelected(oldZoomLevelCheckBoxes[i].isSelected());
			cb.addActionListener(cbl);
			cb.setToolTipText("Select zoom level " + cbz + " for atlas");
			zoomLevelPanel.add(cb);
			cbZoom[i] = cb;

			JLabel l = new JLabel(Integer.toString(cbz));
			zoomLevelPanel.add(l);
		}
		amountOfTilesLabel.setOpaque(false);
		amountOfTilesLabel.setForeground(Color.black);
	}

	private class ZoomLevelCheckBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			calculateNrOfTilesToDownload();
		}
	}

	public void selectionChanged(MercatorPixelCoordinate max, MercatorPixelCoordinate min) {
		mapSelectionMax = max;
		mapSelectionMin = min;
		coordinatesPanel.setSelection(max, min);
		calculateNrOfTilesToDownload();
	}

	public void zoomChanged(int zoomLevel) {
		zoomLevelText.setText(" " + zoomLevel + " ");
		zoomSlider.setValue(zoomLevel);
	}

	public void gridZoomChanged(int newGridZoomLevel) {
		gridZoomCombo.setSelectedItem(new GridZoom(newGridZoomLevel));
	}

	public void applyAtlasOutputFormat() {
		AtlasOutputFormat atlasOutputFormat = (AtlasOutputFormat) atlasOutputFormatCombo.getSelectedItem();
		jAtlasTree.getAtlas().setOutputFormat(atlasOutputFormat);
	}

	public void selectNextMapSource() {
		if (mapSourceCombo.getSelectedIndex() == mapSourceCombo.getItemCount() - 1) {
			Toolkit.getDefaultToolkit().beep();
		} else {
			mapSourceCombo.setSelectedIndex(mapSourceCombo.getSelectedIndex() + 1);
		}
	}

	public void selectPreviousMapSource() {
		if (mapSourceCombo.getSelectedIndex() == 0) {
			Toolkit.getDefaultToolkit().beep();
		} else {
			mapSourceCombo.setSelectedIndex(mapSourceCombo.getSelectedIndex() - 1);
		}
	}

	private void addSelectedAutoCutMultiMapLayers() {
		final String mapNameFmt = "%s %02d";
		AtlasInterface atlasInterface = jAtlasTree.getAtlas();
		String name = atlasNameTextField.getText();
		MapSource tileSource = (MapSource) mapSourceCombo.getSelectedItem();
		SelectedZoomLevels sZL = new SelectedZoomLevels(previewMap.getMapSource().getMinZoom(), cbZoom);
		MapSelection ms = getMapSelectionCoordinates();
		if (ms == null) {
			JOptionPane.showMessageDialog(this, "Please select an area");
			return;
		}
		Settings settings = Settings.getInstance();
		String errorText = validateInput();
		if (errorText.length() > 0) {
			JOptionPane.showMessageDialog(null, errorText, "Errors", JOptionPane.ERROR_MESSAGE);
			return;
		}

		int[] zoomLevels = sZL.getZoomLevels();
		if (zoomLevels.length == 0) {
			JOptionPane.showMessageDialog(this, "Please select at least one zoom level");
			return;
		}

		String layerName = name;
		Layer layer = null;
		int c = 1;
		boolean success = false;
		do {
			try {
				layer = new Layer(atlasInterface, layerName);
				success = true;
			} catch (InvalidNameException e) {
				layerName = name + "_" + Integer.toString(c++);
			}
		} while (!success);
		for (int zoom : zoomLevels) {
			Point tl = ms.getTopLeftPixelCoordinate(zoom);
			Point br = ms.getBottomRightPixelCoordinate(zoom);
			TileImageParameters customTileParameters = getSelectedTileImageParameters();
			try {
				String mapName = String.format(mapNameFmt, new Object[] { layerName, zoom });
				layer.addMapsAutocut(mapName, tileSource, tl, br, zoom, customTileParameters, settings.maxMapSize);
			} catch (InvalidNameException e) {
				log.error("", e);
			}
		}
		atlasInterface.addLayer(layer);
		jAtlasTree.getTreeModel().notifyNodeInsert(layer);
	}

	public void mapSourceChanged(MapSource newMapSource) {
		// TODO update selected area if new map source has different projectionCategory
		calculateNrOfTilesToDownload();
		if (newMapSource.equals(mapSourceCombo.getSelectedItem()))
			return;
		mapSourceCombo.setSelectedItem(newMapSource);
	}

	private void setSelectionByEnteredCoordinates() {
		coordinatesPanel.correctMinMax();
		MapSelection ms = coordinatesPanel.getMapSelection(previewMap.getMapSource());
		if (ms.isAreaSelected()) {
			mapSelectionMax = ms.getBottomRightPixelCoordinate();
			mapSelectionMin = ms.getTopLeftPixelCoordinate();
			previewMap.setSelectionAndZoomTo(ms, false);
		} else {
			Toolkit.getDefaultToolkit().beep();
		}
	}

	private MapSelection getMapSelectionCoordinates() {
		if (mapSelectionMax == null || mapSelectionMin == null)
			return null;
		return new MapSelection(previewMap.getMapSource(), mapSelectionMax, mapSelectionMin);
	}

	private String validateInput() {

		String errorText = "";
		errorText += tileImageParametersPanel.getValidationErrorMessages();

		return errorText;
	}

	public TileImageParameters getSelectedTileImageParameters() {
		return tileImageParametersPanel.getSelectedTileImageParameters();
	}

	private void calculateNrOfTilesToDownload() {
		MapSelection ms = getMapSelectionCoordinates();
		String baseText;
		baseText = " %s tiles ";
		if (ms == null || !ms.isAreaSelected()) {
			amountOfTilesLabel.setText(String.format(baseText, "0"));
			amountOfTilesLabel.setToolTipText("");
		} else {
			try {
				SelectedZoomLevels sZL = new SelectedZoomLevels(previewMap.getMapSource().getMinZoom(), cbZoom);

				int[] zoomLevels = sZL.getZoomLevels();

				long totalNrOfTiles = 0;

				StringBuilder hint = new StringBuilder(1024);
				hint.append("Total amount of tiles to download:");
				for (int i = 0; i < zoomLevels.length; i++) {
					int zoom = zoomLevels[i];
					long[] info = ms.calculateNrOfTilesEx(zoom);
					totalNrOfTiles += info[0];
					hint.append("<br>Level " + zoomLevels[i] + ": " + info[0] + " (" + info[1] + "*" + info[2] + ")");
				}
				String hintText = "<html>" + hint.toString() + "</html>";
				amountOfTilesLabel.setText(String.format(baseText, Long.toString(totalNrOfTiles)));
				amountOfTilesLabel.setToolTipText(hintText);
			} catch (Exception e) {
				amountOfTilesLabel.setText(String.format(baseText, "?"));
				log.error("", e);
			}
		}
	}

	private class AtlasListener implements TreeModelListener {

		protected void changed() {
			profilesPanel.getSaveAsButton().setEnabled(jAtlasTree.getAtlas().getLayerCount() > 0);
		}

		public void treeNodesChanged(TreeModelEvent e) {
			changed();
		}

		public void treeNodesInserted(TreeModelEvent e) {
			changed();
		}

		public void treeNodesRemoved(TreeModelEvent e) {
			changed();
		}

		public void treeStructureChanged(TreeModelEvent e) {
			changed();
		}
	}

	private class WindowDestroyer extends WindowAdapter {

		@Override
		public void windowOpened(WindowEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					previewMap.setEnabled(true);
				}
			});
		}

		public void windowClosing(WindowEvent event) {
			saveSettings();
			TileStore.getInstance().closeAll(true);
		}
	}

	/**
	 * Saves the window position and size when window is moved or resized. This is necessary because of the maximized
	 * state. If a window is maximized it is impossible to retrieve the window size & position of the non-maximized
	 * window - therefore we have to collect the information every time they change.
	 */
	private class MainWindowListener extends ComponentAdapter {
		public void componentResized(ComponentEvent event) {
			// log.debug(event.paramString());
			updateValues();
		}

		public void componentMoved(ComponentEvent event) {
			// log.debug(event.paramString());
			updateValues();
		}

		private void updateValues() {
			// only update old values while window is in NORMAL state
			// Note(Java bug): Sometimes getExtendedState() says the window is
			// not maximized but maximizing is already in progress and therefore
			// the window bounds are already changed.
			if ((getExtendedState() & MAXIMIZED_BOTH) != 0)
				return;
			Settings s = Settings.getInstance();
			s.mainWindow.size = getSize();
			s.mainWindow.position = getLocation();
		}
	}

}
