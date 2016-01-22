package view;

import Bencode.ForLogger;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;

import clientTorrent.Torrent;

public class TorrentWidget extends QWidget{
	
	private TorrentView view ;
	private QWidget parent ;
	private static String LOGGEUR="nothing";
	private static Integer INT_LOGGEUR=1 ; // 0 pour nothing 
//										  1 pour info
//										  2 pour verbose
//								_		  3 pour debbug
	
	private String title ;
	private String info ;
	private String peers_info ;
	private QProgressBar progressbar ;
	private Torrent torrent ;
	private QLabel title_label;
	private QLabel info_label;
	private QLabel peers_info_label;
	private QAction proprietes;
	private QAction more_peers ;
	private QAction opendirectory ;
	private QAction deletetorrent ;
	private QAction deleteall ;
	private Thread torrent_thread ;
	private boolean isPaused = false ;
	
	public TorrentWidget(Torrent torrent_file,QWidget parent,TorrentView view){
		
		this.parent = parent ;
		this.view = view ;
		torrent = torrent_file ;
		title = " <b>"+torrent.getNom()+"</b>" ;
		info = " No file informations" ;
		peers_info = " No peer informations";
		
		/*Labels*/
		title_label = new QLabel(title,this);
		info_label = new QLabel(info,this);
		peers_info_label = new QLabel(peers_info,this);
		
		
		/* ProgressBar */
		progressbar = new QProgressBar(this);
		progressbar.setMinimum(0);
	    progressbar.setMaximum((int)torrent.getSize());

		if(ForLogger.visibility(LOGGEUR, INT_LOGGEUR)){
	    System.out.println(torrent.getNom() + " taille : " + (int)torrent.getSize());
	    System.out.println(torrent.getNom() + " téléchargé: " + (int)torrent.getDownloaded());
		}
	    
		progressbar.setValue((int)torrent.getDownloaded());
		
		QLayout layout = new QVBoxLayout(this);
		layout.setWidgetSpacing(0);
		setAutoFillBackground(true);
		layout.setMargin(0);
		layout.setAlignment(new Qt.Alignment(Qt.AlignmentFlag.AlignTop.value()));
		
		layout.addWidget(title_label);
		layout.addWidget(info_label);
		layout.addWidget(progressbar);
		layout.addWidget(peers_info_label);

		setLayout(layout);
		
		/*Signal*/
		torrent.valueChanged.connect(progressbar,"setValue(int)");
		torrent.stringChanged.connect(info_label, "setText(String)");
		
		/*Action*/
		this.createActions();
		
		/*Menu Contextuel*/
		this.setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu);
		customContextMenuRequested.connect(this,"showContextMenu(QPoint)");
		
		/*Focus*/
		this.setFocusPolicy(Qt.FocusPolicy.ClickFocus);
		QPalette pal = new QPalette();
		pal.setColor(QPalette.ColorRole.Highlight, QColor.blue); 
		setPalette(pal);
		
		torrent_thread = new Thread(torrent);
		torrent_thread.start();
		
	}
	
	private void createActions(){
		proprietes = new QAction(QIcon.fromTheme("document-properties"),
				tr("&Propriétés"), this);
		proprietes.triggered.connect(this,"showProperties()");
		
		more_peers = new QAction(tr("Demander plus de pairs au traqueur"), this);
		more_peers.triggered.connect(this,"askForPeers()");
		
		opendirectory = new QAction(tr("Ouvrir le dossier"),this);
		opendirectory.triggered.connect(this,"openDirectory()");
		
		deletetorrent = new QAction(tr("Enlever le Torrent"),this);
		deletetorrent.triggered.connect(this,"deleteTorrent()");
		
		deleteall = new QAction(tr("Supprimer tout"),this);
		deleteall.triggered.connect(this,"deleteAll()");
		
		this.addAction(view.delete);
		this.addAction(view.pause);
		this.addAction(view.start);
	}
	
	@Override
	public void mousePressEvent(QMouseEvent q ){
		if(this.hasFocus()){
			view.delete.setEnabled(true);
			if(isPaused){
				view.start.setEnabled(true);
				view.pause.setEnabled(false);
			} else {
				view.pause.setEnabled(true);
				view.start.setEnabled(false);
			}
		}
		
	}
	
	public void showContextMenu(QPoint position){
		QPoint global_position = this.mapToGlobal(position);
		QMenu myMenu = new QMenu(this);
		
		/*Actions */
		myMenu.addAction(proprietes);
		myMenu.addAction(opendirectory);
		myMenu.addAction(more_peers);
		myMenu.addAction(deletetorrent);
		myMenu.addAction(deleteall);
		
		QAction triggered = myMenu.exec(global_position);
	}
	
	public void showProperties(){
		System.out.println("Showing Properties");
		QMainWindow window = new QMainWindow(this);
	    
        window.setWindowTitle("Propriétés");
	
        QWidget centralWidget = new QWidget(window);
        QTabWidget tabs = new QTabWidget(centralWidget);
	
        tabs.addTab(new QWidget(),"Détails");  
        tabs.addTab(new QWidget(),"Traqueurs");
	
        window.setCentralWidget(centralWidget);
        window.show();
		
	}
	
	public void askForPeers(){
		System.out.println("More peers");
		try {
			torrent.getTracker().performRequest(Torrent.REGULAR);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void openDirectory(){
		System.out.println("Opening Directory");
		String filepath = torrent.getFilePath() ;
		File fichier = new File(filepath);
		File dirname = fichier.getParentFile();
		Desktop desktop = Desktop.getDesktop() ;
		try {
			desktop.open(dirname);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void deleteTorrent(){
		System.out.println("Delete"+title+" Torrent");
		try{
		torrent.interrupt();
		} catch(Exception e ){
			e.printStackTrace();
		} finally {
		this.hide();
		parent.show();
		}
	}
	
	public void deleteAll(){
		System.out.println("Delete everything");
	}
	
	public void pause() throws Exception{
		System.out.println("Pausing Torrent");
		torrent.interrupt();
		torrent_thread.interrupt();
		isPaused = true ;
		
	}
	
	public void start(){
		torrent_thread.start();
		isPaused = false ;
	}
	

	
	public Torrent getTorrent(){
		return this.torrent ;
	}

}
