// This is free and unencumbered software released into the public domain.
// Author: NotAlexNoyle.
package plugin;

// Import required libraries.
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import com.destroystokyo.paper.event.block.AnvilDamagedEvent.DamageState;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

// Declare listener class.
public class Listeners implements Listener {

	// Listen for an anvil being damaged.
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAnvilBreak(AnvilDamagedEvent event) {

		// If the anvil is in the spawn region, do this...
		if(isInSpawnRegion(BukkitAdapter.adapt(event.getInventory().getLocation())) && event.isBreaking()) {

			// Spawn a particle effect to indicate the anvil restoration.
			event.getInventory().getLocation().getWorld().spawnParticle(Particle.SPELL, event.getInventory().getLocation(), 5);

			// Restore the anvil to full.
			event.setDamageState(DamageState.FULL);

		}

	}

	// Listen for entities being harmed.
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {

		// If the entity that was harmed is an end crystal, and it is in the spawn region, do this...
		if(isInSpawnRegion(BukkitAdapter.adapt(event.getEntity().getLocation())) && event.getEntityType().equals(EntityType.ENDER_CRYSTAL)) {

			// Spawn a particle to indicate the temporary deleted state of an end crystal.
			event.getEntity().getLocation().getWorld().spawnParticle(Particle.SPELL, event.getEntity().getLocation(), 5);

			// Wait a few seconds.
			Bukkit.getScheduler().runTaskLater(HolyGrailOG.getPlugin(), new Runnable() {
				// Run a scheduled task.
				@Override
				public void run() {

					// Spawn particles to indicate the end crystal being restored.
					event.getEntity().getLocation().getWorld().spawnParticle(Particle.SPELL, event.getEntity().getLocation(), 5);
					
					// Restore the end crystal to its original location.
					event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.ENDER_CRYSTAL);

				}
			// Declare amount of ticks to wait (20L = 1 tick)
			}, 100L);

		}

	}

	// Declare a function for checking whether a location is in spawn.
	private boolean isInSpawnRegion(Location worldGuardBlockLocation) {

		// Get the list of regions at the location from the WorldGuard API.
		WorldGuard.getInstance();
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery query = container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(worldGuardBlockLocation);
		List<ProtectedRegion> region = Lists.newArrayList(set);

		// Keeps track of whether spawn is found during the loop on any iteration.
		boolean isInSpawn = false;
		// Iterate through all of the location's WorldGuard regions.
		for(int i = 0; i < region.size(); i++) {

			// If the region matches "spawn", do this...
			if(region.get(i).getId().equals("spawn")) {

				// Pass affirmative result.
				isInSpawn = true;

			}

		}

		// Return outcome.
		return isInSpawn;

	}

}