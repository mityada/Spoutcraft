package org.spoutcraft.client.gui.database;

import net.minecraft.src.FontRenderer;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.spoutcraft.client.SpoutClient;
import org.spoutcraft.client.gui.MCRenderDelegate;
import org.spoutcraft.client.io.CustomTextureManager;
import org.spoutcraft.spoutcraftapi.event.screen.ButtonClickEvent;
import org.spoutcraft.spoutcraftapi.gui.Color;
import org.spoutcraft.spoutcraftapi.gui.GenericRadioButton;
import org.spoutcraft.spoutcraftapi.gui.RadioButton;

public class SortButton extends GenericRadioButton implements UrlElement {
	boolean topdown = true;
	boolean preferredOrder = true;
	boolean allowSorting = true;
	boolean firstClick = false;
	
	protected AbstractAPIModel model;
	String url;
	
	public SortButton(String text, String urlPart, AbstractAPIModel model) {
		super(text);
		url = urlPart;
		this.model = model;
	}
	
	public SortButton(String text, String baseUrl, boolean preferredOrder, AbstractAPIModel model) {
		super(text);
		url = baseUrl;
		this.preferredOrder = preferredOrder;
		this.model = model;
	}

	@Override
	public void onButtonClick(ButtonClickEvent event) {
		if(isSelected() && !firstClick) {
			topdown = !topdown;
		}
		updateUrl();
		firstClick = false;
	}

	@Override
	public RadioButton setSelected(boolean b) {
		if(!isSelected() && b) {
			topdown = preferredOrder;
			firstClick = true;
		}
		super.setSelected(b);
		return this;
	}
	
	private void updateUrl() {
		model.updateUrl();
	}
	
	public void setAllowSorting(boolean b) {
		allowSorting = b;
	}
	
	@Override
	public void render() {
		if(!allowSorting) {
			super.render();
		} else {
			MCRenderDelegate r = (MCRenderDelegate) SpoutClient.getInstance().getRenderDelegate();
			String texture ="";
			if(isSelected()&&topdown||!isSelected()&&preferredOrder) texture = "ascending.png";
			else texture = "descending.png";
			Texture direction = CustomTextureManager.getTextureFromJar("/res/"+texture);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glTranslatef((float) getScreenX(), (float) getScreenY(), 0);
			r.renderBaseBox(this, true);
			FontRenderer font = SpoutClient.getHandle().fontRenderer;
			Color color = r.getColor(this);
			if(!isSelected()){
				color.setAlpha(0.2F);
			}
			r.drawTexture(direction, 20, 20, color, true);
			font.drawString(getText(), 22, 7, r.getColor(this).toInt());
		}
	}

	public boolean isActive() {
		return isSelected();
	}

	public String getUrlPart() {
		String dir = topdown?"asc":"desc";
		String surl = url + (allowSorting?"&order="+dir:"");
		return surl;
	}

	public void clear() {
		setSelected(false);
	}
}
