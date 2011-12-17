package net.minecraft.src;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.src.ActiveRenderInfo;
import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Block;
import net.minecraft.src.ChunkProviderLoadOrGenerate;
import net.minecraft.src.ClippingHelperImpl;
import net.minecraft.src.EffectRenderer;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerSP;
import net.minecraft.src.EntityRainFX;
import net.minecraft.src.EntitySmokeFX;
import net.minecraft.src.Frustrum;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.IChunkProvider;
import net.minecraft.src.ItemRenderer;
import net.minecraft.src.Material;
import net.minecraft.src.MathHelper;
import net.minecraft.src.MouseFilter;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.OpenGlHelper;
import net.minecraft.src.Potion;
import net.minecraft.src.Profiler;
import net.minecraft.src.RenderGlobal;
import net.minecraft.src.RenderHelper;
import net.minecraft.src.ScaledResolution;
import net.minecraft.src.Tessellator;
import net.minecraft.src.Vec3D;
import net.minecraft.src.World;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;
//Spout start
import org.lwjgl.util.vector.*;
import org.spoutcraft.client.SpoutClient;
import org.spoutcraft.client.config.ConfigReader;

//Spout end

public class EntityRenderer {

	public static boolean anaglyphEnable = false;
	public static int anaglyphField;
	private Minecraft mc;
	private float farPlaneDistance = 0.0F;
	public ItemRenderer itemRenderer;
	private int rendererUpdateCount;
	private Entity pointedEntity = null;
	private MouseFilter mouseFilterXAxis = new MouseFilter();
	private MouseFilter mouseFilterYAxis = new MouseFilter();
	private MouseFilter mouseFilterDummy1 = new MouseFilter();
	private MouseFilter mouseFilterDummy2 = new MouseFilter();
	private MouseFilter mouseFilterDummy3 = new MouseFilter();
	private MouseFilter mouseFilterDummy4 = new MouseFilter();
	private float thirdPersonDistance = 4.0F;
	private float thirdPersonDistanceTemp = 4.0F;
	private float debugCamYaw = 0.0F;
	private float prevDebugCamYaw = 0.0F;
	private float debugCamPitch = 0.0F;
	private float prevDebugCamPitch = 0.0F;
	private float smoothCamYaw;
	private float smoothCamPitch;
	private float smoothCamFilterX;
	private float smoothCamFilterY;
	private float smoothCamPartialTicks;
	private float debugCamFOV = 0.0F;
	private float prevDebugCamFOV = 0.0F;
	private float camRoll = 0.0F;
	private float prevCamRoll = 0.0F;
	public int emptyTexture;
	private int[] lightmapColors;
	private float fovModifierHand;
	private float fovModifierHandPrev;
	private float fovMultiplierTemp;
	private boolean cloudFog = false;
	private double cameraZoom = 1.0D;
	private double cameraYaw = 0.0D;
	private double cameraPitch = 0.0D;
	private long prevFrameTime = System.currentTimeMillis();
	private long renderEndNanoTime = 0L;
	private boolean lightmapUpdateNeeded = false;
	float torchFlickerX = 0.0F;
	float torchFlickerDX = 0.0F;
	float torchFlickerY = 0.0F;
	float torchFlickerDY = 0.0F;
	private Random random = new Random();
	private int rainSoundCounter = 0;
	float[] rainXCoords;
	float[] rainYCoords;
	volatile int unusedVolatile0 = 0;
	volatile int unusedVolatile1 = 0;
	FloatBuffer fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);
	float fogColorRed;
	float fogColorGreen;
	float fogColorBlue;
	private float fogColor2;
	private float fogColor1;
	public int debugViewDirection;
	// Spout Start
	private WorldProvider updatedWorldProvider = null;
	private boolean showDebugInfo = false;
	public static Matrix4f view = new Matrix4f();
	public static Matrix4f projection = new Matrix4f();

	// Spout End

	public EntityRenderer(Minecraft var1) {
		this.mc = var1;
		this.itemRenderer = new ItemRenderer(var1);
		this.emptyTexture = var1.renderEngine.allocateAndSetupTexture(new BufferedImage(16, 16, 1));
		this.lightmapColors = new int[256];
	}

	public void updateRenderer() {
		this.updateFovModifierHand();
		this.updateTorchFlicker();
		this.fogColor2 = this.fogColor1;
		this.thirdPersonDistanceTemp = this.thirdPersonDistance;
		this.prevDebugCamYaw = this.debugCamYaw;
		this.prevDebugCamPitch = this.debugCamPitch;
		this.prevDebugCamFOV = this.debugCamFOV;
		this.prevCamRoll = this.camRoll;
		float var1;
		float var2;
		if (this.mc.gameSettings.smoothCamera) {
			var1 = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
			var2 = var1 * var1 * var1 * 8.0F;
			this.smoothCamFilterX = this.mouseFilterXAxis.func_22386_a(this.smoothCamYaw, 0.05F * var2);
			this.smoothCamFilterY = this.mouseFilterYAxis.func_22386_a(this.smoothCamPitch, 0.05F * var2);
			this.smoothCamPartialTicks = 0.0F;
			this.smoothCamYaw = 0.0F;
			this.smoothCamPitch = 0.0F;
		}

		if (this.mc.renderViewEntity == null) {
			this.mc.renderViewEntity = this.mc.thePlayer;
		}

		var1 = this.mc.theWorld.getLightBrightness(MathHelper.floor_double(this.mc.renderViewEntity.posX), MathHelper.floor_double(this.mc.renderViewEntity.posY), MathHelper.floor_double(this.mc.renderViewEntity.posZ));
		var2 = (float) (3 - this.mc.gameSettings.renderDistance) / 3.0F;
		float var3 = var1 * (1.0F - var2) + var2;
		this.fogColor1 += (var3 - this.fogColor1) * 0.1F;
		++this.rendererUpdateCount;
		this.itemRenderer.updateEquippedItem();
		this.addRainParticles();
	}

	public void getMouseOver(float var1) {
		if (this.mc.renderViewEntity != null) {
			if (this.mc.theWorld != null) {
				double var2 = (double) this.mc.playerController.getBlockReachDistance();
				this.mc.objectMouseOver = this.mc.renderViewEntity.rayTrace(var2, var1);
				double var4 = var2;
				Vec3D var6 = this.mc.renderViewEntity.getPosition(var1);
				if (this.mc.objectMouseOver != null) {
					var4 = this.mc.objectMouseOver.hitVec.distanceTo(var6);
				}

				if (this.mc.playerController.extendedReach()) {
					var2 = 6.0D;
					var4 = 6.0D;
				} else {
					if (var4 > 3.0D) {
						var4 = 3.0D;
					}

					var2 = var4;
				}

				Vec3D var7 = this.mc.renderViewEntity.getLook(var1);
				Vec3D var8 = var6.addVector(var7.xCoord * var2, var7.yCoord * var2, var7.zCoord * var2);
				this.pointedEntity = null;
				float var9 = 1.0F;
				List var10 = this.mc.theWorld.getEntitiesWithinAABBExcludingEntity(this.mc.renderViewEntity, this.mc.renderViewEntity.boundingBox.addCoord(var7.xCoord * var2, var7.yCoord * var2, var7.zCoord * var2).expand((double) var9, (double) var9, (double) var9));
				double var11 = 0.0D;

				for (int var13 = 0; var13 < var10.size(); ++var13) {
					Entity var14 = (Entity) var10.get(var13);
					if (var14.canBeCollidedWith()) {
						float var15 = var14.getCollisionBorderSize();
						AxisAlignedBB var16 = var14.boundingBox.expand((double) var15, (double) var15, (double) var15);
						MovingObjectPosition var17 = var16.func_1169_a(var6, var8);
						if (var16.isVecInside(var6)) {
							if (0.0D < var11 || var11 == 0.0D) {
								this.pointedEntity = var14;
								var11 = 0.0D;
							}
						} else if (var17 != null) {
							double var18 = var6.distanceTo(var17.hitVec);
							if (var18 < var11 || var11 == 0.0D) {
								this.pointedEntity = var14;
								var11 = var18;
							}
						}
					}
				}

				if (this.pointedEntity != null) {
					this.mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity);
				}

			}
		}
	}

	private void updateFovModifierHand() {
		EntityPlayerSP var1 = (EntityPlayerSP) this.mc.renderViewEntity;
		this.fovMultiplierTemp = var1.getFOVMultiplier();
		this.fovModifierHandPrev = this.fovModifierHand;
		this.fovModifierHand += (this.fovMultiplierTemp - this.fovModifierHand) * 0.5F;
	}

	private float getFOVModifier(float var1, boolean var2) {
		if (this.debugViewDirection > 0) {
			return 90.0F;
		} else {
			EntityPlayer var3 = (EntityPlayer) this.mc.renderViewEntity;
			float var4 = 70.0F;
			if (var2) {
				var4 += this.mc.gameSettings.fovSetting * 40.0F;
				var4 *= this.fovModifierHandPrev + (this.fovModifierHand - this.fovModifierHandPrev) * var1;
			}

			if (var3.getEntityHealth() <= 0) {
				float var5 = (float) var3.deathTime + var1;
				var4 /= (1.0F - 500.0F / (var5 + 500.0F)) * 2.0F + 1.0F;
			}

			int var6 = ActiveRenderInfo.func_41066_a(this.mc.theWorld, var3, var1);
			if (var6 != 0 && Block.blocksList[var6].blockMaterial == Material.water) {
				var4 = var4 * 60.0F / 70.0F;
			}

			return var4 + this.prevDebugCamFOV + (this.debugCamFOV - this.prevDebugCamFOV) * var1;
		}
	}

	private void hurtCameraEffect(float var1) {
		EntityLiving var2 = this.mc.renderViewEntity;
		float var3 = (float) var2.hurtTime - var1;
		float var4;
		if (var2.getEntityHealth() <= 0) {
			var4 = (float) var2.deathTime + var1;
			GL11.glRotatef(40.0F - 8000.0F / (var4 + 200.0F), 0.0F, 0.0F, 1.0F);
		}

		if (var3 >= 0.0F) {
			var3 /= (float) var2.maxHurtTime;
			var3 = MathHelper.sin(var3 * var3 * var3 * var3 * 3.1415927F);
			var4 = var2.attackedAtYaw;
			GL11.glRotatef(-var4, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(-var3 * 14.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(var4, 0.0F, 1.0F, 0.0F);
		}
	}

	private void setupViewBobbing(float var1) {
		if (this.mc.renderViewEntity instanceof EntityPlayer) {
			EntityPlayer var2 = (EntityPlayer) this.mc.renderViewEntity;
			float var3 = var2.distanceWalkedModified - var2.prevDistanceWalkedModified;
			float var4 = -(var2.distanceWalkedModified + var3 * var1);
			float var5 = var2.prevCameraYaw + (var2.cameraYaw - var2.prevCameraYaw) * var1;
			float var6 = var2.prevCameraPitch + (var2.cameraPitch - var2.prevCameraPitch) * var1;
			GL11.glTranslatef(MathHelper.sin(var4 * 3.1415927F) * var5 * 0.5F, -Math.abs(MathHelper.cos(var4 * 3.1415927F) * var5), 0.0F);
			GL11.glRotatef(MathHelper.sin(var4 * 3.1415927F) * var5 * 3.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(Math.abs(MathHelper.cos(var4 * 3.1415927F - 0.2F) * var5) * 5.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(var6, 1.0F, 0.0F, 0.0F);
		}
	}

	private void orientCamera(float var1) {
		EntityLiving var2 = this.mc.renderViewEntity;
		float var3 = var2.yOffset - 1.62F;
		double var4 = var2.prevPosX + (var2.posX - var2.prevPosX) * (double) var1;
		double var6 = var2.prevPosY + (var2.posY - var2.prevPosY) * (double) var1 - (double) var3;
		double var8 = var2.prevPosZ + (var2.posZ - var2.prevPosZ) * (double) var1;
		GL11.glRotatef(this.prevCamRoll + (this.camRoll - this.prevCamRoll) * var1, 0.0F, 0.0F, 1.0F);
		if (var2.isPlayerSleeping()) {
			var3 = (float) ((double) var3 + 1.0D);
			GL11.glTranslatef(0.0F, 0.3F, 0.0F);
			if (!this.mc.gameSettings.debugCamEnable) {
				int var10 = this.mc.theWorld.getBlockId(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ));
				if (var10 == Block.bed.blockID) {
					int var11 = this.mc.theWorld.getBlockMetadata(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posY), MathHelper.floor_double(var2.posZ));
					int var12 = var11 & 3;
					GL11.glRotatef((float) (var12 * 90), 0.0F, 1.0F, 0.0F);
				}

				GL11.glRotatef(var2.prevRotationYaw + (var2.rotationYaw - var2.prevRotationYaw) * var1 + 180.0F, 0.0F, -1.0F, 0.0F);
				GL11.glRotatef(var2.prevRotationPitch + (var2.rotationPitch - var2.prevRotationPitch) * var1, -1.0F, 0.0F, 0.0F);
			}
		} else if (this.mc.gameSettings.thirdPersonView > 0) {
			double var27 = (double) (this.thirdPersonDistanceTemp + (this.thirdPersonDistance - this.thirdPersonDistanceTemp) * var1);
			float var13;
			float var28;
			if (this.mc.gameSettings.debugCamEnable) {
				var28 = this.prevDebugCamYaw + (this.debugCamYaw - this.prevDebugCamYaw) * var1;
				var13 = this.prevDebugCamPitch + (this.debugCamPitch - this.prevDebugCamPitch) * var1;
				GL11.glTranslatef(0.0F, 0.0F, (float) (-var27));
				GL11.glRotatef(var13, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(var28, 0.0F, 1.0F, 0.0F);
			} else {
				var28 = var2.rotationYaw;
				var13 = var2.rotationPitch;
				if (this.mc.gameSettings.thirdPersonView == 2) {
					var13 += 180.0F;
				}

				double var14 = (double) (-MathHelper.sin(var28 / 180.0F * 3.1415927F) * MathHelper.cos(var13 / 180.0F * 3.1415927F)) * var27;
				double var16 = (double) (MathHelper.cos(var28 / 180.0F * 3.1415927F) * MathHelper.cos(var13 / 180.0F * 3.1415927F)) * var27;
				double var18 = (double) (-MathHelper.sin(var13 / 180.0F * 3.1415927F)) * var27;

				for (int var20 = 0; var20 < 8; ++var20) {
					float var21 = (float) ((var20 & 1) * 2 - 1);
					float var22 = (float) ((var20 >> 1 & 1) * 2 - 1);
					float var23 = (float) ((var20 >> 2 & 1) * 2 - 1);
					var21 *= 0.1F;
					var22 *= 0.1F;
					var23 *= 0.1F;
					MovingObjectPosition var24 = this.mc.theWorld.rayTraceBlocks(Vec3D.createVector(var4 + (double) var21, var6 + (double) var22, var8 + (double) var23), Vec3D.createVector(var4 - var14 + (double) var21 + (double) var23, var6 - var18 + (double) var22, var8 - var16 + (double) var23));
					if (var24 != null) {
						double var25 = var24.hitVec.distanceTo(Vec3D.createVector(var4, var6, var8));
						if (var25 < var27) {
							var27 = var25;
						}
					}
				}

				if (this.mc.gameSettings.thirdPersonView == 2) {
					GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
				}

				GL11.glRotatef(var2.rotationPitch - var13, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(var2.rotationYaw - var28, 0.0F, 1.0F, 0.0F);
				GL11.glTranslatef(0.0F, 0.0F, (float) (-var27));
				GL11.glRotatef(var28 - var2.rotationYaw, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(var13 - var2.rotationPitch, 1.0F, 0.0F, 0.0F);
			}
		} else {
			GL11.glTranslatef(0.0F, 0.0F, -0.1F);
		}

		if (!this.mc.gameSettings.debugCamEnable) {
			GL11.glRotatef(var2.prevRotationPitch + (var2.rotationPitch - var2.prevRotationPitch) * var1, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(var2.prevRotationYaw + (var2.rotationYaw - var2.prevRotationYaw) * var1 + 180.0F, 0.0F, 1.0F, 0.0F);
		}

		GL11.glTranslatef(0.0F, var3, 0.0F);
		var4 = var2.prevPosX + (var2.posX - var2.prevPosX) * (double) var1;
		var6 = var2.prevPosY + (var2.posY - var2.prevPosY) * (double) var1 - (double) var3;
		var8 = var2.prevPosZ + (var2.posZ - var2.prevPosZ) * (double) var1;
		this.cloudFog = this.mc.renderGlobal.func_27307_a(var4, var6, var8, var1);
	}

	private void setupCameraTransform(float var1, int var2) {
		// Spout Start
		this.farPlaneDistance = (float) (32 << 3 - this.mc.gameSettings.renderDistance);
		if (ConfigReader.farView) {
			if (this.farPlaneDistance < 256.0F) {
				this.farPlaneDistance *= 3.0F;
			} else {
				this.farPlaneDistance *= 2.0F;
			}
		}

		if (ConfigReader.fancyFog) {
			this.farPlaneDistance *= 0.95F;
		} else {
			this.farPlaneDistance *= 0.83F;
		}
		// Spout End

		GL11.glMatrixMode(5889 /* GL_PROJECTION */);
		GL11.glLoadIdentity();
		float var3 = 0.07F;
		if (this.mc.gameSettings.anaglyph) {
			GL11.glTranslatef((float) (-(var2 * 2 - 1)) * var3, 0.0F, 0.0F);
		}

		if (this.cameraZoom != 1.0D) {
			GL11.glTranslatef((float) this.cameraYaw, (float) (-this.cameraPitch), 0.0F);
			GL11.glScaled(this.cameraZoom, this.cameraZoom, 1.0D);
			GLU.gluPerspective(this.getFOVModifier(var1, true), (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
		} else {
			GLU.gluPerspective(this.getFOVModifier(var1, true), (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
		}

		float var4;
		if (this.mc.playerController.func_35643_e()) {
			var4 = 0.6666667F;
			GL11.glScalef(1.0F, var4, 1.0F);
		}

		GL11.glMatrixMode(5888 /* GL_MODELVIEW0_ARB */);
		GL11.glLoadIdentity();
		if (this.mc.gameSettings.anaglyph) {
			GL11.glTranslatef((float) (var2 * 2 - 1) * 0.1F, 0.0F, 0.0F);
		}

		this.hurtCameraEffect(var1);
		if (this.mc.gameSettings.viewBobbing) {
			this.setupViewBobbing(var1);
		}

		var4 = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * var1;
		if (var4 > 0.0F) {
			byte var5 = 20;
			if (this.mc.thePlayer.isPotionActive(Potion.potionConfusion)) {
				var5 = 7;
			}

			float var6 = 5.0F / (var4 * var4 + 5.0F) - var4 * 0.04F;
			var6 *= var6;
			GL11.glRotatef(((float) this.rendererUpdateCount + var1) * (float) var5, 0.0F, 1.0F, 1.0F);
			GL11.glScalef(1.0F / var6, 1.0F, 1.0F);
			GL11.glRotatef(-((float) this.rendererUpdateCount + var1) * (float) var5, 0.0F, 1.0F, 1.0F);
		}

		this.orientCamera(var1);
		if (this.debugViewDirection > 0) {
			int var7 = this.debugViewDirection - 1;
			if (var7 == 1) {
				GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
			}

			if (var7 == 2) {
				GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
			}

			if (var7 == 3) {
				GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			}

			if (var7 == 4) {
				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
			}

			if (var7 == 5) {
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
			}
		}

	}

	// Spout Start
	private Matrix4f createPerspective(float fov, float aspect, float znear, float zfar) {

		float ymax, xmax;
		float temp, temp2, temp3, temp4;
		ymax = znear * (float) Math.tan(fov * Math.PI / 360.0);
		// ymin = -ymax;
		// xmin = -ymax * aspectRatio;
		xmax = ymax * aspect;

		temp = 2.0f * znear;
		temp2 = xmax - -xmax;
		temp3 = ymax - -ymax;
		temp4 = zfar - znear;

		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		matrix.m00 = temp / temp2;
		matrix.m11 = temp / temp3;
		matrix.m20 = (xmax + -xmax) / temp2;
		matrix.m21 = (ymax + -ymax) / temp3;
		matrix.m22 = (-zfar - znear) / temp4;
		matrix.m23 = -1;
		matrix.m32 = (-temp * zfar) / temp4;

		return matrix;
	}

	// Spout End

	private void renderHand(float var1, int var2) {
		if (this.debugViewDirection <= 0) {
			GL11.glMatrixMode(5889 /* GL_PROJECTION */);
			GL11.glLoadIdentity();
			float var3 = 0.07F;
			if (this.mc.gameSettings.anaglyph) {
				GL11.glTranslatef((float) (-(var2 * 2 - 1)) * var3, 0.0F, 0.0F);
			}

			if (this.cameraZoom != 1.0D) {
				GL11.glTranslatef((float) this.cameraYaw, (float) (-this.cameraPitch), 0.0F);
				GL11.glScaled(this.cameraZoom, this.cameraZoom, 1.0D);
				GLU.gluPerspective(this.getFOVModifier(var1, false), (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
			} else {
				GLU.gluPerspective(this.getFOVModifier(var1, false), (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
			}

			if (this.mc.playerController.func_35643_e()) {
				float var4 = 0.6666667F;
				GL11.glScalef(1.0F, var4, 1.0F);
			}

			GL11.glMatrixMode(5888 /* GL_MODELVIEW0_ARB */);
			GL11.glLoadIdentity();
			if (this.mc.gameSettings.anaglyph) {
				GL11.glTranslatef((float) (var2 * 2 - 1) * 0.1F, 0.0F, 0.0F);
			}

			GL11.glPushMatrix();
			this.hurtCameraEffect(var1);
			if (this.mc.gameSettings.viewBobbing) {
				this.setupViewBobbing(var1);
			}

			if (this.mc.gameSettings.thirdPersonView == 0 && !this.mc.renderViewEntity.isPlayerSleeping() && !this.mc.gameSettings.hideGUI && !this.mc.playerController.func_35643_e()) {
				this.enableLightmap((double) var1);
				this.itemRenderer.renderItemInFirstPerson(var1);
				this.disableLightmap((double) var1);
			}

			GL11.glPopMatrix();
			if (this.mc.gameSettings.thirdPersonView == 0 && !this.mc.renderViewEntity.isPlayerSleeping()) {
				this.itemRenderer.renderOverlays(var1);
				this.hurtCameraEffect(var1);
			}

			if (this.mc.gameSettings.viewBobbing) {
				this.setupViewBobbing(var1);
			}

		}
	}

	public void disableLightmap(double var1) {
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapEnabled);
		GL11.glDisable(3553 /* GL_TEXTURE_2D */);
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapDisabled);
	}

	public void enableLightmap(double var1) {
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapEnabled);
		GL11.glMatrixMode(5890 /* GL_TEXTURE */);
		GL11.glLoadIdentity();
		float var3 = 0.00390625F;
		GL11.glScalef(var3, var3, var3);
		GL11.glTranslatef(8.0F, 8.0F, 8.0F);
		GL11.glMatrixMode(5888 /* GL_MODELVIEW0_ARB */);
		this.mc.renderEngine.bindTexture(this.emptyTexture);
		GL11.glTexParameteri(3553 /* GL_TEXTURE_2D */, 10241 /* GL_TEXTURE_MIN_FILTER */, 9729 /* GL_LINEAR */);
		GL11.glTexParameteri(3553 /* GL_TEXTURE_2D */, 10240 /* GL_TEXTURE_MAG_FILTER */, 9729 /* GL_LINEAR */);
		GL11.glTexParameteri(3553 /* GL_TEXTURE_2D */, 10241 /* GL_TEXTURE_MIN_FILTER */, 9729 /* GL_LINEAR */);
		GL11.glTexParameteri(3553 /* GL_TEXTURE_2D */, 10240 /* GL_TEXTURE_MAG_FILTER */, 9729 /* GL_LINEAR */);
		GL11.glTexParameteri(3553 /* GL_TEXTURE_2D */, 10242 /* GL_TEXTURE_WRAP_S */, 10496 /* GL_CLAMP */);
		GL11.glTexParameteri(3553 /* GL_TEXTURE_2D */, 10243 /* GL_TEXTURE_WRAP_T */, 10496 /* GL_CLAMP */);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(3553 /* GL_TEXTURE_2D */);
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapDisabled);
	}

	private void updateTorchFlicker() {
		this.torchFlickerDX = (float) ((double) this.torchFlickerDX + (Math.random() - Math.random()) * Math.random() * Math.random());
		this.torchFlickerDY = (float) ((double) this.torchFlickerDY + (Math.random() - Math.random()) * Math.random() * Math.random());
		this.torchFlickerDX = (float) ((double) this.torchFlickerDX * 0.9D);
		this.torchFlickerDY = (float) ((double) this.torchFlickerDY * 0.9D);
		this.torchFlickerX += (this.torchFlickerDX - this.torchFlickerX) * 1.0F;
		this.torchFlickerY += (this.torchFlickerDY - this.torchFlickerY) * 1.0F;
		this.lightmapUpdateNeeded = true;
	}

	private void updateLightmap() {
		World var1 = this.mc.theWorld;
		if (var1 != null) {
			for (int var2 = 0; var2 < 256; ++var2) {
				float var3 = var1.func_35464_b(1.0F) * 0.95F + 0.05F;
				float var4 = var1.worldProvider.lightBrightnessTable[var2 / 16] * var3;
				float var5 = var1.worldProvider.lightBrightnessTable[var2 % 16] * (this.torchFlickerX * 0.1F + 1.5F);
				if (var1.lightningFlash > 0) {
					var4 = var1.worldProvider.lightBrightnessTable[var2 / 16];
				}

				float var6 = var4 * (var1.func_35464_b(1.0F) * 0.65F + 0.35F);
				float var7 = var4 * (var1.func_35464_b(1.0F) * 0.65F + 0.35F);
				float var10 = var5 * ((var5 * 0.6F + 0.4F) * 0.6F + 0.4F);
				float var11 = var5 * (var5 * var5 * 0.6F + 0.4F);
				float var12 = var6 + var5;
				float var13 = var7 + var10;
				float var14 = var4 + var11;
				var12 = var12 * 0.96F + 0.03F;
				var13 = var13 * 0.96F + 0.03F;
				var14 = var14 * 0.96F + 0.03F;
				if (var1.worldProvider.worldType == 1) {
					var12 = 0.22F + var5 * 0.75F;
					var13 = 0.28F + var10 * 0.75F;
					var14 = 0.25F + var11 * 0.75F;
				}

				float var15 = this.mc.gameSettings.gammaSetting;
				if (var12 > 1.0F) {
					var12 = 1.0F;
				}

				if (var13 > 1.0F) {
					var13 = 1.0F;
				}

				if (var14 > 1.0F) {
					var14 = 1.0F;
				}

				float var16 = 1.0F - var12;
				float var17 = 1.0F - var13;
				float var18 = 1.0F - var14;
				var16 = 1.0F - var16 * var16 * var16 * var16;
				var17 = 1.0F - var17 * var17 * var17 * var17;
				var18 = 1.0F - var18 * var18 * var18 * var18;
				var12 = var12 * (1.0F - var15) + var16 * var15;
				var13 = var13 * (1.0F - var15) + var17 * var15;
				var14 = var14 * (1.0F - var15) + var18 * var15;
				var12 = var12 * 0.96F + 0.03F;
				var13 = var13 * 0.96F + 0.03F;
				var14 = var14 * 0.96F + 0.03F;
				if (var12 > 1.0F) {
					var12 = 1.0F;
				}

				if (var13 > 1.0F) {
					var13 = 1.0F;
				}

				if (var14 > 1.0F) {
					var14 = 1.0F;
				}

				if (var12 < 0.0F) {
					var12 = 0.0F;
				}

				if (var13 < 0.0F) {
					var13 = 0.0F;
				}

				if (var14 < 0.0F) {
					var14 = 0.0F;
				}

				short var19 = 255;
				int var20 = (int) (var12 * 255.0F);
				int var21 = (int) (var13 * 255.0F);
				int var22 = (int) (var14 * 255.0F);
				this.lightmapColors[var2] = var19 << 24 | var20 << 16 | var21 << 8 | var22;
			}

			this.mc.renderEngine.createTextureFromBytes(this.lightmapColors, 16, 16, this.emptyTexture);
		}
	}

	public void updateCameraAndRender(float var1) {
		// Spout Start
		World world = this.mc.theWorld;
		RenderBlocks.fancyGrass = ConfigReader.fancyGrass;
		if (ConfigReader.betterGrass == 2) {
			RenderBlocks.fancyGrass = true;
		}

		Block.leaves.setGraphicsLevel(ConfigReader.fancyTrees);

		if (!ConfigReader.weather && world != null && world.worldInfo != null) {
			world.worldInfo.setIsRaining(false);
		}

		if (world != null) {
			long rawTime = world.getWorldTime();
			long time = rawTime % 24000L;
			if (ConfigReader.time == 2) { // day
				if (time <= 1000L) {
					world.worldInfo.setWorldTime(rawTime - time + 1001L);
				}

				if (time >= 11000L) {
					world.worldInfo.setWorldTime(rawTime - time + 24001L);
				}
			}

			else if (ConfigReader.time == 1) { // night
				if (time <= 14000L) {
					world.worldInfo.setWorldTime(rawTime - time + 14001L);
				}

				if (time >= 22000L) {
					world.worldInfo.setWorldTime(rawTime - time + 24000L + 14001L);
				}
			}
		}
		// Spout End
		Profiler.startSection("lightTex");
		if (this.lightmapUpdateNeeded) {
			this.updateLightmap();
		}

		Profiler.endSection();
		if (!Display.isActive()) {
			if (System.currentTimeMillis() - this.prevFrameTime > 500L) {
				this.mc.displayInGameMenu();
			}
		} else {
			this.prevFrameTime = System.currentTimeMillis();
		}

		Profiler.startSection("mouse");
		if (this.mc.inGameHasFocus) {
			this.mc.mouseHelper.mouseXYChange();
			float var2 = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
			float var3 = var2 * var2 * var2 * 8.0F;
			float var4 = (float) this.mc.mouseHelper.deltaX * var3;
			float var5 = (float) this.mc.mouseHelper.deltaY * var3;
			byte var6 = 1;
			if (this.mc.gameSettings.invertMouse) {
				var6 = -1;
			}

			if (this.mc.gameSettings.smoothCamera) {
				this.smoothCamYaw += var4;
				this.smoothCamPitch += var5;
				float var7 = var1 - this.smoothCamPartialTicks;
				this.smoothCamPartialTicks = var1;
				var4 = this.smoothCamFilterX * var7;
				var5 = this.smoothCamFilterY * var7;
				this.mc.thePlayer.setAngles(var4, var5 * (float) var6);
			} else {
				this.mc.thePlayer.setAngles(var4, var5 * (float) var6);
			}
		}

		Profiler.endSection();
		if (!this.mc.skipRenderWorld) {
			anaglyphEnable = this.mc.gameSettings.anaglyph;
			ScaledResolution var13 = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
			int var14 = var13.getScaledWidth();
			int var15 = var13.getScaledHeight();
			int var16 = Mouse.getX() * var14 / this.mc.displayWidth;
			int var17 = var15 - Mouse.getY() * var15 / this.mc.displayHeight - 1;
			short var18 = 200;
			if (this.mc.gameSettings.limitFramerate == 1) {
				var18 = 120;
			}

			if (this.mc.gameSettings.limitFramerate == 2) {
				var18 = 40;
			}

			long var8;
			if (this.mc.theWorld != null) {
				Profiler.startSection("level");
				if (this.mc.gameSettings.limitFramerate == 0) {
					this.renderWorld(var1, 0L);
				} else {
					this.renderWorld(var1, this.renderEndNanoTime + (long) (1000000000 / var18));
				}

				Profiler.endStartSection("sleep");
				// Spout start
				// Fix Balanced not limiting framerate properly
				if (this.mc.gameSettings.limitFramerate == 1 || this.mc.gameSettings.limitFramerate == 2) {
					// Spout end
					var8 = (this.renderEndNanoTime + (long) (1000000000 / var18) - System.nanoTime()) / 1000000L;
					if (var8 > 0L && var8 < 500L) {
						try {
							Thread.sleep(var8);
						} catch (InterruptedException var12) {
							var12.printStackTrace();
						}
					}
				}

				this.renderEndNanoTime = System.nanoTime();
				Profiler.endStartSection("gui");
				if (!this.mc.gameSettings.hideGUI || this.mc.currentScreen != null) {
					// Spout Start
					if (ConfigReader.fastDebug != 0) {
						if (Minecraft.isDebugInfoEnabled()) {
							this.showDebugInfo = !this.showDebugInfo;
						}

						if (this.showDebugInfo) {
							this.mc.gameSettings.showDebugInfo = true;
						}
					}

					this.mc.ingameGUI.renderGameOverlay(var1, this.mc.currentScreen != null, var16, var17);
					if (ConfigReader.fastDebug != 0) {
						this.mc.gameSettings.showDebugInfo = false;
					}
					// Spout End
				}

				Profiler.endSection();
			} else {
				GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
				GL11.glMatrixMode(5889 /* GL_PROJECTION */);
				GL11.glLoadIdentity();
				GL11.glMatrixMode(5888 /* GL_MODELVIEW0_ARB */);
				GL11.glLoadIdentity();
				this.setupOverlayRendering();
				if (this.mc.gameSettings.limitFramerate == 2) {
					var8 = (this.renderEndNanoTime + (long) (1000000000 / var18) - System.nanoTime()) / 1000000L;
					if (var8 < 0L) {
						var8 += 10L;
					}

					if (var8 > 0L && var8 < 500L) {
						try {
							Thread.sleep(var8);
						} catch (InterruptedException var11) {
							var11.printStackTrace();
						}
					}
				}

				this.renderEndNanoTime = System.nanoTime();
			}

			if (this.mc.currentScreen != null) {
				GL11.glClear(256);
				// Spout Start
				this.mc.currentScreen.drawScreenPre(var16, var17, var1);
				// Spout End
				if (this.mc.currentScreen != null && this.mc.currentScreen.guiParticles != null) {
					this.mc.currentScreen.guiParticles.draw(var1);
				}
			}

		}
	}

	public void renderWorld(float var1, long var2) {
		Profiler.startSection("lightTex");
		if (this.lightmapUpdateNeeded) {
			this.updateLightmap();
		}

		GL11.glEnable(2884 /* GL_CULL_FACE */);
		GL11.glEnable(2929 /* GL_DEPTH_TEST */);
		if (this.mc.renderViewEntity == null) {
			this.mc.renderViewEntity = this.mc.thePlayer;
		}

		Profiler.endStartSection("pick");
		this.getMouseOver(var1);
		EntityLiving var4 = this.mc.renderViewEntity;
		RenderGlobal var5 = this.mc.renderGlobal;
		EffectRenderer var6 = this.mc.effectRenderer;
		double var7 = var4.lastTickPosX + (var4.posX - var4.lastTickPosX) * (double) var1;
		double var9 = var4.lastTickPosY + (var4.posY - var4.lastTickPosY) * (double) var1;
		double var11 = var4.lastTickPosZ + (var4.posZ - var4.lastTickPosZ) * (double) var1;
		Profiler.endStartSection("center");
		IChunkProvider var13 = this.mc.theWorld.getIChunkProvider();
		int var16;
		if (var13 instanceof ChunkProviderLoadOrGenerate) {
			ChunkProviderLoadOrGenerate var14 = (ChunkProviderLoadOrGenerate) var13;
			int var15 = MathHelper.floor_float((float) ((int) var7)) >> 4;
			var16 = MathHelper.floor_float((float) ((int) var11)) >> 4;
			var14.setCurrentChunkOver(var15, var16);
		}

		for (int var19 = 0; var19 < 2; ++var19) {
			if (this.mc.gameSettings.anaglyph) {
				anaglyphField = var19;
				if (anaglyphField == 0) {
					GL11.glColorMask(false, true, true, false);
				} else {
					GL11.glColorMask(true, false, false, false);
				}
			}

			Profiler.endStartSection("clear");
			GL11.glViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
			this.updateFogColor(var1);
			GL11.glClear(16640);
			GL11.glEnable(2884 /* GL_CULL_FACE */);
			Profiler.endStartSection("camera");
			this.setupCameraTransform(var1, var19);
			ActiveRenderInfo.func_41067_a(this.mc.thePlayer, this.mc.gameSettings.thirdPersonView == 2);
			Profiler.endStartSection("frustrum");
			ClippingHelperImpl.getInstance();
			if (this.mc.gameSettings.renderDistance < 2 || ConfigReader.farView) { // Spout
				this.setupFog(-1, var1);
				Profiler.endStartSection("sky");
				var5.renderSky(var1);
			}

			GL11.glEnable(2912 /* GL_FOG */);
			this.setupFog(1, var1);
			if (this.mc.gameSettings.ambientOcclusion) {
				GL11.glShadeModel(7425 /* GL_SMOOTH */);
			}

			Profiler.endStartSection("culling");
			Frustrum var18 = new Frustrum();
			var18.setPosition(var7, var9, var11);
			this.mc.renderGlobal.clipRenderersByFrustrum(var18, var1);
			if (var19 == 0) {
				Profiler.endStartSection("updatechunks");
				this.mc.renderGlobal.updateRenderers(var4, false);
				//while (!this.mc.renderGlobal.updateRenderers(var4, false) && var2 != 0L) {
				//	long var21 = var2 - System.nanoTime();
				//	if (var21 < 0L || var21 > 1000000000L) {
				//		break;
				//	}
				//}
			}

			this.setupFog(0, var1);
			GL11.glEnable(2912 /* GL_FOG */);
			GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, this.mc.renderEngine.getTexture("/terrain.png"));
			RenderHelper.disableStandardItemLighting();
			Profiler.endStartSection("terrain");
			var5.sortAndRender(var4, 0, (double) var1);
			GL11.glShadeModel(7424 /* GL_FLAT */);
			EntityPlayer var20;
			if (this.debugViewDirection == 0) {
				RenderHelper.enableStandardItemLighting();
				Profiler.endStartSection("entities");
				var5.renderEntities(var4.getPosition(var1), var18, var1);
				this.enableLightmap((double) var1);
				Profiler.endStartSection("litParticles");
				var6.func_1187_b(var4, var1);
				RenderHelper.disableStandardItemLighting();
				this.setupFog(0, var1);
				Profiler.endStartSection("particles");
				var6.renderParticles(var4, var1);
				this.disableLightmap((double) var1);
				if (this.mc.objectMouseOver != null && var4.isInsideOfMaterial(Material.water) && var4 instanceof EntityPlayer) {
					var20 = (EntityPlayer) var4;
					GL11.glDisable(3008 /* GL_ALPHA_TEST */);
					Profiler.endStartSection("outline");
					var5.drawBlockBreaking(var20, this.mc.objectMouseOver, 0, var20.inventory.getCurrentItem(), var1);
					var5.drawSelectionBox(var20, this.mc.objectMouseOver, 0, var20.inventory.getCurrentItem(), var1);
					GL11.glEnable(3008 /* GL_ALPHA_TEST */);
				}
			}

			GL11.glDisable(3042 /* GL_BLEND */);
			GL11.glEnable(2884 /* GL_CULL_FACE */);
			GL11.glBlendFunc(770, 771);
			GL11.glDepthMask(true);
			this.setupFog(0, var1);
			GL11.glEnable(3042 /* GL_BLEND */);
			GL11.glDisable(2884 /* GL_CULL_FACE */);
			GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, this.mc.renderEngine.getTexture("/terrain.png"));
			if (ConfigReader.fancyWater) { // Spout fancy water
				Profiler.endStartSection("water");
				if (this.mc.gameSettings.ambientOcclusion) {
					GL11.glShadeModel(7425 /* GL_SMOOTH */);
				}

				GL11.glColorMask(false, false, false, false);
				var16 = var5.sortAndRender(var4, 1, (double) var1);
				if (this.mc.gameSettings.anaglyph) {
					if (anaglyphField == 0) {
						GL11.glColorMask(false, true, true, true);
					} else {
						GL11.glColorMask(true, false, false, true);
					}
				} else {
					GL11.glColorMask(true, true, true, true);
				}

				if (var16 > 0) {
					//Spout start
					var5.renderAllRenderLists(1, (double) var1);
					//var5.renderAllSortedRenderers(1, (double) var1);
					//Spout end
				}

				GL11.glShadeModel(7424 /* GL_FLAT */);
			} else {
				Profiler.endStartSection("water");
				var5.sortAndRender(var4, 1, (double) var1);
			}

			GL11.glDepthMask(true);
			GL11.glEnable(2884 /* GL_CULL_FACE */);
			GL11.glDisable(3042 /* GL_BLEND */);
			if (this.cameraZoom == 1.0D && var4 instanceof EntityPlayer && this.mc.objectMouseOver != null && !var4.isInsideOfMaterial(Material.water)) {
				var20 = (EntityPlayer) var4;
				GL11.glDisable(3008 /* GL_ALPHA_TEST */);
				Profiler.endStartSection("outline");
				var5.drawBlockBreaking(var20, this.mc.objectMouseOver, 0, var20.inventory.getCurrentItem(), var1);
				var5.drawSelectionBox(var20, this.mc.objectMouseOver, 0, var20.inventory.getCurrentItem(), var1);
				GL11.glEnable(3008 /* GL_ALPHA_TEST */);
			}

			Profiler.endStartSection("weather");
			this.renderRainSnow(var1);
			GL11.glDisable(2912 /* GL_FOG */);
			if (this.pointedEntity != null) {
				;
			}

			if (this.mc.gameSettings.shouldRenderClouds()) {
				Profiler.endStartSection("clouds");
				GL11.glPushMatrix();
				this.setupFog(0, var1);
				GL11.glEnable(2912 /* GL_FOG */);
				var5.renderClouds(var1);
				GL11.glDisable(2912 /* GL_FOG */);
				this.setupFog(1, var1);
				GL11.glPopMatrix();
			}

			Profiler.endStartSection("hand");
			if (this.cameraZoom == 1.0D) {
				GL11.glClear(256);
				this.renderHand(var1, var19);
			}

			if (!this.mc.gameSettings.anaglyph) {
				Profiler.endSection();
				return;
			}
		}

		GL11.glColorMask(true, true, true, false);
		Profiler.endSection();
	}

	private void addRainParticles() {
		float var1 = this.mc.theWorld.getRainStrength(1.0F);
		// Spout Start
		if (!ConfigReader.fancyWeather) {
			var1 /= 2.0F;
		}
		if (!ConfigReader.weather) {
			return;
		}
		// Spout End

		if (var1 != 0.0F) {
			this.random.setSeed((long) this.rendererUpdateCount * 312987231L);
			EntityLiving var2 = this.mc.renderViewEntity;
			World var3 = this.mc.theWorld;
			int var4 = MathHelper.floor_double(var2.posX);
			int var5 = MathHelper.floor_double(var2.posY);
			int var6 = MathHelper.floor_double(var2.posZ);
			byte var7 = 10;
			double var8 = 0.0D;
			double var10 = 0.0D;
			double var12 = 0.0D;
			int var14 = 0;
			int var15 = (int) (100.0F * var1 * var1);
			if (this.mc.gameSettings.field_41087_P == 1) {
				var15 >>= 1;
			} else if (this.mc.gameSettings.field_41087_P == 2) {
				var15 = 0;
			}

			for (int var16 = 0; var16 < var15; ++var16) {
				int var17 = var4 + this.random.nextInt(var7) - this.random.nextInt(var7);
				int var18 = var6 + this.random.nextInt(var7) - this.random.nextInt(var7);
				int var19 = var3.func_35461_e(var17, var18);
				int var20 = var3.getBlockId(var17, var19 - 1, var18);
				if (var19 <= var5 + var7 && var19 >= var5 - var7 && var3.getWorldChunkManager().getBiomeGenAt(var17, var18).canSpawnLightningBolt() && var3.getWorldChunkManager().func_35554_b(var17, var19, var18) > 0.2F) {
					float var21 = this.random.nextFloat();
					float var22 = this.random.nextFloat();
					if (var20 > 0) {
						if (Block.blocksList[var20].blockMaterial == Material.lava) {
							this.mc.effectRenderer.addEffect(new EntitySmokeFX(var3, (double) ((float) var17 + var21), (double) ((float) var19 + 0.1F) - Block.blocksList[var20].minY, (double) ((float) var18 + var22), 0.0D, 0.0D, 0.0D));
						} else {
							++var14;
							if (this.random.nextInt(var14) == 0) {
								var8 = (double) ((float) var17 + var21);
								var10 = (double) ((float) var19 + 0.1F) - Block.blocksList[var20].minY;
								var12 = (double) ((float) var18 + var22);
							}

							this.mc.effectRenderer.addEffect(new EntityRainFX(var3, (double) ((float) var17 + var21), (double) ((float) var19 + 0.1F) - Block.blocksList[var20].minY, (double) ((float) var18 + var22)));
						}
					}
				}
			}

			if (var14 > 0 && this.random.nextInt(3) < this.rainSoundCounter++) {
				this.rainSoundCounter = 0;
				if (var10 > var2.posY + 1.0D && var3.func_35461_e(MathHelper.floor_double(var2.posX), MathHelper.floor_double(var2.posZ)) > MathHelper.floor_double(var2.posY)) {
					this.mc.theWorld.playSoundEffect(var8, var10, var12, "ambient.weather.rain", 0.1F, 0.5F);
				} else {
					this.mc.theWorld.playSoundEffect(var8, var10, var12, "ambient.weather.rain", 0.2F, 1.0F);
				}
			}

		}
	}

	protected void renderRainSnow(float var1) {
		float var2 = this.mc.theWorld.getRainStrength(var1);
		if (var2 > 0.0F) {
			// Spout Start
			if (ConfigReader.weather) {
				this.enableLightmap((double) var1);
				if (this.rainXCoords == null) {
					this.rainXCoords = new float[1024 /* GL_FRONT_LEFT */];
					this.rainYCoords = new float[1024 /* GL_FRONT_LEFT */];

					for (int var3 = 0; var3 < 32; ++var3) {
						for (int var4 = 0; var4 < 32; ++var4) {
							float var5 = (float) (var4 - 16);
							float var6 = (float) (var3 - 16);
							float var7 = MathHelper.sqrt_float(var5 * var5 + var6 * var6);
							this.rainXCoords[var3 << 5 | var4] = -var6 / var7;
							this.rainYCoords[var3 << 5 | var4] = var5 / var7;
						}
					}
				}

				EntityLiving var43 = this.mc.renderViewEntity;
				World var44 = this.mc.theWorld;
				int var45 = MathHelper.floor_double(var43.posX);
				int var46 = MathHelper.floor_double(var43.posY);
				int var47 = MathHelper.floor_double(var43.posZ);
				Tessellator var8 = Tessellator.instance;
				GL11.glDisable(2884 /* GL_CULL_FACE */);
				GL11.glNormal3f(0.0F, 1.0F, 0.0F);
				GL11.glEnable(3042 /* GL_BLEND */);
				GL11.glBlendFunc(770, 771);
				GL11.glAlphaFunc(516, 0.01F);
				GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, this.mc.renderEngine.getTexture("/environment/snow.png"));
				double var9 = var43.lastTickPosX + (var43.posX - var43.lastTickPosX) * (double) var1;
				double var11 = var43.lastTickPosY + (var43.posY - var43.lastTickPosY) * (double) var1;
				double var13 = var43.lastTickPosZ + (var43.posZ - var43.lastTickPosZ) * (double) var1;
				int var15 = MathHelper.floor_double(var11);
				byte var16 = 5;
				if (ConfigReader.fancyWeather) {
					var16 = 10;
				}

				BiomeGenBase[] var17 = var44.getWorldChunkManager().func_4069_a(var45 - var16, var47 - var16, var16 * 2 + 1, var16 * 2 + 1);
				float[] var18 = var44.getWorldChunkManager().func_40539_b(var45 - var16, var47 - var16, var16 * 2 + 1, var16 * 2 + 1);
				boolean var19 = false;
				byte var20 = -1;
				float var21 = (float) this.rendererUpdateCount + var1;
				if (ConfigReader.fancyWeather) {
					var16 = 10;
				}

				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				int var48 = 0;

				for (int var22 = var47 - var16; var22 <= var47 + var16; ++var22) {
					for (int var23 = var45 - var16; var23 <= var45 + var16; ++var23) {
						int var24 = (var22 - var47 + 16) * 32 + var23 - var45 + 16;
						float var25 = this.rainXCoords[var24] * 0.5F;
						float var26 = this.rainYCoords[var24] * 0.5F;
						BiomeGenBase var27 = var17[var48++];
						if (SpoutClient.getInstance().getBiomeManager().getSnowChanged(var27.getBiomeName())) {
							var27.setEnableSnow(SpoutClient.getInstance().getBiomeManager().getSnowEnabled(var27.getBiomeName()));
						}
						if (var27.canSpawnLightningBolt() || var27.getEnableSnow()) {
							int var28 = var44.func_35461_e(var23, var22);
							int var29 = var46 - var16;
							int var30 = var46 + var16;
							if (var29 < var28) {
								var29 = var28;
							}

							if (var30 < var28) {
								var30 = var28;
							}

							float var31 = 1.0F;
							int var32 = var28;
							if (var28 < var15) {
								var32 = var15;
							}

							if (var29 != var30) {
								this.random.setSeed((long) (var23 * var23 * 3121 /* GL_RGBA_MODE */+ var23 * 45238971 ^ var22 * var22 * 418711 + var22 * 13761));
								float var33 = var18[var48 - 1];
								float var34;
								double var37;
								if (var44.getWorldChunkManager().func_40540_a(var33, var28) >= 0.15F) {
									if (var20 != 0) {
										if (var20 >= 0) {
											var8.draw();
										}

										var20 = 0;
										GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, this.mc.renderEngine.getTexture("/environment/rain.png"));
										var8.startDrawingQuads();
									}

									var34 = ((float) (this.rendererUpdateCount + var23 * var23 * 3121 /* GL_RGBA_MODE */+ var23 * 45238971 + var22 * var22 * 418711 + var22 * 13761 & 31) + var1) / 32.0F * (3.0F + this.random.nextFloat());
									double var35 = (double) ((float) var23 + 0.5F) - var43.posX;
									var37 = (double) ((float) var22 + 0.5F) - var43.posZ;
									float var39 = MathHelper.sqrt_double(var35 * var35 + var37 * var37) / (float) var16;
									float var40 = 1.0F;
									var8.setBrightness(var44.getLightBrightnessForSkyBlocks(var23, var32, var22, 0));
									var8.setColorRGBA_F(var40, var40, var40, ((1.0F - var39 * var39) * 0.5F + 0.5F) * var2);
									var8.setTranslationD(-var9 * 1.0D, -var11 * 1.0D, -var13 * 1.0D);
									var8.addVertexWithUV((double) ((float) var23 - var25) + 0.5D, (double) var29, (double) ((float) var22 - var26) + 0.5D, (double) (0.0F * var31), (double) ((float) var29 * var31 / 4.0F + var34 * var31));
									var8.addVertexWithUV((double) ((float) var23 + var25) + 0.5D, (double) var29, (double) ((float) var22 + var26) + 0.5D, (double) (1.0F * var31), (double) ((float) var29 * var31 / 4.0F + var34 * var31));
									var8.addVertexWithUV((double) ((float) var23 + var25) + 0.5D, (double) var30, (double) ((float) var22 + var26) + 0.5D, (double) (1.0F * var31), (double) ((float) var30 * var31 / 4.0F + var34 * var31));
									var8.addVertexWithUV((double) ((float) var23 - var25) + 0.5D, (double) var30, (double) ((float) var22 - var26) + 0.5D, (double) (0.0F * var31), (double) ((float) var30 * var31 / 4.0F + var34 * var31));
									var8.setTranslationD(0.0D, 0.0D, 0.0D);
								} else {
									if (var20 != 1) {
										if (var20 >= 0) {
											var8.draw();
										}

										var20 = 1;
										GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, this.mc.renderEngine.getTexture("/environment/snow.png"));
										var8.startDrawingQuads();
									}

									var34 = ((float) (this.rendererUpdateCount & 511) + var1) / 512.0F;
									float var49 = this.random.nextFloat() + var21 * 0.01F * (float) this.random.nextGaussian();
									float var36 = this.random.nextFloat() + var21 * (float) this.random.nextGaussian() * 0.0010F;
									var37 = (double) ((float) var23 + 0.5F) - var43.posX;
									double var50 = (double) ((float) var22 + 0.5F) - var43.posZ;
									float var41 = MathHelper.sqrt_double(var37 * var37 + var50 * var50) / (float) var16;
									float var42 = 1.0F;
									var8.setBrightness((var44.getLightBrightnessForSkyBlocks(var23, var32, var22, 0) * 3 + 15728880) / 4);
									var8.setColorRGBA_F(var42, var42, var42, ((1.0F - var41 * var41) * 0.3F + 0.5F) * var2);
									var8.setTranslationD(-var9 * 1.0D, -var11 * 1.0D, -var13 * 1.0D);
									var8.addVertexWithUV((double) ((float) var23 - var25) + 0.5D, (double) var29, (double) ((float) var22 - var26) + 0.5D, (double) (0.0F * var31 + var49), (double) ((float) var29 * var31 / 4.0F + var34 * var31 + var36));
									var8.addVertexWithUV((double) ((float) var23 + var25) + 0.5D, (double) var29, (double) ((float) var22 + var26) + 0.5D, (double) (1.0F * var31 + var49), (double) ((float) var29 * var31 / 4.0F + var34 * var31 + var36));
									var8.addVertexWithUV((double) ((float) var23 + var25) + 0.5D, (double) var30, (double) ((float) var22 + var26) + 0.5D, (double) (1.0F * var31 + var49), (double) ((float) var30 * var31 / 4.0F + var34 * var31 + var36));
									var8.addVertexWithUV((double) ((float) var23 - var25) + 0.5D, (double) var30, (double) ((float) var22 - var26) + 0.5D, (double) (0.0F * var31 + var49), (double) ((float) var30 * var31 / 4.0F + var34 * var31 + var36));
									var8.setTranslationD(0.0D, 0.0D, 0.0D);
								}
							}
						}
					}
				}

				if (var20 >= 0) {
					var8.draw();
				}

				GL11.glEnable(2884 /* GL_CULL_FACE */);
				GL11.glDisable(3042 /* GL_BLEND */);
				GL11.glAlphaFunc(516, 0.1F);
				this.disableLightmap((double) var1);
			}
			//Spout end
		}
	}

	public void setupOverlayRendering() {
		ScaledResolution var1 = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
		GL11.glClear(256);
		GL11.glMatrixMode(5889 /* GL_PROJECTION */);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, var1.scaledWidthD, var1.scaledHeightD, 0.0D, 1000.0D, 3000.0D);
		GL11.glMatrixMode(5888 /* GL_MODELVIEW0_ARB */);
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
	}

	private void updateFogColor(float var1) {
		World var2 = this.mc.theWorld;
		EntityLiving var3 = this.mc.renderViewEntity;
		float var4 = 1.0F / (float) (4 - this.mc.gameSettings.renderDistance);
		var4 = 1.0F - (float) Math.pow((double) var4, 0.25D);
		Vec3D var5 = var2.getSkyColor(this.mc.renderViewEntity, var1);
		float var6 = (float) var5.xCoord;
		float var7 = (float) var5.yCoord;
		float var8 = (float) var5.zCoord;
		Vec3D var9 = var2.getFogColor(var1);
		this.fogColorRed = (float) var9.xCoord;
		this.fogColorGreen = (float) var9.yCoord;
		this.fogColorBlue = (float) var9.zCoord;
		float var11;
		if (this.mc.gameSettings.renderDistance < 2) {
			Vec3D var10 = MathHelper.sin(var2.getCelestialAngleRadians(var1)) > 0.0F ? Vec3D.createVector(-1.0D, 0.0D, 0.0D) : Vec3D.createVector(1.0D, 0.0D, 0.0D);
			var11 = (float) var3.getLook(var1).dotProduct(var10);
			if (var11 < 0.0F) {
				var11 = 0.0F;
			}

			if (var11 > 0.0F) {
				float[] var12 = var2.worldProvider.calcSunriseSunsetColors(var2.getCelestialAngle(var1), var1);
				if (var12 != null) {
					var11 *= var12[3];
					this.fogColorRed = this.fogColorRed * (1.0F - var11) + var12[0] * var11;
					this.fogColorGreen = this.fogColorGreen * (1.0F - var11) + var12[1] * var11;
					this.fogColorBlue = this.fogColorBlue * (1.0F - var11) + var12[2] * var11;
				}
			}
		}

		this.fogColorRed += (var6 - this.fogColorRed) * var4;
		this.fogColorGreen += (var7 - this.fogColorGreen) * var4;
		this.fogColorBlue += (var8 - this.fogColorBlue) * var4;
		float var19 = var2.getRainStrength(var1);
		float var20;
		if (var19 > 0.0F) {
			var11 = 1.0F - var19 * 0.5F;
			var20 = 1.0F - var19 * 0.4F;
			this.fogColorRed *= var11;
			this.fogColorGreen *= var11;
			this.fogColorBlue *= var20;
		}

		var11 = var2.getWeightedThunderStrength(var1);
		if (var11 > 0.0F) {
			var20 = 1.0F - var11 * 0.5F;
			this.fogColorRed *= var20;
			this.fogColorGreen *= var20;
			this.fogColorBlue *= var20;
		}

		int var21 = ActiveRenderInfo.func_41066_a(this.mc.theWorld, var3, var1);
		if (this.cloudFog) {
			Vec3D var13 = var2.drawClouds(var1);
			this.fogColorRed = (float) var13.xCoord;
			this.fogColorGreen = (float) var13.yCoord;
			this.fogColorBlue = (float) var13.zCoord;
		} else if (var21 != 0 && Block.blocksList[var21].blockMaterial == Material.water) {
			this.fogColorRed = 0.02F;
			this.fogColorGreen = 0.02F;
			this.fogColorBlue = 0.2F;
		} else if (var21 != 0 && Block.blocksList[var21].blockMaterial == Material.lava) {
			this.fogColorRed = 0.6F;
			this.fogColorGreen = 0.1F;
			this.fogColorBlue = 0.0F;
		}

		float var22 = this.fogColor2 + (this.fogColor1 - this.fogColor2) * var1;
		this.fogColorRed *= var22;
		this.fogColorGreen *= var22;
		this.fogColorBlue *= var22;
		double var14 = (var3.lastTickPosY + (var3.posY - var3.lastTickPosY) * (double) var1) / 32.0D;
		if (var3.isPotionActive(Potion.potionBlindness)) {
			int var16 = var3.getActivePotionEffect(Potion.potionBlindness).getDuration();
			if (var16 < 20) {
				var14 *= (double) (1.0F - (float) var16 / 20.0F);
			} else {
				var14 = 0.0D;
			}
		}

		if (var14 < 1.0D) {
			if (var14 < 0.0D) {
				var14 = 0.0D;
			}

			var14 *= var14;
			this.fogColorRed = (float) ((double) this.fogColorRed * var14);
			this.fogColorGreen = (float) ((double) this.fogColorGreen * var14);
			this.fogColorBlue = (float) ((double) this.fogColorBlue * var14);
		}

		if (this.mc.gameSettings.anaglyph) {
			float var23 = (this.fogColorRed * 30.0F + this.fogColorGreen * 59.0F + this.fogColorBlue * 11.0F) / 100.0F;
			float var17 = (this.fogColorRed * 30.0F + this.fogColorGreen * 70.0F) / 100.0F;
			float var18 = (this.fogColorRed * 30.0F + this.fogColorBlue * 70.0F) / 100.0F;
			this.fogColorRed = var23;
			this.fogColorGreen = var17;
			this.fogColorBlue = var18;
		}

		GL11.glClearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 0.0F);
	}

	private void setupFog(int var1, float var2) {
		EntityLiving var3 = this.mc.renderViewEntity;
		if (var1 == 999) {
			GL11.glFog(2918 /* GL_FOG_COLOR */, this.setFogColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
			GL11.glFogi(2917 /* GL_FOG_MODE */, 9729 /* GL_LINEAR */);
			GL11.glFogf(2915 /* GL_FOG_START */, 0.0F);
			GL11.glFogf(2916 /* GL_FOG_END */, 8.0F);
			if (GLContext.getCapabilities().GL_NV_fog_distance) {
				GL11.glFogi('\u855a', '\u855b');
			}

			GL11.glFogf(2915 /* GL_FOG_START */, 0.0F);
		} else {
			GL11.glFog(2918 /* GL_FOG_COLOR */, this.setFogColorBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
			GL11.glNormal3f(0.0F, -1.0F, 0.0F);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			int var4 = ActiveRenderInfo.func_41066_a(this.mc.theWorld, var3, var2);
			float var5;
			if (var3.isPotionActive(Potion.potionBlindness)) {
				var5 = 5.0F;
				int var6 = var3.getActivePotionEffect(Potion.potionBlindness).getDuration();
				if (var6 < 20) {
					var5 = 5.0F + (this.farPlaneDistance - 5.0F) * (1.0F - (float) var6 / 20.0F);
				}

				GL11.glFogi(2917 /* GL_FOG_MODE */, 9729 /* GL_LINEAR */);
				if (var1 < 0) {
					GL11.glFogf(2915 /* GL_FOG_START */, 0.0F);
					GL11.glFogf(2916 /* GL_FOG_END */, var5 * 0.8F);
				} else {
					GL11.glFogf(2915 /* GL_FOG_START */, var5 * 0.25F);
					GL11.glFogf(2916 /* GL_FOG_END */, var5);
				}

				if (GLContext.getCapabilities().GL_NV_fog_distance) {
					GL11.glFogi('\u855a', '\u855b');
				}
			} else {
				float var7;
				float var8;
				float var9;
				float var10;
				float var12;
				if (this.cloudFog) {
					GL11.glFogi(2917 /* GL_FOG_MODE */, 2048 /* GL_EXP */);
					GL11.glFogf(2914 /* GL_FOG_DENSITY */, 0.1F);
					var5 = 1.0F;
					var12 = 1.0F;
					var7 = 1.0F;
					if (this.mc.gameSettings.anaglyph) {
						var8 = (var5 * 30.0F + var12 * 59.0F + var7 * 11.0F) / 100.0F;
						var9 = (var5 * 30.0F + var12 * 70.0F) / 100.0F;
						var10 = (var5 * 30.0F + var7 * 70.0F) / 100.0F;
					}
				} else if (var4 > 0 && Block.blocksList[var4].blockMaterial == Material.water) {
					GL11.glFogi(2917 /* GL_FOG_MODE */, 2048 /* GL_EXP */);
					// Spout start
					float density = 0.1F;
					if (var3.isPotionActive(Potion.potionWaterBreathing)) {
						density = 0.05F;
					}
					if (ConfigReader.clearWater) {
						density = 0.02F;
					}
					GL11.glFogf(2914 /* GL_FOG_DENSITY */, density);
					// Spout end
					var5 = 0.4F;
					var12 = 0.4F;
					var7 = 0.9F;
					if (this.mc.gameSettings.anaglyph) {
						var8 = (var5 * 30.0F + var12 * 59.0F + var7 * 11.0F) / 100.0F;
						var9 = (var5 * 30.0F + var12 * 70.0F) / 100.0F;
						var10 = (var5 * 30.0F + var7 * 70.0F) / 100.0F;
					}
				} else if (var4 > 0 && Block.blocksList[var4].blockMaterial == Material.lava) {
					GL11.glFogi(2917 /* GL_FOG_MODE */, 2048 /* GL_EXP */);
					GL11.glFogf(2914 /* GL_FOG_DENSITY */, 2.0F);
					var5 = 0.4F;
					var12 = 0.3F;
					var7 = 0.3F;
					if (this.mc.gameSettings.anaglyph) {
						var8 = (var5 * 30.0F + var12 * 59.0F + var7 * 11.0F) / 100.0F;
						var9 = (var5 * 30.0F + var12 * 70.0F) / 100.0F;
						var10 = (var5 * 30.0F + var7 * 70.0F) / 100.0F;
					}
				} else {
					var5 = this.farPlaneDistance;
					if (!this.mc.theWorld.worldProvider.hasNoSky) {
						double var11 = (double) ((var3.getEntityBrightnessForRender(var2) & 15728640) >> 20) / 16.0D + (var3.lastTickPosY + (var3.posY - var3.lastTickPosY) * (double) var2 + 4.0D) / 32.0D;
						if (var11 < 1.0D) {
							if (var11 < 0.0D) {
								var11 = 0.0D;
							}

							var11 *= var11;
							var8 = 100.0F * (float) var11;
							if (var8 < 5.0F) {
								var8 = 5.0F;
							}

							if (var5 > var8) {
								var5 = var8;
							}
							
						}
					}
					
					// Spout Start
					if (!ConfigReader.voidFog) {
						var5 = 0.8F * this.farPlaneDistance;
						var12 = this.farPlaneDistance;
					}
					// Spout End

					GL11.glFogi(2917 /* GL_FOG_MODE */, 9729 /* GL_LINEAR */);
					if (var1 < 0) {
						GL11.glFogf(2915 /* GL_FOG_START */, 0.0F);
						GL11.glFogf(2916 /* GL_FOG_END */, var5 * 0.8F);
					} else {
						GL11.glFogf(2915 /* GL_FOG_START */, var5 * 0.6F); //Spout changed fog start from 0.25F to 0.6F
						GL11.glFogf(2916 /* GL_FOG_END */, var5);
					}

					if (GLContext.getCapabilities().GL_NV_fog_distance) {
						// Spout start
						if (ConfigReader.fancyFog) {
							GL11.glFogi('\u855a', '\u855b');
						} else {
							GL11.glFogi('\u855a', '\u855c');
						}
						// Spout end
					}

					if (this.mc.theWorld.worldProvider.isNether) {
						GL11.glFogf(2915 /* GL_FOG_START */, 0.0F);
					}
				}
			}

			GL11.glEnable(2903 /* GL_COLOR_MATERIAL */);
			GL11.glColorMaterial(1028 /* GL_FRONT */, 4608 /* GL_AMBIENT */);
		}
	}

	private FloatBuffer setFogColorBuffer(float var1, float var2, float var3, float var4) {
		this.fogColorBuffer.clear();
		this.fogColorBuffer.put(var1).put(var2).put(var3).put(var4);
		this.fogColorBuffer.flip();
		return this.fogColorBuffer;
	}

}
