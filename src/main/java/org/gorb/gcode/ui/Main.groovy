package org.gorb.gcode.ui;

import org.gorb.gcode.GCodeMachineSetter;
import org.gorb.gcode.PlayerListener;

import groovy.swing.SwingBuilder;
import javax.swing.Timer;

import org.gorb.gcode.GCodeMachineListener;
import org.gorb.gcode.impl.GCodeMachine;
import org.gorb.gcode.impl.Jogger;
import org.gorb.gcode.sim.SenderSimulator;
import org.gorb.pcbgcode.DrillSplit;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import java.awt.BorderLayout as BL;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.Rectangle;
import javax.swing.JFrame;


public class Main implements GCodeMachineListener, PlayerListener {
	public static void main(String[] args) {
		def ui = new Main()
		def setter = new GCodeMachineSetter()
		ui.setter = setter
		ui.machine = setter.buildMachine(ui)
		ui.player = setter.buildPlayer(ui.machine, ui)
		ui.run()
	}
	def machine
	def player
	def setter
	def swing = new SwingBuilder()
	def openFileDialog
	def logPane
	def gcodeText
	def gcodeLabel
	def startButton
	def abortButton
	def pauseButton
	def busyLED
	def playingLED
	def positionModeIndicator
	def mainPanel
	def jogDistance
	
