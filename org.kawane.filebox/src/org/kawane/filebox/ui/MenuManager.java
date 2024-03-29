/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;
import org.kawane.filebox.Resources;
import org.kawane.filebox.core.DistantFilebox;
import org.kawane.filebox.core.FileboxApplication;
import org.kawane.filebox.core.Globals;
import org.kawane.filebox.core.Preferences;
import org.kawane.filebox.network.http.TransferMonitor;
import org.kawane.filebox.ui.toolkit.ToolKit;

/**
 *
 * Handles the global application menu.
 * @author Jean-Charles Roger
 *
 */
public class MenuManager {

	private static Logger logger = Logger.getLogger(MenuManager.class.getName());

	/** Shared resources instances. */
	protected Resources resources = Resources.getInstance();

	/** Toolkit used for dialogs */
	protected final ToolKit tk = new ToolKit();

	/** The handled menu bar  */
	protected Menu menuBar = null;

	protected IAction quitAction = null;
	protected IAction showHideAction = null;


	/** FileBox system tray menu action list */
	protected List<IAction> systemTrayActions = null;

	/** FileBox menu action list */
	protected List<IAction> fileBoxActions = null;

	/** Tools menu action list */
	protected List<IAction> toolsActions = null;
	
	/** Window menu action list */
	protected List<IAction> windowActions = null;
	
	/** Files menu action list */
	protected List<IAction> filesActions = null;
	
	private FileboxApplication application;

	public MenuManager() {
		setApplication(Globals.getFileboxShell());
	}
	
	public void setApplication(FileboxApplication application) {
		this.application = application;
	}

