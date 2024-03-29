package org.kawane.filebox.ui.internal;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.kawane.filebox.core.Filebox;
import org.kawane.filebox.ui.FileboxMainComposite;
import org.kawane.services.Service;

import static org.kawane.services.advanced.ServiceRegistry.*;

@Service(UIFileboxApplication.class)
public class Application implements UIFileboxApplication {

	private static Logger logger = Logger.getLogger(Application.class.getName());

	/** Shared resources instances. */
	protected Resources resources;

	protected Filebox filebox;

	private Display display;
	private FileboxMainComposite composite;

	/* (non-Javadoc)
	 * @see org.kawane.filebox.ui.internal.UIFileboxApplication#getDisplay()
	 */
	public Display getDisplay() {
		return display;
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.ui.internal.UIFileboxApplication#getActiveShell()
	 */
	public Shell getActiveShell() {
		return display.getActiveShell();
	}

	public void start() {
		register(this);

		display = Display.getDefault();
		logger.log(Level.FINE, "Start file box ui");
		resources = Resources.getInstance();

		// our first window
		final Shell shell = new Shell(display);
		shell.setImage(resources.getImage("filebox.png"));
		shell.setLayout(new FillLayout());
		shell.setSize(300, 300);
		shell.setText("FileBox");
		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				boolean visible = !shell.isVisible();
				shell.setVisible(visible);

				// do not quit the application when closing the shell
				event.doit = false;
			}
		});

		MenuManager menuManager = new MenuManager();
		manage(menuManager);
		menuManager.createMenuBar(shell);
		menuManager.createSystemTray(shell);

		composite = new FileboxMainComposite(shell, SWT.NONE);
		manage(composite);

		shell.open();
		while (!shell.isDisposed()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "Internal Error", e);
			}
		}
		if (!display.isDisposed()) {
			display.dispose();
		}

		resources.dispose();
		logger.log(Level.FINE, "Stop file box ui");
	}

	/* (non-Javadoc)
	 * @see org.kawane.filebox.ui.internal.UIFileboxApplication#stop()
	 */
	public void stop() {
		if (display != null && !display.isDisposed()) {
			display.dispose();
		}
	}

}
