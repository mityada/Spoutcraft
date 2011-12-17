/*
 * This file is part of Spoutcraft (http://wiki.getspout.org/).
 * 
 * Spoutcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Spoutcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.spoutcraft.client.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.spoutcraft.client.SpoutClient;
import org.spoutcraft.client.gui.*;
import org.spoutcraft.spoutcraftapi.gui.InGameHUD;
import org.spoutcraft.spoutcraftapi.gui.OverlayScreen;
import org.spoutcraft.spoutcraftapi.gui.PopupScreen;
import org.spoutcraft.spoutcraftapi.gui.Screen;
import org.spoutcraft.spoutcraftapi.gui.Widget;
import org.spoutcraft.spoutcraftapi.gui.WidgetType;

public class PacketWidget implements SpoutPacket {
	protected Widget widget;
	protected UUID screen;
	private static final int[] nags;
	
	static {
		nags = new int[WidgetType.getNumWidgetTypes()];
		for (int i = 0; i < WidgetType.getNumWidgetTypes(); i++) {
			nags[i] = CustomPacket.NAG_MSG_AMT;
		}
	}
	
	public PacketWidget() {

	}
	
	public PacketWidget(Widget widget, UUID screen) {
		this.widget = widget;
		this.screen = screen;
	}

	public int getNumBytes() {
		return (widget != null ? widget.getNumBytes() : 0) + 26;
	}

	public void readData(DataInputStream input) throws IOException {
		int id = input.readInt();
		long msb = input.readLong();
		long lsb = input.readLong();
		int size = input.readInt();
		int version = input.readShort();
		screen = new UUID(msb, lsb);
		WidgetType widgetType = WidgetType.getWidgetFromId(id);
		if (widgetType != null) {
			try {
				widget = widgetType.getWidgetClass().newInstance();
				if (widget.getVersion() == version) {
					widget.readData(input);
				}
				else {
					input.skipBytes(size);
					if (nags[id]-- > 0) {
						System.out.println("Received invalid widget: " + widgetType + " v: " + version + " current v: " + widget.getVersion());
					}
					widget = null;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public void writeData(DataOutputStream output) throws IOException {
		output.writeInt(widget.getType().getId());
		output.writeLong(screen.getMostSignificantBits());
		output.writeLong(screen.getLeastSignificantBits());
		output.writeInt(widget.getNumBytes());
		output.writeShort(widget.getVersion());
		widget.writeData(output);
	}

	public void run(int playerId) {
		if (widget != null) {
			InGameHUD mainScreen = SpoutClient.getInstance().getActivePlayer().getMainScreen();
			PopupScreen popup = mainScreen.getActivePopup();
			Screen overlay = null;
			if(SpoutClient.getHandle().currentScreen != null) { 
				overlay = SpoutClient.getHandle().currentScreen.getScreen();
			}
			//Determine if this is a popup screen and if we need to update it
			if (widget instanceof PopupScreen) {
				if (popup != null){
					if (widget.getId().equals(popup)) {
						if (SpoutClient.getHandle().currentScreen instanceof CustomScreen) {
							((CustomScreen)SpoutClient.getHandle().currentScreen).update((PopupScreen)widget);
						}
					}
				}
				else {
					mainScreen.attachPopupScreen((PopupScreen)widget);
				}
			}
			//Determine if this screen overrides another screen
			else if(widget instanceof OverlayScreen) {
				if (SpoutClient.getHandle().currentScreen != null) {
					SpoutClient.getHandle().currentScreen.update((OverlayScreen)widget);
					overlay = (OverlayScreen)widget;
				}
			}
			//Determine if this is a widget on the main screen
			else if (screen.equals(mainScreen.getId())) {
				if (mainScreen.containsWidget(widget.getId())) {
					mainScreen.updateWidget(widget);
					widget.setScreen(mainScreen);
				}
				else {
					widget.setScreen(mainScreen);
					mainScreen.attachWidget(widget.getAddon(), widget);
				}
			}
			//Determine if this is a widget on the popup screen
			else if (popup != null && screen.equals(popup.getId())) {
				if (popup.containsWidget(widget.getId())) {
					popup.updateWidget(widget);
					widget.setScreen(popup);
				}
				else {
					widget.setScreen(popup);
					popup.attachWidget(widget.getAddon(), widget);
				}
			} 
			//Determine if this is a widget on an overlay screen
			else if (overlay != null && screen.equals(overlay.getId())){
				if(overlay.containsWidget(widget.getId())){
					overlay.updateWidget(widget);
					widget.setScreen(overlay);
				} else {
					widget.setScreen(overlay);
					overlay.attachWidget(widget.getAddon(), widget);
				}
			}
		}
	}

	public PacketType getPacketType() {
		return PacketType.PacketWidget;
	}
	
	public int getVersion() {
		return 1;
	}

	public void failure(int playerId) {
		
	}
}
