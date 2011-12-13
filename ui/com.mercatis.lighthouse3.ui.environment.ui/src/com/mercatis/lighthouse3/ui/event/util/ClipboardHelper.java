package com.mercatis.lighthouse3.ui.event.util;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

public class ClipboardHelper {
	public static void copyToClipboard(Display display, String text) {
		Clipboard clipboard = new Clipboard(display);
		TextTransfer textTransfer = TextTransfer.getInstance();
		clipboard.setContents(new String[] { text }, new Transfer[] { textTransfer });
		clipboard.dispose();
	}
}