	private void run() {
		openFileDialog  = swing.fileChooser(dialogTitle:"Choose a gcode file", 
	            id:"openFileDialog", fileSelectionMode : JFileChooser.FILES_ONLY, currentDirectory: new File(".")) {
		}
		swing.frame(title: 'GCodePlayer', defaultCloseOperation: JFrame.DISPOSE_ON_CLOSE,
					size: [800, 600], show: true, locationRelativeTo: null,
					windowGainedFocus: { runStartupScript() },
					windowClosing: { runShutdownScript() }
				) {
		    lookAndFeel("system")
		    
		    menuBar() {
		        menu(text: "File", mnemonic: 'F') {
		            menuItem(text: "Open...", mnemonic: 'O', actionPerformed: {
		        		if(openFileDialog.showOpenDialog() != JFileChooser.APPROVE_OPTION) return //user canceled
		        		openFile openFileDialog.selectedFile 
	        		})
		            menuItem(text: "Reload", mnemonic: 'R', actionPerformed: {
			        	reloadFile() 
		        	})
		            separator()
		            menuItem(text: "Split by tool...", mnemonic: 'P', actionPerformed: {
		        		if(openFileDialog.showOpenDialog() != JFileChooser.APPROVE_OPTION) return //user canceled
		        		splitFile openFileDialog.selectedFile 
	        		})
		            separator()
		            menuItem(text: "Exit", mnemonic: 'X', actionPerformed: {dispose() })
		        }
		        menu(text: "Port", mnemonic: 'P') {
		        	def sim = menuItem(text: "Simulator", mnemonic: 'S', actionPerformed: {
	        			it.source.background = Color.ORANGE
		        	})
		        	for (String portName : setter.getSerialPortNames()) {
			        	menuItem(text: portName, actionPerformed: {
		        			sim.background = it.source.background
		        			it.source.background = Color.ORANGE
		        			setter.setSerialPort(machine, portName)
			        	})
		        	}
		        }
		    }
		    mainPanel = panel {
		    	borderLayout()
		    	
		    	splitPane(dividerLocation:280) {
		    		panel(name: "gcodeFile", constraints: "left") {
	            		borderLayout()
	            		gcodeLabel = label(constraints: BL.NORTH, text: "gcode file name")
			            scrollPane() { 
		            		gcodeText = textPane(editable: false)
					    }
			    		panel( name: "buttons", constraints: BL.SOUTH) {
					    	startButton = button ( action: action(name: "Start", closure: {start() }))
					    	abortButton = button ( action: action(name: "Abort", closure: {abort() }))
					    	pauseButton = button ( action: action(name: "Pause", closure: {pauseOrResume() }))
					    	busyLED 	= label (text: "Busy")
					    	playingLED 	= label (text: "Playing")
				    	}
		    		}
		            scrollPane(constraints: "right") { 
		            	logPane = textPane() 
		            }
		        }

		    	panel(name: "immediate", constraints: BL.NORTH) {
		    		borderLayout()
		    		panel(constraints: BL.SOUTH) {
		    			borderLayout()
			    		def execAction = action(name: "Exec Immediate", closure: {execImmediate immediate.text })
			    		def immediate = textField(id: 'immediate')
			    		immediate.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), execAction)
			    		button ( constraints: BL.EAST, action: execAction)
		    		}
				    panel(name: "jogger", constraints: BL.CENTER) {
						positionModeIndicator 	= label (text: "Absolute")
						
			    		jogDistance = textField(id: 'jogDistance', text: ".001")

				    	panel(name: "xyJogger") {
				    		gridLayout(cols: 3, rows: 3)
					        ["NW", "N", "NE", 
					         "W", "", "E", 
					         "SW", "S", "SE"
					        ].each { l -> 
						        def jogAction = action(name: l.toString(), 
				    					closure: { jog l.toString() }
			    				)
						    	def b = button ( 
						    			id: l.toString(), 
						    			action: jogAction,
					    		)

						    	b.addMouseListener(
					    			new MouseAdapter()  {
					    				def Timer timer
					    				public void mousePressed(java.awt.event.MouseEvent e) {
					    					timer = new Timer(150, jogAction)
					    					timer.setInitialDelay(250)
					    					timer.start()
						    			};
					    				public void mouseReleased(java.awt.event.MouseEvent e) {
					    					timer.stop()
						    			};
					    			}
				    			)
					        }
				    	}
				    	panel(name: "zJogger") {
				    		gridLayout(cols: 1, rows: 2)
					        ["Up", 
					         "Down"
					        ].each { l -> 
						        def jogAction = action(name: l.toString(), 
				    					closure: { jog l.toString() }
			    				)
						    	def b = button ( 
						    			id: l.toString(), 
						    			action: jogAction,
					    		)
	
						    	b.addMouseListener(
					    			new MouseAdapter()  {
					    				def Timer timer
					    				public void mousePressed(java.awt.event.MouseEvent e) {
					    					timer = new Timer(150, jogAction)
					    					timer.setInitialDelay(250)
					    					timer.start()
						    			};
					    				public void mouseReleased(java.awt.event.MouseEvent e) {
					    					timer.stop()
						    			};
					    			}
				    			)
					        }
				    	}
				    }
		    	}
		    }
		}
	}

	void runStartupScript() {
		updateControlsFromMachine()
	}
	void runShutdownScript() {
		machine.shutdown()
	}
	public void log(String line) {
		if (logPane == null) {
			println("log: " + line);
			return;
		}
		logPane.caretPosition = logPane.document.getLength()
	    logPane.replaceSelection(line + "\n")
	}
	
	public void sentLine(String line) {
		log(line)
		updateControlsFromMachine()
	}
	public void receivedLine(String line) {
		log(line)
		updateControlsFromMachine()
	}
	public void startedPlaying(String fileName) {
		log("Started " + fileName)
		updateControlsFromMachine()
	}
	public void finishedPlaying(String fileName) {
		log("Finished " + fileName);
		updateControlsFromMachine()
	}
	public void abortedPlaying(String fileName) {
		log("Aborted " + fileName);
		updateControlsFromMachine()
	}
	public void pausedPlaying(String fileName) {
		log("Paused " + fileName);
		updateControlsFromMachine()
	}
	public void resumedPlaying(String fileName) {
		log("Resumed " + fileName);
		updateControlsFromMachine()
	}
	public void fileLoaded(String fileName, String fileContents) {
		gcodeLabel.text = fileName
    	gcodeText.text = fileContents
    	gcodeText.caretPosition = 1
    	updateControlsFromMachine()
		log("Loaded file: " + fileName + " " + fileContents.length() + " chars");
	}
	public void busy(boolean busy) {
		updateControlsFromMachine()
	}


	void execImmediate(s) {
		machine.execImmediate(s)
	}
	void jog(direction) {
		def distance = jogDistance.text
		machine.jog(distance, direction)
	}
	void start() {
		player.play()
	}
	void abort() {
		player.abort()
	}
	void pauseOrResume() {
    	if (player.isPaused()) {
    		player.resume()
    	} else {
    		player.pause()
    	}
	}
	void openFile(f) {
		player.openFile(f)
	}
	void reloadFile() {
		player.reloadFile()
	}

	void splitFile(f) {
		new DrillSplit().splitFile(f)
	}

	void updateControlsFromMachine() {
		if (startButton == null)
			return;
		startButton.enabled = (!player.playing && player.fileOpen)
    	pauseButton.enabled = player.playing
    	abortButton.enabled = player.playing
    	playingLED.enabled = player.playing
    	busyLED.enabled = machine.busy
		positionModeIndicator.text = (machine.positionAbsolute ? "Absolute" : "Relative")
    	if (player.paused) {
    		pauseButton.text = "Resume"
    	} else {
    		pauseButton.text = "Pause"
    	}
	}
}
