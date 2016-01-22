package view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import clientTorrent.Resume;
import clientTorrent.Torrent;
import clientTorrent.TextUtil;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QKeySequence;
import com.trolltech.qt.gui.QLayout;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QMenuBar;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QScrollArea;
import com.trolltech.qt.gui.QToolBar;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import Bencode.Decoding;
import BencodeType.Dictionary;

public class TorrentView extends QMainWindow {

	private QMenu fileMenu;
	private QMenu torrentMenu;
	private QAction nouveau;
	private QAction fermer;
	private QAction ouvrir;
	private QAction proprietes;
	private QAction sauver;
	public QAction start;
	public QAction pause;
	public QAction delete;
	private QToolBar fileToolbar;
	private QToolBar torrentToolbar;
	private QLayout fenetre;
	private QScrollArea window;
	private QWidget layout_in_fenetre;
	private ArrayList<Torrent> list_torrent = new ArrayList<Torrent>();

	// private ExecutorService executor = Executors.newFixedThreadPool(5);

	public ArrayList<Torrent> getList_torrent() {
		return list_torrent;
	}

	public synchronized void setList_torrent(ArrayList<Torrent> list_torrent) {
		this.list_torrent = list_torrent;
	}

	public TorrentView() {

//	File file = new File("./LOG/LOG_"+System.currentTimeMillis()/1000+".txt");
//		PrintStream printstream ;
//		try {
//		printstream = new PrintStream(file);
//			System.setOut(printstream);
//		} catch (FileNotFoundException e) {
//			// TODO AUTO-GENERATED CATCH BLOCK
//			e.printStackTrace();
//		}

		this.setWindowTitle("Torrent Application");
		QMenuBar menubar = new QMenuBar();
		this.setMenuBar(menubar);
		window = new QScrollArea(this);
		window.setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAsNeeded);
		window.setAutoFillBackground(true);
		layout_in_fenetre = new QWidget(this);

		fenetre = new QVBoxLayout(window);
		fenetre.setWidgetSpacing(1);
		fenetre.setMargin(0);
		fenetre.setAlignment(new Qt.Alignment(Qt.AlignmentFlag.AlignTop.value()));

		layout_in_fenetre.setLayout(fenetre);
		window.setWidget(layout_in_fenetre);
		window.setWidgetResizable(true);
		this.setCentralWidget(window);

		// Color Widget
		QPalette pal = new QPalette();
		pal.setColor(QPalette.ColorRole.Window, QColor.white);
		layout_in_fenetre.setPalette(pal);
		layout_in_fenetre.show();

