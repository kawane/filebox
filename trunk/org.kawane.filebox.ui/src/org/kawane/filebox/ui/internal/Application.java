package org.kawane.filebox.ui.internal;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.log.LogService;

public class Application implements IApplication {
	private static LogService logger = Activator.getInstance().getLogger();
	
	private Display display;
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		// this the way to retrieve command line option
		Object args = context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		display = Display.getDefault();
		logger.log(LogService.LOG_INFO, "Start file box ui");
		
		// our first window
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		Label label = new Label(shell, SWT.NONE);
		label.setText("File box hello wold!");
		shell.open();
		
		context.applicationRunning();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		logger.log(LogService.LOG_INFO, "Stop file box ui");
		return null;
	}

	@Override
	public void stop() {
		if(display != null && !display.isDisposed()) {
			display.dispose();
		}
	}

}