package de.srendi.advancedperipherals.common.blocks.tileentity;

import de.srendi.advancedperipherals.common.addons.computercraft.peripheral.EnergyDetectorPeripheral;
import de.srendi.advancedperipherals.common.blocks.EnergyDetectorBlock;
import de.srendi.advancedperipherals.common.blocks.base.PeripheralTileEntity;
import de.srendi.advancedperipherals.common.configuration.AdvancedPeripheralsConfig;
import de.srendi.advancedperipherals.common.setup.TileEntityTypes;
import de.srendi.advancedperipherals.common.util.EnergyStorageProxy;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EnergyDetectorTileEntity extends PeripheralTileEntity<EnergyDetectorPeripheral> implements ITickableTileEntity {

    public int transferRate = 0;
    // storageProxy that will forward the energy to the output but limit it to maxTransferRate
    public EnergyStorageProxy storageProxy = new EnergyStorageProxy(this, AdvancedPeripheralsConfig.energyDetectorMaxFlow);
    LazyOptional<IEnergyStorage> energyStorageCap = LazyOptional.of(() -> storageProxy);
    Direction energyInDirection = Direction.NORTH;
    Direction energyOutDirection = Direction.SOUTH;
    @NotNull
    private Optional<IEnergyStorage> outReceivingStorage = Optional.empty();
    // an zero size, zero transfer energy storage to enshure that cables connect
    private EnergyStorage zeroStorage = new EnergyStorage(0, 0, 0);
    LazyOptional<IEnergyStorage> zeroStorageCap = LazyOptional.of(() -> zeroStorage);

    public EnergyDetectorTileEntity() {
        super(TileEntityTypes.ENERGY_DETECTOR.get());
    }

    @Override
    protected EnergyDetectorPeripheral createPeripheral() {
        return new EnergyDetectorPeripheral("energyDetector", this);
    }

    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction direction) {
        energyInDirection = getBlockState().get(EnergyDetectorBlock.FACING);
        energyOutDirection = getBlockState().get(EnergyDetectorBlock.FACING).getOpposite();
        if (cap == CapabilityEnergy.ENERGY) {
            if (direction == energyInDirection) {
                return energyStorageCap.cast();
            } else if (direction == energyOutDirection) {
                return zeroStorageCap.cast();
            }
        }
        return super.getCapability(cap, direction);
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            // this handles the rare edge case that receiveEnergy is called multiple times in one tick
            transferRate = storageProxy.getTransferedInThisTick();
            storageProxy.resetTransferedInThisTick();
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("rateLimit", storageProxy.getMaxTransferRate());
        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        storageProxy.setMaxTransferRate(nbt.getInt("rateLimit"));
        super.read(state, nbt);
    }

    // returns the cached output storage of the receiving block or refetches it if it has been invalidated
    @NotNull
    public Optional<IEnergyStorage> getOutputStorage() {
        // the documentation says that the value of the LazyOptional should be cached locally and invallidated using addListener
        if (!outReceivingStorage.isPresent()) {
            TileEntity teOut = world.getTileEntity(pos.offset(energyOutDirection));
            if (teOut == null) {
                return Optional.empty();
            }
            LazyOptional<IEnergyStorage> lazyOptionalOutStorage = teOut.getCapability(CapabilityEnergy.ENERGY, energyOutDirection.getOpposite());
            outReceivingStorage = lazyOptionalOutStorage.resolve();
            lazyOptionalOutStorage.addListener(l -> {
                outReceivingStorage = Optional.empty();
            });
        }
        return outReceivingStorage;
    }
}