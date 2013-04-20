package biomesoplenty.items;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBOPFoliage extends ItemBlock
{
    private static final String[] foliageTypes = new String[] {"algae", "shortgrass", "mediumgrass", "highgrass", "bush", "sprout", "highgrasstop"};
    @SideOnly(Side.CLIENT)
    private Icon[] textures;
    
    public ItemBOPFoliage(int par1)
    {
        super(par1);
        setMaxDamage(0);
        setHasSubtypes(true);
    }
    
    @SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister iconRegister)
    {
        textures = new Icon[foliageTypes.length - 1];
        
        for (int i = 0; i < foliageTypes.length - 1; ++i)
            textures[i] = iconRegister.registerIcon("BiomesOPlenty:item_" + foliageTypes[i]);
    }

    @Override
    public int getMetadata(int meta)
    {
        return meta & 15;
    }
    
    @Override
    public String getUnlocalizedName(ItemStack itemStack)
    {
        return (new StringBuilder()).append(foliageTypes[itemStack.getItemDamage()]).toString();
    }
    
    @Override
    public Icon getIconFromDamage(int meta)
    {
        if (meta == 6)
            meta = 3;
        return textures[meta];
    }
    
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
    {
        if (itemStack.getItemDamage() != 0)
            return super.onItemRightClick(itemStack, world, player);

        MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);

        if (movingobjectposition == null)
            return itemStack;
        else
        {
            if (movingobjectposition.typeOfHit == EnumMovingObjectType.TILE)
            {
                int i = movingobjectposition.blockX;
                int j = movingobjectposition.blockY;
                int k = movingobjectposition.blockZ;

                if (!world.canMineBlock(player, i, j, k))
                    return itemStack;

                if (!player.canPlayerEdit(i, j, k, movingobjectposition.sideHit, itemStack))
                    return itemStack;

                if (world.getBlockMaterial(i, j, k) == Material.water && world.getBlockMetadata(i, j, k) == 0 && world.isAirBlock(i, j + 1, k))
                {
                    world.setBlock(i, j + 1, k, itemStack.itemID, 0, 2);

                    if (!player.capabilities.isCreativeMode)
                    {
                        --itemStack.stackSize;
                    }
                }
            }

            return itemStack;
        }   
    }
    
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (itemStack.getItemDamage() == 0)
            return super.onItemUse(itemStack, player, world, x, y, z, side, hitX, hitY, hitZ);
                    
        int id = world.getBlockId(x, y, z);

        if (id == Block.snow.blockID && (world.getBlockMetadata(x, y, z) & 7) < 1)
            side = 1;
        else if (!Block.blocksList[id].isBlockReplaceable(world, x, y, z))
        {
            if (side == 0)
                --y;

            if (side == 1)
                ++y;

            if (side == 2)
                --z;

            if (side == 3)
                ++z;

            if (side == 4)
                --x;

            if (side == 5)
                ++x;
        }

        if (!player.canPlayerEdit(x, y, z, side, itemStack))
        {
            return false;
        }
        else if (itemStack.stackSize == 0)
        {
            return false;
        }
        else
        {
            if (world.canPlaceEntityOnSide(this.getBlockID(), x, y, z, false, side, (Entity)null, itemStack))
            {
                Block block = Block.blocksList[this.getBlockID()];
                int j1 = block.onBlockPlaced(world, x, y, z, side, hitX, hitY, hitZ, 0);

                if (world.setBlock(x, y, z, this.getBlockID(), itemStack.getItemDamage(), 3))
                {
                    if (itemStack.getItemDamage() == 3 && world.getBlockMaterial(x, y + 1, z).isReplaceable())
                        world.setBlock(x, y + 1, z, this.getBlockID(), 6, 2);
                    
                    if (world.getBlockId(x, y, z) == this.getBlockID())
                    {
                        Block.blocksList[this.getBlockID()].onBlockPlacedBy(world, x, y, z, player, itemStack);
                        Block.blocksList[this.getBlockID()].onPostBlockPlaced(world, x, y, z, j1);
                    }

                    world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), block.stepSound.getPlaceSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
                    --itemStack.stackSize;
                }
            }

            return true;
        }
    }
}
