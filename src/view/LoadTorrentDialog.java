package view;

import java.util.ArrayList;

import Bencode.CreateTorrent;
import clientTorrent.Torrent;

import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDialogButtonBox;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QScrollArea;
import com.trolltech.qt.gui.QWidget;

public class LoadTorrentDialog extends QDialog {
	private QLabel tracker;
	private QLabel file_source;
	private QLabel torrent_dest;
	private QLabel comment ;
	private QLineEdit tracker_line ;
	private QLineEdit file_source_line ;
	private QLineEdit torrent_dest_line ;
	private QLineEdit comment_line ;
	private QWidget father ;
	private TorrentView view ;
	
	public LoadTorrentDialog(QWidget parent,TorrentView view){
		super(parent);
		this.father = parent ;
		this.view = view ;
		
		QGroupBox filebox = new QGroupBox(tr("File Informations:"),this);
		QGroupBox torrentbox = new QGroupBox(tr("Torrent Informations"),this);
		
		QPushButton location = new QPushButton(QIcon.fromTheme("document-open"),"",this);
		QPushButton location2 = new QPushButton(QIcon.fromTheme("document-open"),"",this);
		
		location.clicked.connect(this,"load_file()");
		location2.clicked.connect(this,"load_torrent_dest()");
		
		
		/*Init labels*/
		tracker = new QLabel(tr("Tracker :"),this);
		file_source = new QLabel(tr("File source :"),this);
		torrent_dest = new QLabel(tr("Destination :"),this);
		comment = new QLabel(tr("Comment :"),this);
		
		/*Init Line Edits*/
		tracker_line = new QLineEdit();
		tracker_line.setFocus();
		
		torrent_dest_line = new QLineEdit();
		torrent_dest_line.setFocus();
		torrent_dest_line.setReadOnly(true);
				
		file_source_line = new QLineEdit();
		file_source_line.setFocus();
		file_source_line.setReadOnly(true);
		
		comment_line = new QLineEdit();
		comment_line.setFocus();
		
		/*Init Layouts*/
		QGridLayout file_grid = new QGridLayout(this);
		file_grid.addWidget(file_source,0,0);
		file_grid.addWidget(file_source_line,0,1,1,2);
		file_grid.addWidget(location,0,3);
		file_grid.addWidget(torrent_dest,1,0);
		file_grid.addWidget(torrent_dest_line,1,1,1,2);
		file_grid.addWidget(location2,1,3);
		filebox.setLayout(file_grid);
		
		QGridLayout torrent_grid = new QGridLayout(this);
		torrent_grid.addWidget(tracker,0,0);
		torrent_grid.addWidget(tracker_line,0,1,1,2);
		torrent_grid.addWidget(comment,1,0);
		torrent_grid.addWidget(comment_line,1,1,1,2);
		torrentbox.setLayout(torrent_grid);
		
		/*Init Buttons*/
		QDialogButtonBox boutons = new QDialogButtonBox();
		
		
        boutons.addButton(QDialogButtonBox.StandardButton.Ok);
        boutons.addButton(QDialogButtonBox.StandardButton.Cancel);
        boutons.clearFocus();
        boutons.accepted.connect(this,"create_widget()");
        boutons.rejected.connect(this,"close()");
		
        QGridLayout loadlayout = new QGridLayout();
        loadlayout.addWidget(filebox,0,0);
        loadlayout.addWidget(torrentbox,1,0);
        loadlayout.addWidget(boutons,2,0);
        
        setLayout(loadlayout);
        setWindowTitle("Create a Torrent");
        setSizeGripEnabled(true);
	}
	
	public void load_torrent_dest(){
		String dir = QFileDialog.getExistingDirectory(this, tr("Choose Directory"),
                "/home",
                QFileDialog.Option.createQFlags(
                QFileDialog.Option.ShowDirsOnly,
                QFileDialog.Option.DontResolveSymlinks));
				if(dir != null && dir.length()>0)
				torrent_dest_line.setText(dir);
	}
	
	public void load_file(){
		String filename = QFileDialog.getOpenFileName(this,
			      tr("Open File"), "./");
				  if(filename != null && filename.length()>0){
					  file_source_line.setText(filename);
				  }
		
	}
	
	public void create_widget(){
		if(tracker_line.text().length() > 0 && file_source_line.text().length()> 0 && torrent_dest_line.text().length() >0){
			String announce = tracker_line.text();
			String filepath = file_source_line.text();
			String torrent_path = torrent_dest_line.text();
			String comment = comment_line.text();
			try{
			Torrent torrent = CreateTorrent.create_torrent(filepath, torrent_path, announce, comment);
			view.getList_torrent().add(torrent);
            view.getFenetre().addWidget(new TorrentWidget(torrent,((QScrollArea)this.parentWidget()).widget(),view));
			this.close();
			}catch(Exception e){
				e.printStackTrace();
				return ;
			}
		}
		return ;
	}
	
	
	
	
}
