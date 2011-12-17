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
package net.minecraft.src;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;
import org.spoutcraft.client.SpoutClient;
import org.spoutcraft.client.chunkcache.ChunkCache;
import org.spoutcraft.client.config.ConfigReader;
import org.spoutcraft.client.player.ChatManager;
import org.spoutcraft.spoutcraftapi.gui.ChatTextBox;
import org.spoutcraft.spoutcraftapi.gui.InGameHUD;
import org.spoutcraft.spoutcraftapi.gui.ServerPlayerList;

public class GuiIngame extends Gui{

	private static RenderItem itemRenderer = new RenderItem();
	//Spout Improved Chat Start
	//Increased default size, efficiency reasons
	public List<ChatLine> chatMessageList = new ArrayList<ChatLine>(2500);
	//Spout Improved Chat End
	public static final Random rand = new Random(); //Spout private -> public static final
	private Minecraft mc;
	public String field_933_a;
	private int updateCounter;
	private String recordPlaying;
	private int recordPlayingUpFor;
	private boolean recordIsPlaying;
	public float damageGuiPartialTime;
	float prevVignetteBrightness;

	public GuiIngame(Minecraft minecraft)
	{
		chatMessageList = new ArrayList();
		//rand = new Random();	//Spout removed
		field_933_a = null;
		updateCounter = 0;
		recordPlaying = "";
		recordPlayingUpFor = 0;
		recordIsPlaying = false;
		prevVignetteBrightness = 1.0F;
		mc = minecraft;
	}

	//Spout Start
	//Most of function rewritten
	public void renderGameOverlay(float f, boolean flag, int i, int j)
	{
		SpoutClient.getInstance().onTick();
		InGameHUD mainScreen = SpoutClient.getInstance().getActivePlayer().getMainScreen();

		ScaledResolution scaledRes = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
		int screenWidth = scaledRes.getScaledWidth();
		int screenHeight = scaledRes.getScaledHeight();
		FontRenderer font = this.mc.fontRenderer;
		this.mc.entityRenderer.setupOverlayRendering();
		GL11.glEnable(3042 /*GL_BLEND*/);
		if(Minecraft.isFancyGraphicsEnabled()) {
			this.renderVignette(this.mc.thePlayer.getEntityBrightness(f), screenWidth, screenHeight);
		}

		ItemStack helmet = this.mc.thePlayer.inventory.armorItemInSlot(3);
		if(this.mc.gameSettings.thirdPersonView == 0 && helmet != null && helmet.itemID == Block.pumpkin.blockID) {
			this.renderPumpkinBlur(screenWidth, screenHeight);
		}

		float var10 = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * f;
		if(var10 > 0.0F) {
			this.renderPortalOverlay(var10, screenWidth, screenHeight);
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, this.mc.renderEngine.getTexture("/gui/gui.png"));
		InventoryPlayer var11 = this.mc.thePlayer.inventory;
		this.zLevel = -90.0F;
		this.drawTexturedModalRect(screenWidth / 2 - 91, screenHeight - 22, 0, 0, 182, 22);
		this.drawTexturedModalRect(screenWidth / 2 - 91 - 1 + var11.currentItem * 20, screenHeight - 22 - 1, 0, 22, 24, 22);
		GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, this.mc.renderEngine.getTexture("/gui/icons.png"));
		GL11.glEnable(3042 /* GL_BLEND */);
		GL11.glBlendFunc(775, 769);
		this.drawTexturedModalRect(screenWidth / 2 - 7, screenHeight / 2 - 7, 0, 0, 16, 16);
		GL11.glDisable(3042 /* GL_BLEND */);

		GuiIngame.rand.setSeed((long) (this.updateCounter * 312871));
		int var15;
		int var17;
		
		//better safe than sorry
		SpoutClient.enableSandbox();

		// Hunger Bar Begin
		mainScreen.getHungerBar().render();
		// Hunger Bar End

		// Armor Bar Begin
		mainScreen.getArmorBar().render();
		// Armor Bar End

		// Health Bar Begin
		mainScreen.getHealthBar().render();
		// Health Bar End

		// Bubble Bar Begin
		mainScreen.getBubbleBar().render();
		// Bubble Bar End