	/** Generic selection listener for MenuItems that have Actions as data.  */
	protected SelectionAdapter selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			// TODO handles toolitems
			if (e.getSource() instanceof MenuItem && ((MenuItem) e.getSource()).getData() instanceof IAction) {
				IAction action = (IAction) ((MenuItem) e.getSource()).getData();
				// TODO handles end status
				action.run();
			}
		}
	};

	/** Creates the shell menu bar */
	public void createMenuBar(Shell shell) {
		menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);
		createMenu(shell, menuBar, "FileBox", getFileBoxActions(shell));
		createMenu(shell, menuBar, "Tools", getToolsActions(shell));
		createMenu(shell, menuBar, "Window", getWindowActions(shell));
	}

	/** Creates the system tray (if available) */
	public void createSystemTray(final Shell shell) {
		final Tray tray = application.getDisplay().getSystemTray();
		if (tray == null) {
			logger.log(Level.WARNING, "The system tray is not available");
		} else {
			final TrayItem item = new TrayItem(tray, SWT.NONE);
			final Menu menu = createPopupMenu(shell, getSystemTrayActions(shell));

			item.setImage(resources.getImage("filebox-tray.png"));
			item.setToolTipText("Filebox");
			Listener itemListener = new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
					case SWT.MenuDetect:
						menu.setVisible(true);
						break;
					case SWT.Dispose:
						item.removeListener(SWT.MenuDetect, this);
						item.removeListener(SWT.Dispose, this);
						break;
					}
				}
			};
			item.addListener(SWT.Dispose, itemListener);
			item.addListener(SWT.MenuDetect, itemListener);
			
			if ( Utils.isWindows() ) {
				Listener windowsItemListerner = new Listener() {
					public void handleEvent(Event event) {
						switch (event.type) {
						case SWT.DefaultSelection:
							IAction action = getShowHideAction();
							if ( action.getVisibility() == IAction.VISIBILITY_ENABLE ) action.run();
							break;
						case SWT.Dispose:
							item.removeListener(SWT.DefaultSelection, this);
							item.removeListener(SWT.Dispose, this);
						}
					}
				};
				item.addListener(SWT.DefaultSelection, windowsItemListerner);
				item.addListener(SWT.Dispose, windowsItemListerner);
			}
		}
	}


	/** Creates a cascaded {@link MenuItem} in a SWT.BAR menu from a list of actions. */
	protected Menu createMenu(final Shell shell, final Menu bar, final String name, final List<IAction> actions) {
		MenuItem menuItem = new MenuItem(bar, SWT.CASCADE);
		menuItem.setText(name);

		final Menu menu = new Menu(shell, SWT.DROP_DOWN);
		menuItem.setMenu(menu);

		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				depopulateMenu(menu);
				populateMenu(menu, actions);
			}
		});

		return menu;
	}

	/** Creates a popup menu from a list of actions */
	public Menu createPopupMenu(final Control control, final List<IAction> actions) {
		final Menu menu = new Menu(control);
		Listener listener = new Listener() {
			
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Show:
					depopulateMenu(menu);
					populateMenu(menu, actions);
					break;
				case SWT.Dispose:
					menu.removeListener(SWT.Show, this);
					menu.removeListener(SWT.Dispose, this);
					break;
				}
				
			}
		};
		menu.addListener(SWT.Show, listener);
		menu.addListener(SWT.Dispose, listener);
		
		Listener controlListener = new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.MenuDetect:
					menu.setVisible(true);
					break;
				case SWT.Dispose:
					control.removeListener(SWT.MenuDetect, this);
					control.removeListener(SWT.Dispose, this);
					break;
				}
			}
		};
		control.addListener(SWT.MenuDetect, controlListener);
		control.addListener(SWT.Dispose, controlListener);
		return menu;
	}

	/** Populates menu with the given actions.  */
	protected void populateMenu(Menu menu, List<IAction> actions) {
		for (IAction oneAction : actions) {

			int visibility = oneAction.getVisibility();
			if (visibility != IAction.VISIBILITY_HIDDEN ) {
				if (oneAction.hasStyle(IAction.STYLE_SEPARATOR)) {
					new MenuItem(menu, SWT.SEPARATOR);
				} else {
					MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
					menuItem.setText(oneAction.getLabel());
					menuItem.setImage(oneAction.getImage());
					menuItem.setData(oneAction);
					menuItem.setEnabled(visibility == IAction.VISIBILITY_ENABLE);
					menuItem.addSelectionListener(selectionListener);
					if ( oneAction.hasStyle(IAction.STYLE_DEFAULTACTION)) menu.setDefaultItem(menuItem);
				}
			}
		}
		
		if ( menu.getItemCount() == 0 ) {
			MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
			menuItem.setText("None");
			menuItem.setEnabled(false);
		}
	}

	protected void depopulateMenu(Menu menu) {
		MenuItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) {
			items[i].dispose();
		}
	}

	public IAction getQuitAction(final Shell shell) {
		if ( quitAction == null ) {
			quitAction = new IAction.Stub("Quit") {
				@Override
				public int run() {
					application.stop();
					return STATUS_OK;
				}
			};
		}
		return quitAction;
	}

	public IAction getShowHideAction() {
		if ( showHideAction == null ) {
			showHideAction = new IAction.Stub(IAction.STYLE_DEFAULTACTION) {

				@Override
				public String getLabel() {
					return application.getContactShell().isVisible() ? "Hide" : "Show";
				}

				@Override
				public int run() {
					// Show hide filebox
					Shell shell = application.getContactShell();
					boolean visible = !shell.getVisible();
					shell.setVisible(visible);
					shell.forceActive();
					return STATUS_OK;
				}
			};
		}
		return showHideAction;
	}

	public List<IAction> getFileBoxActions(final Shell shell) {
		if ( fileBoxActions == null ) {
			fileBoxActions = new ArrayList<IAction>();


			fileBoxActions.add(new IAction.Stub("About") {
				@Override
				public int run() {
					Shell dialog = tk.dialogShell(application.getActiveShell(), "About");
					File about = new File("about.txt");
					String aboutMessage = getContent(about);
					if(aboutMessage != null && aboutMessage.length() > 0) {
						tk.message(dialog, aboutMessage);
					} else {
						tk.message(dialog, "FileBox Development version");
						tk.message(dialog, "Copyrights Kawane "+ Calendar.getInstance().get(Calendar.YEAR)+".");
					}
					Button[] buttons = tk.buttons(dialog, "Ok");
					tk.computeSizes(dialog, 200);
					dialog.open();
					tk.waitSelectedButton(buttons);
					dialog.dispose();
					return STATUS_OK;
				}
			});

			fileBoxActions.add(new IAction.Stub("Preferences") {
				@Override
				public int run() {
					Preferences preferences = application.getPreferences();
					Shell dialog = tk.dialogShell(application.getActiveShell(), "Preferences");
					Text nameText = tk.textField(dialog, "Name:", preferences.getName() );
					Text portText = tk.textField(dialog, "Port:", Integer.toString(preferences.getPort()) );
					Text publicDirText = tk.fileField(dialog, "Public directory:", preferences.getPublicDir());
					Button[] buttons = tk.buttons(dialog, "Ok", "Cancel");
					dialog.setDefaultButton(buttons[0]);
					tk.computeSizes(dialog, 300);
					dialog.open();
					int result = tk.waitSelectedButton(buttons);
					if (result == 0) {
						preferences.setName(nameText.getText());
						try {
							preferences.setPort(Integer.valueOf(portText.getText()));
						} catch (NumberFormatException e) { /* do nothing */ }
						preferences.setPublicDir(publicDirText.getText());
						preferences.saveProperties();
					}
					dialog.dispose();
					return result == 0 ? STATUS_OK : STATUS_CANCEL;
				}
			});
			fileBoxActions.add(new IAction.Stub(IAction.STYLE_SEPARATOR));
			fileBoxActions.add(getQuitAction(shell));

		}
		return fileBoxActions;
	}

	protected String getContent(File file) {
		if(!file.exists()) return null;
		StringBuilder b = new StringBuilder();
		try {
			InputStreamReader in = new InputStreamReader(new FileInputStream(file), "UTF-8");
			char[] cbuf = new char[8192];
			int read = in.read(cbuf);
			while(read>=0) {
				b.append(cbuf, 0, read);
				read = in.read(cbuf);
			}
			in.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b.toString();
	}

	public List<IAction> getToolsActions(final Shell shell) {
		if ( toolsActions == null ) {
			toolsActions = new ArrayList<IAction>();

			toolsActions.add(new IAction.Stub("Open Browser\u2026") {
				
				@Override
				public int getVisibility() {
					return application.getContactController().getSelectedFilebox() == null ? VISIBILITY_DISABLE : VISIBILITY_ENABLE;
				}
				
				@Override
				public int run() {
					Shell dialog = new Shell(Display.getCurrent(), SWT.DIALOG_TRIM |SWT.RESIZE);
					dialog.setText( "Browser");
					dialog.setImage(application.getActiveShell().getImage());
					dialog.setLayout(new FillLayout());
					
					Browser browser = new Browser(dialog, SWT.NONE);

					DistantFilebox selectedFilebox = application.getContactController().getSelectedFilebox();
					StringBuilder url = new StringBuilder();
					url.append("http://");
					url.append(selectedFilebox.getHost());
					url.append(":");
					url.append(selectedFilebox.getPort());
					browser.setUrl(url.toString());
					
					dialog.open();
					return STATUS_OK;
				}
			});
		}
		return toolsActions;
	}
	
	
	public List<IAction> getWindowActions(final Shell shell) {
		if ( windowActions == null ) {
			windowActions = new ArrayList<IAction>();
		}
		return windowActions;
	}

	public List<IAction> getSystemTrayActions(final Shell shell) {
		if ( systemTrayActions == null ) {
			systemTrayActions = new ArrayList<IAction>();
			systemTrayActions.add(getShowHideAction());
			systemTrayActions.add(getQuitAction(shell));
		}
		return systemTrayActions;
	}
	
	public List<IAction> getFilesActions(final Shell shell) {
		if ( filesActions == null ) {
			filesActions = new ArrayList<IAction>();
			
			filesActions.add(new IAction.Stub("Copy URL to clipboard") {
				@Override
				public int run() {
					try {
						Clipboard clipboard = new Clipboard(application.getDisplay());
						FileDescriptor file = application.getContactController().getSelectedFile();
						if ( file != null ) {
							String url = file.getCompleteURL();
							clipboard.setContents(new Object[]{url,url}, new Transfer[]{URLTransfer.getInstance(), TextTransfer.getInstance()});
						}
					} catch (Throwable ex) {
						ex.printStackTrace();
					}
					return STATUS_OK;
				}
			});

			filesActions.add(new IAction.Stub("Save in specific folder\u2026") {
				
				@Override
				public int getVisibility() {
					FileDescriptor file = application.getContactController().getSelectedFile();
					if ( file == null) return VISIBILITY_HIDDEN;
					return file.isDirectory() ? VISIBILITY_HIDDEN : VISIBILITY_ENABLE;
				}
				
				@Override
				public int run() {
					DirectoryDialog dialog = new DirectoryDialog(application.getContactShell());
					dialog.setMessage("Choose directory where to save file");
					dialog.setText("Filebox");
					String directory = dialog.open();
					if  ( directory != null ) {
						FileDescriptor file = application.getContactController().getSelectedFile();
						File destinationFile = new File(directory, file.getName());
						TransferMonitor monitor = application.getContactController().getTransferController().getTransferMonitor();
						application.getTransferManager().startDownload(file.getFilebox(), file.getPathURL(), destinationFile, monitor);
					}
					
					return STATUS_OK;
				}
			});
			
			filesActions.add(new IAction.Stub("Save as zip") {
				
				@Override
				public int getVisibility() {
					FileDescriptor file = application.getContactController().getSelectedFile();
					if ( file == null) return VISIBILITY_HIDDEN;
					return file.isDirectory() ? VISIBILITY_HIDDEN : VISIBILITY_HIDDEN;
				}
				
				@Override
				public int run() {
//					FileDescriptor file = application.getContactController().getSelectedFile();
					
					return STATUS_OK;
				}
			});
		}
		return filesActions;
	}
	
	
	
}
