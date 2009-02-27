/**
 * Filebox developed by Kawane.
 * LGPL License.
 */
package org.kawane.filebox.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.kawane.filebox.core.Preferences;
import org.kawane.filebox.ui.internal.Application;
import org.kawane.filebox.ui.toolkit.ToolKit;

/**
 * 
 * Handles the global application menu.
 * @author Jean-Charles Roger
 *
 */
public class MenuManager {

	/** Parent application */
	protected final Application application;
	
	/** Toolkit used for dialogs */
	protected final ToolKit tk = new ToolKit();

	/** The handled menu bar  */
	protected Menu menuBar = null;

	/** FileBox menu action list */
	protected List<Action> fileBoxActions = null;
	
	public MenuManager(Application application) {
		this.application = application;
	}
	
	protected Preferences getPreferences() {
		return application.getFilebox().getPreferences();
	}
	
	/** Generic selection listener for MenuItems that have Actions as data.  */
	protected SelectionAdapter selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			// TODO handles toolitems
			if (e.getSource() instanceof MenuItem && ((MenuItem) e.getSource()).getData() instanceof Action) {
				Action action = (Action) ((MenuItem) e.getSource()).getData();
				// TODO handles end status
				action.run();
			}
		}
	};
	
	/** Creates the shell menu bar */
	public void createMenuBar(Shell shell) {

		menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);
		
		createMenu(shell, menuBar, "FileBox", getFileBoxActions());

	}

	/** Creates a cascaded {@link MenuItem} from a list of actions. */
	protected Menu createMenu(final Shell shell, final Menu bar, final String name, final List<Action> actions) {
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

	/** Populates menu with the given actions.  */
	protected void populateMenu(Menu menu, List<Action> actions) {
		for (Action oneAction : actions) {

			int visibility = oneAction.getVisibility();
			if (visibility != Action.VISIBILITY_HIDDEN ) {
				if (oneAction.hasStyle(Action.STYLE_SEPARATOR)) {
					new MenuItem(menu, SWT.SEPARATOR);
				} else {
					MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
					menuItem.setText(oneAction.getLabel());
					menuItem.setImage(oneAction.getImage());
					menuItem.setData(oneAction);
					menuItem.setEnabled(visibility == Action.VISIBILITY_ENABLE);
					menuItem.addSelectionListener(selectionListener);
				}
			}
		}
	}

	protected void depopulateMenu(Menu menu) {
		MenuItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) {
			items[i].dispose();
		}
	}

	
	public List<Action> getFileBoxActions() {
		if ( fileBoxActions == null ) {
			fileBoxActions = new ArrayList<Action>();
			
			
			fileBoxActions.add(new Action.Stub("About") {
				@Override
				public int run() {
					Shell dialog = tk.dialogShell(application.getActiveShell(), "About");
					tk.message(dialog, "FileBox version 1.0.");
					tk.message(dialog, "Copyrights Kawane 2009.");
					Button[] buttons = tk.buttons(dialog, "Ok");
					tk.computeSizes(dialog, 200);
					dialog.open();
					tk.waitSelectedButton(buttons);
					dialog.dispose();
					return STATUS_OK;
				}
			});
			
			fileBoxActions.add(new Action.Stub("Preferences") {
				@Override
				public int run() {
					Shell dialog = tk.dialogShell(application.getActiveShell(), "Preferences");
					String nameValue = getPreferences().getName();
					Text nameText = tk.textField(dialog, "Name:", nameValue == null ? "" : nameValue );
					Button[] buttons = tk.buttons(dialog, "Ok", "Cancel");
					dialog.setDefaultButton(buttons[0]);
					tk.computeSizes(dialog, 300);
					dialog.open();
					int result = tk.waitSelectedButton(buttons);
					if (result == 0) {
						getPreferences().setName(nameText.getText());
						getPreferences().saveProperties();
					}
					dialog.dispose();
					return result == 0 ? STATUS_OK : STATUS_CANCEL;
				}
			});
			
			fileBoxActions.add(new Action.Stub(Action.STYLE_SEPARATOR));
			
			fileBoxActions.add(new Action.Stub("Quit") {
				@Override
				public int run() {
					application.stop();
					return STATUS_OK;
				}
			});
			
		}
		return fileBoxActions;
	}
}