		// Exp Bar Begin
		mainScreen.getExpBar().render();
		// Exp Bar End
		
		SpoutClient.disableSandbox();

		GL11.glDisable(3042 /* GL_BLEND */);
		GL11.glEnable('\u803a');
		GL11.glPushMatrix();
		GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GL11.glPopMatrix();

		for (var15 = 0; var15 < 9; ++var15) {
			int x = screenWidth / 2 - 90 + var15 * 20 + 2;
			var17 = screenHeight - 16 - 3;
			this.renderInventorySlot(var15, x, var17, f);
		}
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable('\u803a');

		if (this.mc.thePlayer.getSleepTimer() > 0) {
			GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
			GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
			var15 = this.mc.thePlayer.getSleepTimer();
			float var26 = (float)var15 / 100.0F;
			if(var26 > 1.0F) {
				var26 = 1.0F - (float)(var15 - 100) / 10.0F;
			}

			var17 = (int)(220.0F * var26) << 24 | 1052704;
			this.drawRect(0, 0, screenWidth, screenHeight, var17);
			GL11.glEnable(3008 /*GL_ALPHA_TEST*/);
			GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
		}
		
		SpoutClient.enableSandbox();
		mainScreen.render();
		SpoutClient.disableSandbox();
		
		String var23;
		if(this.mc.gameSettings.showDebugInfo) {
			GL11.glPushMatrix();
			if(Minecraft.hasPaidCheckTime > 0L) {
				GL11.glTranslatef(0.0F, 32.0F, 0.0F);
			}
			if (ConfigReader.fastDebug != 2) {
				font.drawStringWithShadow("Minecraft 1.0.0 (" + this.mc.debug + ")", 2, 2, 16777215);
				font.drawStringWithShadow(this.mc.debugInfoRenders(), 2, 12, 16777215);
				font.drawStringWithShadow(this.mc.func_6262_n(), 2, 22, 16777215);
				font.drawStringWithShadow(this.mc.debugInfoEntities(), 2, 32, 16777215);
				font.drawStringWithShadow(this.mc.func_21002_o(), 2, 42, 16777215);
				long maxMem = Runtime.getRuntime().maxMemory();
				long totalMem = Runtime.getRuntime().totalMemory();
				long freeMem = Runtime.getRuntime().freeMemory();
				long usedMem = (totalMem - freeMem);
				String os = System.getProperty("os.name");
				var23 = "Used memory: " + ((int)((usedMem / (float)totalMem) * 100f)) + "% (" + usedMem / 1024L / 1024L + "MB) of " + totalMem / 1024L / 1024L + "MB";
				this.drawString(font, var23, screenWidth - font.getStringWidth(var23) - 2, 2, 14737632);
				var23 = "Allocated memory: " + totalMem * 100L / maxMem + "% (" + totalMem / 1024L / 1024L + "MB) of " + maxMem / 1024L / 1024L + "MB";
				this.drawString(font, var23, screenWidth - font.getStringWidth(var23) - 2, 12, 14737632);
				//No Cheating!
				int offset = 0;
				if (SpoutClient.getInstance().isCoordsCheat()) {
					this.drawString(font, "x: " + this.mc.thePlayer.posX, 2, 64, 14737632);
					this.drawString(font, "y: " + this.mc.thePlayer.posY, 2, 72, 14737632);
					this.drawString(font, "z: " + this.mc.thePlayer.posZ, 2, 80, 14737632);
					this.drawString(font, "f: " + (MathHelper.floor_double((double)(this.mc.thePlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3), 2, 88, 14737632);
					offset = 40;
				}
				if (mc.isMultiplayerWorld() && SpoutClient.getInstance().isSpoutEnabled()) {
					this.drawString(font, "Spout Map Data Cache Info:", 2, 64 + offset, 0xE0E000);
					this.drawString(font, "Average packet size: " + ChunkCache.averageChunkSize.get() + " bytes", 2, 72 + offset, 14737632);
					this.drawString(font, "Cache hit percent: " + ChunkCache.hitPercentage.get(), 2, 80 + offset, 14737632);
					long currentTime = System.currentTimeMillis();
					long downBandwidth = (8 * ChunkCache.totalPacketDown.get()) / (currentTime - ChunkCache.loggingStart.get());
					long upBandwidth = (8 * ChunkCache.totalPacketUp.get()) / (currentTime - ChunkCache.loggingStart.get());
					this.drawString(font, "Bandwidth (Up): " + Math.max(1, upBandwidth) + "kbps", 2, 88 + offset, 14737632);
					this.drawString(font, "Bandwidth (Down): " + Math.max(1, downBandwidth) + "kbps", 2, 96 + offset, 14737632);
				}
			}
			else {
				font.drawStringWithShadow(Integer.toString(Minecraft.framesPerSecond), 4, 2, 0xFFE303);
			}
			GL11.glPopMatrix();
		}

		if(this.recordPlayingUpFor > 0) {
			float var24 = (float)this.recordPlayingUpFor - f;
			int fontColor = (int)(var24 * 256.0F / 20.0F);
			if(fontColor > 255) {
				fontColor = 255;
			}

			if(fontColor > 0) {
				GL11.glPushMatrix();
				GL11.glTranslatef((float)(screenWidth / 2), (float)(screenHeight - 48), 0.0F);
				GL11.glEnable(3042 /*GL_BLEND*/);
				GL11.glBlendFunc(770, 771);
				var17 = 16777215;
				if(this.recordIsPlaying) {
					var17 = Color.HSBtoRGB(var24 / 50.0F, 0.7F, 0.6F) & 16777215;
				}

				font.drawString(this.recordPlaying, -font.getStringWidth(this.recordPlaying) / 2, -4, var17 + (fontColor << 24));
				GL11.glDisable(3042 /*GL_BLEND*/);
				GL11.glPopMatrix();
			}
		}

		SpoutClient.enableSandbox();
		boolean chatOpen = mainScreen.getChatBar().isVisible() && mc.currentScreen instanceof GuiChat;
		int lines = chatOpen ? mainScreen.getChatTextBox().getNumVisibleChatLines() : mainScreen.getChatTextBox().getNumVisibleLines();

		GL11.glEnable(3042 /*GL_BLEND*/);
		GL11.glBlendFunc(770, 771);
		GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, (float)(screenHeight - 48), 0.0F);
		
		ChatTextBox chatTextWidget = mainScreen.getChatTextBox();
		if (chatTextWidget.isVisible()) {
			int viewedLine = 0;
			for (int line = SpoutClient.getInstance().getChatManager().chatScroll; line < Math.min(chatMessageList.size(), (lines + SpoutClient.getInstance().getChatManager().chatScroll)); line++) {
				if (chatOpen || chatMessageList.get(line).updateCounter < chatTextWidget.getFadeoutTicks()) {
					double opacity = 1.0D - chatMessageList.get(line).updateCounter / (double)chatTextWidget.getFadeoutTicks();
					opacity *= 10D;
					if(opacity < 0.0D) {
						opacity = 0.0D;
					}
					if(opacity > 1.0D) {
						opacity = 1.0D;
					}
					opacity *= opacity;
					int color = chatOpen ? 255 : (int)(255D * opacity);
					if (color > 0) {
						int x = 2;
						int y = -viewedLine * 9;
						String chat = chatMessageList.get(line).message;
						chat = SpoutClient.getInstance().getChatManager().formatChatColors(chat);
						chat = ChatManager.formatUrl(chat);
						//TODO add support for opening URL in browser if clicked?
						drawRect(x, y - 1, x + 320, y + 8, color / 2 << 24);
						GL11.glEnable(3042 /*GL_BLEND*/);
						font.drawStringWithShadow(chat, x, y, 0xffffff + (color << 24));
					}
					viewedLine++;
				}
			}
		}
		SpoutClient.disableSandbox();

		GL11.glPopMatrix();
		
		ServerPlayerList playerList = mainScreen.getServerPlayerList();
		if(this.mc.thePlayer instanceof EntityClientPlayerMP && this.mc.gameSettings.keyBindPlayerList.pressed && playerList.isVisible()) {
			NetClientHandler var41 = ((EntityClientPlayerMP)this.mc.thePlayer).sendQueue;
			List var44 = var41.field_35786_c;
			int var40 = var41.field_35785_d;
			int var38 = var40;
			int var16;
			for(var16 = 1; var38 > 20; var38 = (var40 + var16 - 1) / var16) {
				++var16;
			}

			var17 = 300 / var16;
			if(var17 > 150) {
				var17 = 150;
			}

			int var18 = (screenWidth - var16 * var17) / 2;
			byte var46 = 10;
			this.drawRect(var18 - 1, var46 - 1, var18 + var17 * var16, var46 + 9 * var38, Integer.MIN_VALUE);

			for(int var20 = 0; var20 < var40; ++var20) {
				int var47 = var18 + var20 % var16 * var17;
				int var22 = var46 + var20 / var16 * 9;
				this.drawRect(var47, var22, var47 + var17 - 1, var22 + 8, 553648127);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				GL11.glEnable(3008 /*GL_ALPHA_TEST*/);
				if(var20 < var44.size()) {
					GuiSavingLevelString var50 = (GuiSavingLevelString)var44.get(var20);
					font.drawStringWithShadow(var50.field_35624_a, var47, var22, 16777215);
					this.mc.renderEngine.bindTexture(this.mc.renderEngine.getTexture("/gui/icons.png"));
					boolean var48 = false;
					boolean var53 = false;
					byte var49 = 0;
					var53 = false;
					byte var54;
					if(var50.field_35623_b < 0) {
						var54 = 5;
					} else if(var50.field_35623_b < 150) {
						var54 = 0;
					} else if(var50.field_35623_b < 300) {
						var54 = 1;
					} else if(var50.field_35623_b < 600) {
						var54 = 2;
					} else if(var50.field_35623_b < 1000) {
						var54 = 3;
					} else {
						var54 = 4;
					}

					this.zLevel += 100.0F;
					this.drawTexturedModalRect(var47 + var17 - 12, var22, 0 + var49 * 10, 176 + var54 * 8, 10, 8);
					this.zLevel -= 100.0F;
				}
			}
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(2896 /*GL_LIGHTING*/);
		GL11.glEnable(3008 /*GL_ALPHA_TEST*/);
		GL11.glDisable(3042 /*GL_BLEND*/);
	}

	private void func_41039_c()
	{
		if(RenderDragon.field_41038_a == null)
		{
			return;
		}
		EntityDragon entitydragon = RenderDragon.field_41038_a;
		RenderDragon.field_41038_a = null;
		FontRenderer fontrenderer = mc.fontRenderer;
		ScaledResolution scaledresolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
		int i = scaledresolution.getScaledWidth();
		char c = '\266';
		int j = i / 2 - c / 2;
		int k = (int)(((float)entitydragon.func_41010_ax() / (float)entitydragon.getMaxHealth()) * (float)(c + 1));
		byte byte0 = 12;
		drawTexturedModalRect(j, byte0, 0, 74, c, 5);
		drawTexturedModalRect(j, byte0, 0, 74, c, 5);
		if(k > 0)
		{
			drawTexturedModalRect(j, byte0, 0, 79, k, 5);
		}
		String s = "Boss health";
		fontrenderer.drawStringWithShadow(s, i / 2 - fontrenderer.getStringWidth(s) / 2, byte0 - 10, 0xff00ff);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, mc.renderEngine.getTexture("/gui/icons.png"));
	}

