package net.minecraft.src;

import java.util.Random;
import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityItem;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Render;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.Tessellator;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

//Spout start
import org.newdawn.slick.opengl.Texture;
import org.spoutcraft.client.io.CustomTextureManager;
import org.spoutcraft.spoutcraftapi.block.design.BlockDesign;
import org.spoutcraft.spoutcraftapi.material.MaterialData;

//Spout end

public class RenderItem extends Render {

	private RenderBlocks renderBlocks = new RenderBlocks();
	private Random random = new Random();
	public boolean field_27004_a = true;
 	public float field_40268_b = 0.0F;


	public RenderItem() {
		this.shadowSize = 0.15F;
		this.field_194_c = 0.75F;
	}

	public void doRenderItem(EntityItem itemEntity, double x, double yOffset, double z, float var8, float deltaTime) {
		// Spout Start
		// Sanity Checks
		if (itemEntity == null || itemEntity.item == null) {
			return;
		}
		// Spout End
		this.random.setSeed(187L);
		ItemStack itemStack = itemEntity.item;
		float bounceAmmount = MathHelper.sin(((float) itemEntity.age + deltaTime) / 10.0F + itemEntity.field_804_d) * 0.1F + 0.1F;
		float rotation = (((float) itemEntity.age + deltaTime) / 20.0F + itemEntity.field_804_d) * 57.295776F;
		byte itemsOnGround = 1;
		if (itemEntity.item.stackSize > 1) {
			itemsOnGround = 2;
		}

		if (itemEntity.item.stackSize > 5) {
			itemsOnGround = 3;
		}

		if (itemEntity.item.stackSize > 20) {
			itemsOnGround = 4;
		}

		float red;
		int fullColor;
		float blue;
		float green;
		int renderType;
		
		//Spout start
		boolean custom = false;
		BlockDesign design = null;
		if (itemStack.itemID == 318) {
			org.spoutcraft.spoutcraftapi.material.CustomItem item = MaterialData.getCustomItem(itemStack.getItemDamage());
			if (item != null) {
				String textureURI = item.getTexture();
				if (textureURI == null) {
					org.spoutcraft.spoutcraftapi.material.CustomBlock block = MaterialData.getCustomBlock(itemStack.getItemDamage());
					design = block != null ? block.getBlockDesign() : null;
					textureURI = design != null ? design.getTexureURL() : null;
				}
				if (textureURI != null) {
					Texture texture = CustomTextureManager.getTextureFromUrl(item.getAddon().getDescription().getName(), textureURI);
					if (texture != null) {
						GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());
						custom = true;
					}
				}
			}
			
			/*org.spoutcraft.spoutcraftapi.material.CustomBlock block = MaterialData.getCustomBlock(var10.getItemDamage());
			design = block != null ? block.getBlockDesign() : null;
			if (design != null && design.getTextureAddon() != null && design.getTexureURL() != null) {
				Texture texture = CustomTextureManager.getTextureFromUrl(design.getTextureAddon(), design.getTexureURL());
				if (texture != null) {
					this.renderManager.renderEngine.bindTexture(texture.getTextureID());
					custom = true;
				}
			}*/
		}
	
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		
		if (design != null && custom) {
			//GL11.glScalef(0.25F, 0.25F, 0.25F);
			System.out.println("This right meow");
			design.renderItemstack((org.spoutcraft.spoutcraftapi.entity.Item)itemEntity.spoutEntity, (float)x, (float)(yOffset + bounceAmmount), (float)z, rotation, 0.25F, random);
		}
		else{
			//Spout end
			
			GL11.glPushMatrix();
			
			if (itemStack.itemID < 256) {
				this.loadTexture("/terrain.png");
			}
			else {
				this.loadTexture("/gui/items.png");
			}
			GL11.glTranslatef((float) x, (float) yOffset + bounceAmmount, (float) z);
			
	
			if(itemStack.itemID < 256 && RenderBlocks.renderItemIn3d(Block.blocksList[itemStack.itemID].getRenderType())) {
				GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
				float renderScale = 0.25F;
				renderType = Block.blocksList[itemStack.itemID].getRenderType();
				if(renderType == 1 || renderType == 19 || renderType == 12 || renderType == 2) {
					renderScale = 0.5F;
				}
	
				GL11.glScalef(renderScale, renderScale, renderScale);
	
				for(fullColor = 0; fullColor < itemsOnGround; ++fullColor) {
					GL11.glPushMatrix();
					if(fullColor > 0) {
						red = (this.random.nextFloat() * 2.0F - 1.0F) * 0.2F / renderScale;
						green = (this.random.nextFloat() * 2.0F - 1.0F) * 0.2F / renderScale;
						blue = (this.random.nextFloat() * 2.0F - 1.0F) * 0.2F / renderScale;
						GL11.glTranslatef(red, green, blue);
					}
	
					red = 1.0F;
					
					this.renderBlocks.renderBlockOnInventory(Block.blocksList[itemStack.itemID], itemStack.getItemDamage(), red);
					
					GL11.glPopMatrix();
				}
			} else if(itemStack.itemID == Item.potion.shiftedIndex) {
				GL11.glScalef(0.5F, 0.5F, 0.5F);
				short potionIndex = 141;
				float colorScale = 1.0F;
				if(this.field_27004_a) {
					fullColor = Item.itemsList[itemStack.itemID].getColorFromDamage(itemStack.getItemDamage());
					red = (float)(fullColor >> 16 & 255) / 255.0F;
					green = (float)(fullColor >> 8 & 255) / 255.0F;
					blue = (float)(fullColor & 255) / 255.0F;
					GL11.glColor4f(red * colorScale, green * colorScale, blue * colorScale, 1.0F);
				}
	
				this.renderItemBillboard(potionIndex, itemsOnGround);
				if (this.field_27004_a) {
					GL11.glColor4f(colorScale, colorScale, colorScale, 1.0F);
					this.renderItemBillboard(itemStack.getIconIndex(), itemsOnGround);
				}
			} else {
				GL11.glScalef(0.5F, 0.5F, 0.5F);
				int iconIndex = itemStack.getIconIndex();
	
				if(this.field_27004_a) {
					//The names here are wrong due to de-obfuscation renames;
					renderType = Item.itemsList[itemStack.itemID].getColorFromDamage(itemStack.getItemDamage());
					float var23 = (float)(renderType >> 16 & 255) / 255.0F;
					red = (float)(renderType >> 8 & 255) / 255.0F;
					green = (float)(renderType & 255) / 255.0F;
					blue = 1.0F;
					GL11.glColor4f(var23 * blue, red * blue, green * blue, 1.0F);
				}
	
				//Spout start
				this.renderItemBillboard(iconIndex, itemsOnGround, custom);
				//Spout end
			}
	
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glPopMatrix();
		}
	}
	
	//Spout start
	private void renderItemBillboard(int var1, int var2) {
		renderItemBillboard(var1, var2, false);
	}
	//Spout end

	private void renderItemBillboard(int var1, int var2, boolean customTexture) {
		Tessellator var3 = Tessellator.instance;
		float var4 = (float)(var1 % 16 * 16 + 0) / 256.0F;
		float var5 = (float)(var1 % 16 * 16 + 16) / 256.0F;
		float var6 = (float)(var1 / 16 * 16 + 0) / 256.0F;
		float var7 = (float)(var1 / 16 * 16 + 16) / 256.0F;
		float var8 = 1.0F;
		float var9 = 0.5F;
		float var10 = 0.25F;
		
		//Spout start
		if (customTexture) {
			var4 = 0F;
			var5 = 1F;
			var6 = 1F;
			var7 = 0F;
		}
		//Spout end

		for(int var11 = 0; var11 < var2; ++var11) {
			GL11.glPushMatrix();
			if(var11 > 0) {
				float var12 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
				float var13 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
				float var14 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3F;
				GL11.glTranslatef(var12, var13, var14);
			}

			GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
			var3.startDrawingQuads();
			var3.setNormal(0.0F, 1.0F, 0.0F);
			var3.addVertexWithUV((double)(0.0F - var9), (double)(0.0F - var10), 0.0D, (double)var4, (double)var7);
			var3.addVertexWithUV((double)(var8 - var9), (double)(0.0F - var10), 0.0D, (double)var5, (double)var7);
			var3.addVertexWithUV((double)(var8 - var9), (double)(1.0F - var10), 0.0D, (double)var5, (double)var6);
			var3.addVertexWithUV((double)(0.0F - var9), (double)(1.0F - var10), 0.0D, (double)var4, (double)var6);
			var3.draw();
			GL11.glPopMatrix();
		}
	}

	public void drawItemIntoGui(FontRenderer var1, RenderEngine var2, int var3, int var4, int var5, int var6, int var7) {
		
		boolean custom = false;
		BlockDesign design = null;
		if (var3 == 318) {
			
			org.spoutcraft.spoutcraftapi.material.CustomItem item = MaterialData.getCustomItem(var4);
			if (item != null) {
				String textureURI = item.getTexture();
				if (textureURI == null) {
					org.spoutcraft.spoutcraftapi.material.CustomBlock block = MaterialData.getCustomBlock(var4);
					design = block != null ? block.getBlockDesign() : null;
					textureURI = design != null ? design.getTexureURL() : null;
				}
				if (textureURI != null) {
					Texture texture = CustomTextureManager.getTextureFromUrl(item.getAddon().getDescription().getName(), textureURI);
					if (texture != null) {
						GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());
						custom = true;
					}
				}
			}
		}
		if (!custom) {
			if (var3 < 256) {
				var2.bindTexture(var2.getTexture("/terrain.png"));
			}
			else {
				var2.bindTexture(var2.getTexture("/gui/items.png"));
			}
		}
		
		float var11;
		float var12;
		
		if (design != null && custom) {
			design.renderItemOnHUD((float)(var6 - 2), (float)(var7 + 3), -3.0F + this.field_40268_b);
		}
		else if(var3 < 256 && RenderBlocks.renderItemIn3d(Block.blocksList[var3].getRenderType())) {
			Block var16 = Block.blocksList[var3];
			GL11.glPushMatrix();
			GL11.glTranslatef((float)(var6 - 2), (float)(var7 + 3), -3.0F + this.field_40268_b);
			GL11.glScalef(10.0F, 10.0F, 10.0F);
			GL11.glTranslatef(1.0F, 0.5F, 1.0F);
			GL11.glScalef(1.0F, 1.0F, -1.0F);
			GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
			int var17 = Item.itemsList[var3].getColorFromDamage(var4);
			var11 = (float)(var17 >> 16 & 255) / 255.0F;
			var12 = (float)(var17 >> 8 & 255) / 255.0F;
			float var13 = (float)(var17 & 255) / 255.0F;
			if (this.field_27004_a) {
				GL11.glColor4f(var11, var12, var13, 1.0F);
			}

			GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			this.renderBlocks.useInventoryTint = this.field_27004_a;
			this.renderBlocks.renderBlockOnInventory(var16, var4, 1.0F);
			this.renderBlocks.useInventoryTint = true;
			GL11.glPopMatrix();
		} else {
			float var10;
			if(var3 == Item.potion.shiftedIndex) {
				GL11.glDisable(2896 /* GL_LIGHTING */);
				short var8 = 141;
				int var9 = Item.itemsList[var3].getColorFromDamage(var4);
				var10 = (float)(var9 >> 16 & 255) / 255.0F;
				var11 = (float)(var9 >> 8 & 255) / 255.0F;
				var12 = (float)(var9 & 255) / 255.0F;
				if(this.field_27004_a) {
					GL11.glColor4f(var10, var11, var12, 1.0F);
				}
	
				this.renderTexturedQuad(var6, var7, var8 % 16 * 16, var8 / 16 * 16, 16, 16);
				if (this.field_27004_a) {
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				}
	
				this.renderTexturedQuad(var6, var7, var5 % 16 * 16, var5 / 16 * 16, 16, 16);
				
				GL11.glEnable(2896 /*GL_LIGHTING*/);
			}
			else if(var5 >= 0) {
				GL11.glDisable(2896 /*GL_LIGHTING*/);

				int var14 = Item.itemsList[var3].getColorFromDamage(var4);
				float var15 = (float)(var14 >> 16 & 255) / 255.0F;
				var10 = (float)(var14 >> 8 & 255) / 255.0F;
				var11 = (float)(var14 & 255) / 255.0F;
				if(this.field_27004_a) {
					GL11.glColor4f(var15, var10, var11, 1.0F);
				}

				// Spout Start
				if (custom) {
					Tessellator tes = Tessellator.instance;
					tes.startDrawingQuads();
					tes.addVertexWithUV((double) (var6 + 0), (double) (var7 + 16), (double) 0, 0, 0);
					tes.addVertexWithUV((double) (var6 + 16), (double) (var7 + 16), (double) 0, 1, 0);
					tes.addVertexWithUV((double) (var6 + 16), (double) (var7 + 0), (double) 0, 1, 1);
					tes.addVertexWithUV((double) (var6 + 0), (double) (var7 + 0), (double) 0, 0, 1);
					tes.draw();
				} else
					this.renderTexturedQuad(var6, var7, var5 % 16 * 16, var5 / 16 * 16, 16, 16);
				// Spout End
				GL11.glEnable(2896 /* GL_LIGHTING */);
			}
		}

		GL11.glEnable(2884 /* GL_CULL_FACE */);
	}

	public void renderItemIntoGUI(FontRenderer var1, RenderEngine var2, ItemStack var3, int var4, int var5) {
		if (var3 != null) {
			this.drawItemIntoGui(var1, var2, var3.itemID, var3.getItemDamage(), var3.getIconIndex(), var4, var5);
			if(var3 != null && var3.func_40713_r()) {
				GL11.glDepthFunc(516);
				GL11.glDisable(2896 /*GL_LIGHTING*/);
				GL11.glDepthMask(false);
				var2.bindTexture(var2.getTexture("%blur%/misc/glint.png"));
				this.field_40268_b -= 50.0F;
				GL11.glEnable(3042 /*GL_BLEND*/);
				GL11.glBlendFunc(774, 774);
				GL11.glColor4f(0.5F, 0.25F, 0.8F, 1.0F);
				this.func_40266_a(var4 * 431278612 + var5 * 32178161, var4 - 2, var5 - 2, 20, 20);
				GL11.glDisable(3042 /*GL_BLEND*/);
				GL11.glDepthMask(true);
				this.field_40268_b += 50.0F;
				GL11.glEnable(2896 /*GL_LIGHTING*/);
				GL11.glDepthFunc(515);
			}

		}
	}

	private void func_40266_a(int var1, int var2, int var3, int var4, int var5) {
		for(int var6 = 0; var6 < 2; ++var6) {
			if(var6 == 0) {
				GL11.glBlendFunc(768, 1);
			}

			if(var6 == 1) {
				GL11.glBlendFunc(768, 1);
			}

			float var7 = 0.00390625F;
			float var8 = 0.00390625F;
			float var9 = (float)(System.currentTimeMillis() % (long)(3000 + var6 * 1873)) / (3000.0F + (float)(var6 * 1873)) * 256.0F;
			float var10 = 0.0F;
			Tessellator var11 = Tessellator.instance;
			float var12 = 4.0F;
			if(var6 == 1) {
				var12 = -1.0F;
		}

			var11.startDrawingQuads();
			var11.addVertexWithUV((double)(var2 + 0), (double)(var3 + var5), (double)this.field_40268_b, (double)((var9 + (float)var5 * var12) * var7), (double)((var10 + (float)var5) * var8));
			var11.addVertexWithUV((double)(var2 + var4), (double)(var3 + var5), (double)this.field_40268_b, (double)((var9 + (float)var4 + (float)var5 * var12) * var7), (double)((var10 + (float)var5) * var8));
			var11.addVertexWithUV((double)(var2 + var4), (double)(var3 + 0), (double)this.field_40268_b, (double)((var9 + (float)var4) * var7), (double)((var10 + 0.0F) * var8));
			var11.addVertexWithUV((double)(var2 + 0), (double)(var3 + 0), (double)this.field_40268_b, (double)((var9 + 0.0F) * var7), (double)((var10 + 0.0F) * var8));
			var11.draw();
		}

	}

	public void renderItemOverlayIntoGUI(FontRenderer var1, RenderEngine var2, ItemStack var3, int var4, int var5) {
		if (var3 != null) {
			if (var3.stackSize > 1) {
				String var6 = "" + var3.stackSize;
				GL11.glDisable(2896 /* GL_LIGHTING */);
				GL11.glDisable(2929 /* GL_DEPTH_TEST */);
				var1.drawStringWithShadow(var6, var4 + 19 - 2 - var1.getStringWidth(var6), var5 + 6 + 3, 16777215);
				GL11.glEnable(2896 /* GL_LIGHTING */);
				GL11.glEnable(2929 /* GL_DEPTH_TEST */);
			}

			if (var3.isItemDamaged()) {
				int var11 = (int) Math.round(13.0D - (double) var3.getItemDamageForDisplay() * 13.0D / (double) var3.getMaxDamage());
				int var7 = (int) Math.round(255.0D - (double) var3.getItemDamageForDisplay() * 255.0D / (double) var3.getMaxDamage());
				GL11.glDisable(2896 /* GL_LIGHTING */);
				GL11.glDisable(2929 /* GL_DEPTH_TEST */);
				GL11.glDisable(3553 /* GL_TEXTURE_2D */);
				Tessellator var8 = Tessellator.instance;
				int var9 = 255 - var7 << 16 | var7 << 8;
				int var10 = (255 - var7) / 4 << 16 | 16128;
				this.renderQuad(var8, var4 + 2, var5 + 13, 13, 2, 0);
				this.renderQuad(var8, var4 + 2, var5 + 13, 12, 1, var10);
				this.renderQuad(var8, var4 + 2, var5 + 13, var11, 1, var9);
				GL11.glEnable(3553 /* GL_TEXTURE_2D */);
				GL11.glEnable(2896 /* GL_LIGHTING */);
				GL11.glEnable(2929 /* GL_DEPTH_TEST */);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			}

		}
	}

	private void renderQuad(Tessellator var1, int var2, int var3, int var4, int var5, int var6) {
		var1.startDrawingQuads();
		var1.setColorOpaque_I(var6);
		var1.addVertex((double) (var2 + 0), (double) (var3 + 0), 0.0D);
		var1.addVertex((double) (var2 + 0), (double) (var3 + var5), 0.0D);
		var1.addVertex((double) (var2 + var4), (double) (var3 + var5), 0.0D);
		var1.addVertex((double) (var2 + var4), (double) (var3 + 0), 0.0D);
		var1.draw();
	}

	public void renderTexturedQuad(int var1, int var2, int var3, int var4, int var5, int var6) {
		float var7 = 0.00390625F;
		float var8 = 0.00390625F;
		Tessellator var9 = Tessellator.instance;
		var9.startDrawingQuads();
		var9.addVertexWithUV((double)(var1 + 0), (double)(var2 + var6), (double)this.field_40268_b, (double)((float)(var3 + 0) * var7), (double)((float)(var4 + var6) * var8));
		var9.addVertexWithUV((double)(var1 + var5), (double)(var2 + var6), (double)this.field_40268_b, (double)((float)(var3 + var5) * var7), (double)((float)(var4 + var6) * var8));
		var9.addVertexWithUV((double)(var1 + var5), (double)(var2 + 0), (double)this.field_40268_b, (double)((float)(var3 + var5) * var7), (double)((float)(var4 + 0) * var8));
		var9.addVertexWithUV((double)(var1 + 0), (double)(var2 + 0), (double)this.field_40268_b, (double)((float)(var3 + 0) * var7), (double)((float)(var4 + 0) * var8));
		var9.draw();
	}

	// $FF: synthetic method
	// $FF: bridge method
	public void doRender(Entity var1, double var2, double var4, double var6, float var8, float var9) {
		this.doRenderItem((EntityItem) var1, var2, var4, var6, var8, var9);
	}
}
