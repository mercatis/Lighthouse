package com.mercatis.lighthouse3.ui.branding.mercatis;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.splash.AbstractSplashHandler;

public class SplashHandler extends AbstractSplashHandler {

	@Override
	public void init(Shell splash) {
		splash.setBackgroundMode(SWT.INHERIT_DEFAULT);
		Label label = new Label(splash, SWT.RIGHT);
		label.setText("3.4.0");
		label.setFont(new Font(null, "Helvetica", 20, SWT.BOLD));
		label.setSize(350, 32);
		label.setLocation(90, 200);
		super.init(splash);
	}

}