	private void renderPumpkinBlur(int i, int j)
	{
		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
		GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, mc.renderEngine.getTexture("%blur%/misc/pumpkinblur.png"));
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(0.0D, j, -90D, 0.0D, 1.0D);
		tessellator.addVertexWithUV(i, j, -90D, 1.0D, 1.0D);
		tessellator.addVertexWithUV(i, 0.0D, -90D, 1.0D, 0.0D);
		tessellator.addVertexWithUV(0.0D, 0.0D, -90D, 0.0D, 0.0D);
		tessellator.draw();
		GL11.glDepthMask(true);
		GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
		GL11.glEnable(3008 /*GL_ALPHA_TEST*/);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private void renderVignette(float f, int i, int j)
	{
		f = 1.0F - f;
		if(f < 0.0F)
		{
			f = 0.0F;
		}
		if(f > 1.0F)
		{
			f = 1.0F;
		}
		prevVignetteBrightness += (double)(f - prevVignetteBrightness) * 0.01D;
		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(0, 769);
		GL11.glColor4f(prevVignetteBrightness, prevVignetteBrightness, prevVignetteBrightness, 1.0F);
		GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, mc.renderEngine.getTexture("%blur%/misc/vignette.png"));
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(0.0D, j, -90D, 0.0D, 1.0D);
		tessellator.addVertexWithUV(i, j, -90D, 1.0D, 1.0D);
		tessellator.addVertexWithUV(i, 0.0D, -90D, 1.0D, 0.0D);
		tessellator.addVertexWithUV(0.0D, 0.0D, -90D, 0.0D, 0.0D);
		tessellator.draw();
		GL11.glDepthMask(true);
		GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glBlendFunc(770, 771);
	}

	private void renderPortalOverlay(float f, int i, int j)
	{
		if(f < 1.0F)
		{
			f *= f;
			f *= f;
			f = f * 0.8F + 0.2F;
		}
		GL11.glDisable(3008 /*GL_ALPHA_TEST*/);
		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		GL11.glDepthMask(false);
		GL11.glBlendFunc(770, 771);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, f);
		GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, mc.renderEngine.getTexture("/terrain.png"));
		float f1 = (float)(Block.portal.blockIndexInTexture % 16) / 16F;
		float f2 = (float)(Block.portal.blockIndexInTexture / 16) / 16F;
		float f3 = (float)(Block.portal.blockIndexInTexture % 16 + 1) / 16F;
		float f4 = (float)(Block.portal.blockIndexInTexture / 16 + 1) / 16F;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(0.0D, j, -90D, f1, f4);
		tessellator.addVertexWithUV(i, j, -90D, f3, f4);
		tessellator.addVertexWithUV(i, 0.0D, -90D, f3, f2);
		tessellator.addVertexWithUV(0.0D, 0.0D, -90D, f1, f2);
		tessellator.draw();
		GL11.glDepthMask(true);
		GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
		GL11.glEnable(3008 /*GL_ALPHA_TEST*/);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private void renderInventorySlot(int i, int j, int k, float f)
	{
		ItemStack itemstack = mc.thePlayer.inventory.mainInventory[i];
		if(itemstack == null)
		{
			return;
		}
		float f1 = (float)itemstack.animationsToGo - f;
		if(f1 > 0.0F)
		{
			GL11.glPushMatrix();
			float f2 = 1.0F + f1 / 5F;
			GL11.glTranslatef(j + 8, k + 12, 0.0F);
			GL11.glScalef(1.0F / f2, (f2 + 1.0F) / 2.0F, 1.0F);
			GL11.glTranslatef(-(j + 8), -(k + 12), 0.0F);
		}
		itemRenderer.renderItemIntoGUI(mc.fontRenderer, mc.renderEngine, itemstack, j, k);
		if(f1 > 0.0F)
		{
			GL11.glPopMatrix();
		}
		itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, itemstack, j, k);
	}

	public void updateTick()
	{
		if(recordPlayingUpFor > 0)
		{
			recordPlayingUpFor--;
		}
		updateCounter++;
		for(int i = 0; i < chatMessageList.size(); i++)
		{
			((ChatLine)chatMessageList.get(i)).updateCounter++;
		}

	}

	public void clearChatMessages()
	{
		chatMessageList.clear();
	}

	public void addChatMessage(String s)
	{
		int i;
		for(; mc.fontRenderer.getStringWidth(s) > 320; s = s.substring(i))
		{
			for(i = 1; i < s.length() && mc.fontRenderer.getStringWidth(s.substring(0, i + 1)) <= 320; i++) { }
			addChatMessage(s.substring(0, i));
		}

		chatMessageList.add(0, new ChatLine(s));
		//Spout Improved Chat Start
		while(this.chatMessageList.size() > 3000) {
			this.chatMessageList.remove(this.chatMessageList.size() - 1);
		}
		//Spout Improved Chat End
	}

	public void setRecordPlayingMessage(String s)
	{
		recordPlaying = (new StringBuilder()).append("Now playing: ").append(s).toString();
		recordPlayingUpFor = 60;
		recordIsPlaying = true;
	}

	public void addChatMessageTranslate(String s)
	{
		StringTranslate stringtranslate = StringTranslate.getInstance();
		String s1 = stringtranslate.translateKey(s);
		addChatMessage(s1);
	}

}
