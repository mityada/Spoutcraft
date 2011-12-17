package net.minecraft.src;

import org.spoutcraft.client.entity.CraftExperienceOrb;
import org.spoutcraft.spoutcraftapi.Spoutcraft;
import org.spoutcraft.spoutcraftapi.material.CustomBlock;
import org.spoutcraft.spoutcraftapi.material.MaterialData;

import net.minecraft.src.Block;
import net.minecraft.src.DamageSource;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.MathHelper;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public class EntityXPOrb extends Entity {

	public int xpColor;
	public int xpOrbAge = 0;
	public int unusedPickupCooldown;
	private int xpOrbHealth = 5;
	public int xpValue; //Spout private -> public


	public EntityXPOrb(World var1, double var2, double var4, double var6, int var8) {
		super(var1);
		this.setSize(0.5F, 0.5F);
		this.yOffset = this.height / 2.0F;
		this.setPosition(var2, var4, var6);
		this.rotationYaw = (float)(Math.random() * 360.0D);
		this.motionX = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F);
		this.motionY = (double)((float)(Math.random() * 0.2D) * 2.0F);
		this.motionZ = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D) * 2.0F);
		this.xpValue = var8;
	}

	protected boolean canTriggerWalking() {
		return false;
	}

	public EntityXPOrb(World var1) {
		super(var1);
		this.setSize(0.25F, 0.25F);
		this.yOffset = this.height / 2.0F;
	}

	protected void entityInit() {}

	public int getEntityBrightnessForRender(float var1) {
		float var2 = 0.5F;
		if(var2 < 0.0F) {
			var2 = 0.0F;
		}

		if(var2 > 1.0F) {
			var2 = 1.0F;
		}

		int var3 = super.getEntityBrightnessForRender(var1);
		int var4 = var3 & 255;
		int var5 = var3 >> 16 & 255;
		var4 += (int)(var2 * 15.0F * 16.0F);
		if(var4 > 240) {
			var4 = 240;
		}

		return var4 | var5 << 16;
	}

	public void onUpdate() {
		super.onUpdate();
		if(this.unusedPickupCooldown > 0) {
			--this.unusedPickupCooldown;
		}

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY -= 0.029999999329447746D;
		if(this.worldObj.getBlockMaterial(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)) == Material.lava) {
			this.motionY = 0.20000000298023224D;
			this.motionX = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
			this.motionZ = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
			this.worldObj.playSoundAtEntity(this, "random.fizz", 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
		}

		this.pushOutOfBlocks(this.posX, (this.boundingBox.minY + this.boundingBox.maxY) / 2.0D, this.posZ);
		double var1 = 8.0D;
		EntityPlayer var3 = this.worldObj.getClosestPlayerToEntity(this, var1);
		if(var3 != null) {
			double var4 = (var3.posX - this.posX) / var1;
			double var6 = (var3.posY + (double)var3.getEyeHeight() - this.posY) / var1;
			double var8 = (var3.posZ - this.posZ) / var1;
			double var10 = Math.sqrt(var4 * var4 + var6 * var6 + var8 * var8);
			double var12 = 1.0D - var10;
			if(var12 > 0.0D) {
				var12 *= var12;
				this.motionX += var4 / var10 * var12 * 0.1D;
				this.motionY += var6 / var10 * var12 * 0.1D;
				this.motionZ += var8 / var10 * var12 * 0.1D;
			}
		}

		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		float var14 = 0.98F;
		if(this.onGround) {
			var14 = 0.58800006F;
			int var5 = this.worldObj.getBlockId(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.boundingBox.minY) - 1, MathHelper.floor_double(this.posZ));
			if(var5 > 0) {
				var14 = Block.blocksList[var5].slipperiness * 0.98F;
				//Spout start
				if (!worldObj.multiplayerWorld) {
					int x = MathHelper.floor_double(this.posX);
					int y = MathHelper.floor_double(this.boundingBox.minY) - 1;
					int z = MathHelper.floor_double(this.posZ);
					short customId = Spoutcraft.getWorld().getChunkAt(x, y, z).getCustomBlockId(x, y, z);
					if (customId > 0) {
						CustomBlock block = MaterialData.getCustomBlock(customId);
						if (block != null) {
							var14 = block.getFriction() * 0.98F;
						}
					}
				}
				//Spout end
			}
		}

		this.motionX *= (double)var14;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= (double)var14;
		if(this.onGround) {
			this.motionY *= -0.8999999761581421D;
		}

		++this.xpColor;
		++this.xpOrbAge;
		if(this.xpOrbAge >= 6000) {
			this.setEntityDead();
		}

	}

	public boolean handleWaterMovement() {
		return this.worldObj.handleMaterialAcceleration(this.boundingBox, Material.water, this);
	}

	protected void dealFireDamage(int var1) {
		this.attackEntityFrom(DamageSource.inFire, var1);
	}

	public boolean attackEntityFrom(DamageSource var1, int var2) {
		this.setBeenAttacked();
		this.xpOrbHealth -= var2;
		if(this.xpOrbHealth <= 0) {
			this.setEntityDead();
		}

		return false;
	}

	public void writeEntityToNBT(NBTTagCompound var1) {
		var1.setShort("Health", (short)((byte)this.xpOrbHealth));
		var1.setShort("Age", (short)this.xpOrbAge);
		var1.setShort("Value", (short)this.xpValue);
	}

	public void readEntityFromNBT(NBTTagCompound var1) {
		this.xpOrbHealth = var1.getShort("Health") & 255;
		this.xpOrbAge = var1.getShort("Age");
		this.xpValue = var1.getShort("Value");
	}

	public void onCollideWithPlayer(EntityPlayer var1) {
		if(!this.worldObj.multiplayerWorld) {
			if(this.unusedPickupCooldown == 0 && var1.xpCooldown == 0) {
				var1.xpCooldown = 2;
				this.worldObj.playSoundAtEntity(this, "random.orb", 0.1F, 0.5F * ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.8F));
				var1.onItemPickup(this, 1);
				var1.increaseXP(this.xpValue);
				this.setEntityDead();
			}

		}
	}

	public int getXpValue() {
		return this.xpValue;
	}

	public int getTextureByXP() {
		return this.xpValue >= 2477?10:(this.xpValue >= 1237?9:(this.xpValue >= 617?8:(this.xpValue >= 307?7:(this.xpValue >= 149?6:(this.xpValue >= 73?5:(this.xpValue >= 37?4:(this.xpValue >= 17?3:(this.xpValue >= 7?2:(this.xpValue >= 3?1:0)))))))));
	}

	public static int getXPSplit(int var0) {
		return var0 >= 2477?2477:(var0 >= 1237?1237:(var0 >= 617?617:(var0 >= 307?307:(var0 >= 149?149:(var0 >= 73?73:(var0 >= 37?37:(var0 >= 17?17:(var0 >= 7?7:(var0 >= 3?3:1)))))))));
	}
}
