// This is free and unencumbered software released into the public domain.
// Author: NotAlexNoyle.
package plugin;

import java.util.ArrayList;
// Import required libraries.
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

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

	// Listen for entities being spawned.
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {

		// If the entity that was spawned is an end crystal, and it is in the spawn region, do this...
		if(isInSpawnRegion(BukkitAdapter.adapt(event.getEntity().getLocation())) && event.getEntityType().equals(EntityType.ENDER_CRYSTAL)) {

			// Wait a few seconds.
			Bukkit.getScheduler().runTaskLater(HolyGrailOG.getPlugin(), new Runnable() {
				// Run a scheduled task.
				@Override
				public void run() {

					// Restore the end crystal to its original location.
					event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.ENDER_CRYSTAL);

				}
				// Declare amount of ticks to wait (20L = 1 tick).
			}, 100L);

		}
		// TODO: Attempt to always cancel damage in duels arenas.
		else {

			if(event.getEntity().getType().equals(EntityType.ENDER_CRYSTAL)){

				event.setCancelled(false);

			}

		}

	}

	// TODO: Complete the mechanism for staff to intentionally delete crystals.
	// Listen for a player interacting with a block or entity.
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityInteract(PlayerInteractEntityEvent event) {

		// Get the nature of the interaction and store it.
		Entity entityClicked = event.getRightClicked();

		// If the player right clicked on the block or entity, do this...
		if(entityClicked.getType().equals(EntityType.ENDER_CRYSTAL)) {

			// Get the player who interacted and store them.
			Player player = event.getPlayer();

			// TODO: Remove dev logger.
			HolyGrailOG.getPlugin();
			HolyGrailOG.getPlugin().getLogger().info("Player " + player.getName() + " right clicked an ender crystal.");

		}

	}

	// Listen for entities being spawned.
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntitySpawn(EntitySpawnEvent event) {

		// If the entity that was spawned is an end crystal, and it is in the spawn region, do this...
		if(isInSpawnRegion(BukkitAdapter.adapt(event.getLocation())) && event.getEntityType().equals(EntityType.ENDER_CRYSTAL)) {

			// If an end crystal already exists at the location, do this...
			if(crystalsReasonablyNearby(event.getEntity()).contains("ender_crystal")) {

				// Don't spawn in another crystal since there is one already.
				event.setCancelled(true);

			}
			else {

				// Spawn particles to indicate the end crystal being restored.
				event.getEntity().getWorld().spawnParticle(Particle.SPELL, event.getEntity().getLocation(), 6);

			}

		}

	}

	// TODO: Move crystal finding logic here so its re-usable.
	private ArrayList<String> crystalsReasonablyNearby(Entity eventEntity) {

		// Make a list of to hold the nearby entities sized of the maximum reasonable amount of nearby entities.
		List<Entity> entityList = new ArrayList<Entity>(3457);
		
		// Make a list of to hold the nearby entity names sized of the maximum reasonable amount of nearby entities.
		ArrayList<String> entityConverter = new ArrayList<String>(3457);

		// Iterate over the nearby entities with a pre-set maximum.
		for(int i = 0; i < entityList.size(); i++) {

			// Attempt to run error-throwing code.
			try {

				// Get a list of nearby crystal entities.
				entityList.add(eventEntity.getNearbyEntities(2, 2, 2).get(i));
				entityConverter.add(eventEntity.getNearbyEntities(2, 2, 2).get(i).getType().toString().toLowerCase());

			}
			// If the amount of entities is less than 3457, this error will be thrown.
			catch(IndexOutOfBoundsException error) {

				// Stop the loop if maximum entity count was reached. The last value of i will be used as the list size.
				break;

			}

		}

		// Return the completed entity list.
		return entityConverter;

	}

	// Declare a function for checking whether a location is in spawn.
	private boolean isInSpawnRegion(Location worldGuardBlockLocation) {

		// Get the list of regions at the location using the WorldGuard API.
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