		createActions();
		createMenus();
		createToolBars();
		createStatusBar();
		setUnifiedTitleAndToolBarOnMac(true);
		
//		try {
//			loadResume();
//		} catch (IOException e) {
//			System.err.println("PROBLEM RESUME TORRENTS");
//			e.printStackTrace();
//		}

	}

	private void createStatusBar() {
		statusBar().showMessage(tr("Pret"));
	}

	private void createToolBars() {
		/* File ToolBar */
		fileToolbar = this.addToolBar(tr("Fichier"));
		fileToolbar.addAction(nouveau);
		fileToolbar.addAction(ouvrir);

		/* Torrent ToolBar */
		torrentToolbar = this.addToolBar(tr("Propriétés"));
		torrentToolbar.addAction(start);
		torrentToolbar.addAction(pause);
		torrentToolbar.addAction(delete);
	}

	private void createMenus() {
		/* Menu Fichier */
		fileMenu = menuBar().addMenu(tr("&Fichier"));
		fileMenu.addAction(nouveau);
		fileMenu.addAction(ouvrir);

		/* Menu Propriétés */
		torrentMenu = menuBar().addMenu(tr("&Torrent"));

	}

	private void createActions() {

		nouveau = new QAction(QIcon.fromTheme("document-new"), tr("&Nouveau"),
				this);
		nouveau.setShortcut(new QKeySequence(tr("Ctrl+N")));
		nouveau.setStatusTip("Nouveau Torrent");
		nouveau.triggered.connect(this, "load()");

		ouvrir = new QAction(QIcon.fromTheme("document-open"), tr("&Ouvrir"),
				this);
		ouvrir.setShortcut(new QKeySequence(tr("Ctrl+O")));
		ouvrir.setStatusTip("Ouvrir un Torrent");
		ouvrir.triggered.connect(this, "open()");

		

		start = new QAction(QIcon.fromTheme("media-playback-start"),
				tr("&Démarrer"), this);
		start.setStatusTip("Démarrer le téléchargement");
		start.setEnabled(false);
		start.triggered.connect(this, "start()");

		pause = new QAction(QIcon.fromTheme("media-playback-pause"),
				tr("&Suspendre"), this);
		pause.setStatusTip("Suspendre le téléchargement");
		pause.setEnabled(false);
		pause.triggered.connect(this, "pause()");

		delete = new QAction(QIcon.fromTheme("edit-delete"),
				tr("&Supprimer le Torrent"), this);
		delete.setStatusTip("Supprimer le Torrent");
		delete.setEnabled(false);
		delete.triggered.connect(this, "deleteTorrent()");

	}	
	
	@Override
	protected void closeEvent(QCloseEvent event){
		System.out.println("Doing Resumes");
		for(Torrent torrent : list_torrent){
			try {
				torrent.doResume();
				torrent.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.close();

	}

	public void open() {
		String fileName = QFileDialog.getOpenFileName(this, tr("Open File"),
				"./", new QFileDialog.Filter(tr("Torrent Files (*.torrent)")));
		if (fileName != null && fileName.length() > 0) {
			try {
				Torrent tor = new Torrent(fileName);
				QWidget torrent_widget = new TorrentWidget(tor,
						layout_in_fenetre, this);
				list_torrent.add(tor);
				fenetre.addWidget(torrent_widget);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void load() {
		LoadTorrentDialog dialogbox = new LoadTorrentDialog(window, this);
		dialogbox.show();

	}

	public void loadResume() throws IOException {
		
		File [] tab_files = TextUtil.listFilesMatching(new File("./resume/"), "[^ ]*.resume");
		for(File fichier : tab_files){
			 System.out.println("Resuming "+fichier.getAbsolutePath());
			 Decoding decodeur = new Decoding();
				decodeur.readFile(fichier.getAbsolutePath());
				Dictionary dico = decodeur.getDictionary() ;
				String filepath = new String((byte []) dico.getAttribute("filepath"),"UTF-8");
				if(filepath != null){
					Torrent tor = new Torrent(fichier.getAbsolutePath(),filepath) ;
					TorrentWidget tor_widget = new TorrentWidget(tor,layout_in_fenetre,this);
					list_torrent.add(tor);
					fenetre.addWidget(tor_widget);
				}			
		}	
	
		/*
		ArrayList<Torrent> liste = Resume.resumeTorrents();
		for (Torrent tor : liste) {
			QWidget torrent_widget = new TorrentWidget(tor,window,this);

		*/
	}
	
	public void save() throws IOException {
		System.out.println(list_torrent.size());
		
		for(Torrent torrent : list_torrent)
		Resume.createResume(torrent);
	}

	public void pause() throws Exception {
		System.out.println("Pausing Torrent");
		List<QWidget> liste = pause.associatedWidgets();
		for (QWidget widget : liste) {
			if (widget.hasFocus()) {
				((TorrentWidget) widget).pause();
				pause.setEnabled(false);
				start.setEnabled(true);
				return;
			}
		}

	}

	public void start() {
		System.out.println("Restarting torrent");
		List<QWidget> liste = start.associatedWidgets();
		for (QWidget widget : liste) {
			if (widget.hasFocus()) {
				System.out.println("Il a le focus !");
				((TorrentWidget) widget).start();
				pause.setEnabled(true);
				start.setEnabled(false);
				return;
			}
		}

	}

	public void deleteTorrent() throws Exception {
		System.out.println("Deleting Torrent");
		List<QWidget> liste = delete.associatedWidgets();
		for (QWidget widget : liste) {
			if (widget.hasFocus()) {
				((TorrentWidget) widget).deleteTorrent();
				return;
			}
		}
	}

	public QLayout getFenetre() {
		return fenetre;
	}

	public static void main(String[] args) {
		QApplication.initialize(args);
		TorrentView application = new TorrentView();
		application.show();
		QApplication qapp = QApplication.instance();
		qapp.exec();
	}

}
