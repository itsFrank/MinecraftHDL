package minecrafthdl.synthesis;

import com.google.common.collect.ImmutableMap;
import minecrafthdl.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Francis on 10/28/2016.
 */
public class Circuit {

    public static boolean TEST = false;

    ArrayList<ArrayList<ArrayList<IBlockState>>> blocks;
    HashMap<Vec3i, TileEntity> te_map = new HashMap<Vec3i, TileEntity>();

    public Circuit(int sizeX, int sizeY, int sizeZ){
        this.blocks = new ArrayList<ArrayList<ArrayList<IBlockState>>>();
        for (int x = 0; x < sizeX; x++) {
            this.blocks.add(new ArrayList<ArrayList<IBlockState>>());
            for (int y = 0; y < sizeY; y++) {
                this.blocks.get(x).add(new ArrayList<IBlockState>());
                for (int z = 0; z < sizeZ; z++) {
                    if (!Circuit.TEST) this.blocks.get(x).get(y).add(Blocks.AIR.getDefaultState());
                }
            }
        }
    }



    public void setBlock(int x, int y, int z, IBlockState blockstate) {
        if (TEST) return;
        this.blocks.get(x).get(y).set(z, blockstate);
    }

    public void placeInWorld(World worldIn, BlockPos pos, EnumFacing direction) {
        int width = blocks.size();
        int height = blocks.get(0).size();
        int length = blocks.get(0).get(0).size();

        int start_x = pos.getX();
        int start_y = pos.getY();
        int start_z = pos.getZ();

        if (direction == EnumFacing.NORTH){
            start_z += 2;
        } else if (direction == EnumFacing.SOUTH) {
            start_z -= length + 1;
        } else if (direction == EnumFacing.EAST){
            start_x -= width + 1;
        } else if (direction == EnumFacing.WEST) {
            start_x += 2;
        }

        int y = start_y - 1;
        for (int z = start_z - 1; z < start_z + length + 1; z ++){
            for (int x = start_x - 1; x < start_x + width + 1; x++){
                worldIn.setBlockState(new BlockPos(x, y, z), Blocks.STONEBRICK.getDefaultState());
            }
        }

        HashMap<Vec3i ,IBlockState> torches = new HashMap<Vec3i, IBlockState>();

        for (int i = 0; i < width; i++){
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < length; k++) {
                    if (this.getState(i, j, k).getBlock().getDefaultState() == Blocks.REDSTONE_TORCH.getDefaultState()) {
                        torches.put(new Vec3i(i, j, k), this.getState(i, j, k));
                    } else {
                        BlockPos blk_pos = new BlockPos(start_x + i, start_y + j, start_z + k);
                        worldIn.setBlockState(blk_pos, this.getState(i, j, k));

                        TileEntity te = this.te_map.get(new Vec3i(i, j, k));
                        if (te != null) {
                            worldIn.setTileEntity(blk_pos, te);
                        }
                    }
                }
            }
        }

        for (Map.Entry<Vec3i, IBlockState> set : torches.entrySet()){
            worldIn.setBlockState(new BlockPos(start_x + set.getKey().getX(), start_y + set.getKey().getY(), start_z + set.getKey().getZ()), set.getValue());
        }
    }

    public int getSizeX() {
        return this.blocks.size();
    }

    public int getSizeY() {
        return this.blocks.get(0).size();
    }

    public int getSizeZ() {
        return this.blocks.get(0).get(0).size();
    }

    public IBlockState getState(int x, int y, int z){
        return this.blocks.get(x).get(y).get(z);
    }

    public void insertCircuit(int x_offset, int y_offset, int z_offset, Circuit c) {
        for (int x = 0; x < c.getSizeX(); x++) {
            for (int y = 0; y < c.getSizeY(); y++) {
                for (int z = 0; z < c.getSizeZ(); z++) {
                    this.setBlock(x + x_offset, y + y_offset, z + z_offset, c.getState(x, y, z));

                    TileEntity te = c.te_map.get(new Vec3i(x, y, z));
                    if (te != null) {
                        this.te_map.put(new Vec3i(x + x_offset, y + y_offset, z + z_offset), te);
                    }
                }
            }
        }
    }

    public void rotateLeft() {
        HashMap<EnumFacing, EnumFacing> rot_facing = new HashMap<EnumFacing, EnumFacing>();
        rot_facing.put(EnumFacing.DOWN, EnumFacing.DOWN);
        rot_facing.put(EnumFacing.UP, EnumFacing.UP);
        rot_facing.put(EnumFacing.NORTH, EnumFacing.WEST);
        rot_facing.put(EnumFacing.SOUTH, EnumFacing.EAST);
        rot_facing.put(EnumFacing.WEST, EnumFacing.SOUTH);
        rot_facing.put(EnumFacing.EAST, EnumFacing.NORTH);

        ArrayList<ArrayList<ArrayList<IBlockState>>> new_blocks = new ArrayList<ArrayList<ArrayList<IBlockState>>>();

        int width = blocks.size();
        int height = blocks.get(0).size();
        int length = blocks.get(0).get(0).size();

        for (int z = 0; z < length; z++) {
            new_blocks.add(new ArrayList<ArrayList<IBlockState>>());
            for (int y = 0; y < height; y++) {
                new_blocks.get(z).add(new ArrayList<IBlockState>());
                for (int x = 0; x < width; x++) {
                    if (!Circuit.TEST) new_blocks.get(z).get(y).add(Blocks.AIR.getDefaultState());
                }
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    IBlockState block = this.blocks.get(x).get(y).get(z);

                    for (Map.Entry<IProperty<?>, Comparable<?>> e : block.getProperties().entrySet()) {
                        if (e.getKey().getName().equals("facing")) {
                            block = block.getBlock().getDefaultState().withProperty(Utils.getPropertyByName(block.getBlock(), "facing"), rot_facing.get(e.getValue()));
                        } else if (e.getKey().getName().equals("rotation")) {
                            block = block.getBlock().getDefaultState().withProperty(Utils.getPropertyByName(block.getBlock(), "rotation"), ((((((Integer)e.getValue()) - 4) % 16) + 16) % 16));
                        }

                    }
                    new_blocks.get(z).get(y).set(width - 1 - x, block);
                }
            }
        }

        HashMap<Vec3i, TileEntity> new_te_map = new HashMap<Vec3i, TileEntity>();
        for (Map.Entry<Vec3i, TileEntity> e : te_map.entrySet()) {
            Vec3i v = e.getKey();
            new_te_map.put(new Vec3i(v.getZ(), v.getY(), width - 1 - v.getX()), e.getValue());
        }

        this.blocks = new_blocks;
        this.te_map = new_te_map;
    }

    public void rotateRight() {
        HashMap<EnumFacing, EnumFacing> rot_facing = new HashMap<EnumFacing, EnumFacing>();
        rot_facing.put(EnumFacing.DOWN, EnumFacing.DOWN);
        rot_facing.put(EnumFacing.UP, EnumFacing.UP);
        rot_facing.put(EnumFacing.NORTH, EnumFacing.EAST);
        rot_facing.put(EnumFacing.SOUTH, EnumFacing.WEST);
        rot_facing.put(EnumFacing.WEST, EnumFacing.NORTH);
        rot_facing.put(EnumFacing.EAST, EnumFacing.SOUTH);

        ArrayList<ArrayList<ArrayList<IBlockState>>> new_blocks = new ArrayList<ArrayList<ArrayList<IBlockState>>>();

        int width = blocks.size();
        int height = blocks.get(0).size();
        int length = blocks.get(0).get(0).size();

        for (int z = 0; z < length; z++) {
            new_blocks.add(new ArrayList<ArrayList<IBlockState>>());
            for (int y = 0; y < height; y++) {
                new_blocks.get(z).add(new ArrayList<IBlockState>());
                for (int x = 0; x < width; x++) {
                    if (!Circuit.TEST) new_blocks.get(z).get(y).add(Blocks.AIR.getDefaultState());
                }
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    IBlockState block = this.blocks.get(x).get(y).get(z);

                    for (Map.Entry<IProperty<?>, Comparable<?>> e : block.getProperties().entrySet()) {
                        if (e.getKey().getName().equals("facing")) {
                            block = block.getBlock().getDefaultState().withProperty(Utils.getPropertyByName(block.getBlock(), "facing"), rot_facing.get(e.getValue()));
                        } else if (e.getKey().getName().equals("rotation")) {
                            block = block.getBlock().getDefaultState().withProperty(Utils.getPropertyByName(block.getBlock(), "rotation"), ((((((Integer)e.getValue()) + 4) % 16) + 16) % 16));
                        }
                    }
                    new_blocks.get(length - 1 - z).get(y).set(x, block);
                }
            }
        }

        HashMap<Vec3i, TileEntity> new_te_map = new HashMap<Vec3i, TileEntity>();
        for (Map.Entry<Vec3i, TileEntity> e : te_map.entrySet()) {
            Vec3i v = e.getKey();
            new_te_map.put(new Vec3i(length - 1 - v.getZ(), v.getY(), v.getX()), e.getValue());
        }

        this.blocks = new_blocks;
        this.te_map = new_te_map;
    }

    public void rotate180() {

    }
}
