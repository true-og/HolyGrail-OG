// This is free and unencumbered software released into the public domain.
// Author: NotAlexNoyle.
package plugin;

import java.util.ArrayList;
// Import required libraries.
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

// Declare listener class.
public class Listeners implements Listener {

	private BukkitTask task = null;

	// Listen for an anvil being damaged.
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAnvilBreak(AnvilDamagedEvent event) {

		// If the anvil is in the spawn region, do this...
		if(event.isBreaking() && getRegionNames(BukkitAdapter.adapt(event.getInventory().getLocation())).contains("spawn") ) {

			// Spawn a particle effect to indicate the anvil restoration.
			event.getInventory().getLocation().getWorld().spawnParticle(Particle.SPELL, event.getInventory().getLocation(), 5);

			// Restore the anvil to unused status.
			event.setDamageState(DamageState.FULL);

		}

	}

	// Listen for entities being damaged.
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {

		// TODO: Test destroying crystals in vs outside of duels.
		// TODO: Test to make sure crystals only do damage to those in a duel match.

		// If the entity that was damaged is an ender crystal, do this...
		if(event.getEntityType().equals(EntityType.ENDER_CRYSTAL)) {

			// Get a list of regions from WorldGuard.
			ArrayList<String> regionQuery = getRegionNames(BukkitAdapter.adapt(event.getEntity().getLocation()));

			// If the ender crystal that was damaged is in the spawn region, do this...
			if(regionQuery.contains("spawn")) {

				CharSequence arenaQuery = "arena";
				if(! doAnyRegionNamesContain(arenaQuery, event.getEntity())) {

					event.setCancelled(true);

				}
				else {

					// TODO: Test the pending restore particles.
					// Create a final array to capture the value of the remaining seconds (similar to a loop).
					final int[] secondsRemaining = { 5 };

					// Schedule a repeating task to show pending end crystal restore particles.
					task = Bukkit.getScheduler().runTaskTimer(HolyGrailOG.getPlugin(), new Runnable() {

						@Override
						public void run() {

							event.getEntity().getWorld().spawnParticle(Particle.FIREWORKS_SPARK, event.getEntity().getLocation(), 3);

							// Decrease the remaining time
							secondsRemaining[0] = secondsRemaining[0] - 1;

							// Check if we should stop the task
							if (secondsRemaining[0] <= 0) {

								Bukkit.getScheduler().cancelTask(task.getTaskId()); // Cancel the task

							}

						}
					}, 0L, 20L); // The '0L' means start immediately, and '20L' means run every 20 ticks (1 second).

					// Wait 5 seconds, so that it doesn't appear that the crystal didn't get destroyed.
					Bukkit.getScheduler().runTaskLater(HolyGrailOG.getPlugin(), new Runnable() {
						// Run a scheduled task.
						@Override
						public void run() {

							// TODO: Delete all crystals at the entity location to make room for the new one.

							// Restore the ender crystal to its original location.
							event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.ENDER_CRYSTAL);

						}
					}, 20L); // '20L' means run every 20 ticks (1 second).

				}

			}

		}

	}

	// Listen for entities exploding.
	/*@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityExplosion(EntityExplodeEvent event) {

		// Stop explosion from being cancelled in arenas.
		if(doAnyRegionNamesContain("arena", event.getEntity())) {

			event.setCancelled(false);

		}

	}*/

	// Listen for a player interacting with a block or entity.
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityInteract(PlayerInteractEntityEvent event) {

		// Get the nature of the interaction.
		Entity entityClicked = event.getRightClicked();

		// If the player right clicked on an ender crystal, do this...
		if(entityClicked.getType().equals(EntityType.ENDER_CRYSTAL)) {

			// Get the player who performed the interaction.
			Player player = event.getPlayer();

			// Get the item in the player's main hand.
			ItemStack itemInHand = player.getInventory().getItemInMainHand();

			// If the player is holding an ender crystal in their hand, and they have permission, send them an ender crystal deletion confirmation prompt.
			if(itemInHand.getType().equals(Material.END_CRYSTAL) && player.hasPermission("holygrail.deletecrystals")) {

				// TODO: Delete all crystals at the entity location according to player command.

				sendClickableMessage(player, entityClicked.getLocation().getX(), entityClicked.getLocation().getY(), entityClicked.getLocation().getBlockZ());

			}

		}

	}

	// Listen for entities being spawned.
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntitySpawn(EntitySpawnEvent event) {

		// If the entity that was spawned is an ender crystal, and it is in the spawn region, do this...
		if(event.getEntityType().equals(EntityType.ENDER_CRYSTAL) && getRegionNames(BukkitAdapter.adapt(event.getLocation())).contains("spawn")) {

			// Make a list of to hold the nearby entities sized of the maximum reasonable amount of nearby entities.
			List<Entity> entityList = new ArrayList<Entity>(3457);

			// Make a list of to hold the nearby entity names sized of the maximum reasonable amount of nearby entities.
			ArrayList<String> entityTitles = new ArrayList<String>(3457);

			// Iterate over the nearby entities with a pre-set maximum.
			for(int i = 0; i < entityList.size(); i++) {

				// Attempt to run error-throwing code.
				try {

					// Get a list of nearby entities.
					entityList.add(event.getEntity().getNearbyEntities(2, 2, 2).get(i));

					// Get a list of nearby entities' titles.
					entityTitles.add(event.getEntity().getNearbyEntities(2, 2, 2).get(i).getType().toString().toLowerCase());

				}
				// If the amount of entities is less than 3457, this error will be thrown.
				catch(IndexOutOfBoundsException error) {

					// Stop the loop if maximum entity count was reached. The last value of i will be used as the list size.
					break;

				}

			}

			// If an ender crystal already exists at the location, do this...
			if(entityTitles.contains("ender_crystal")) {

				// Don't spawn in another crystal since there is one already.
				event.setCancelled(true);

			}
			else {

				// Spawn particles to indicate the ender crystal being restored.
				event.getEntity().getWorld().spawnParticle(Particle.SPELL, event.getEntity().getLocation(), 6);

			}

		}

	}

	// Declare a function for searching for specific sequences of characters in region names.
	private boolean doAnyRegionNamesContain(CharSequence lookup, Entity entity) {

		// Get a list of regions from WorldGuard.
		ArrayList<String> regionQuery = getRegionNames(BukkitAdapter.adapt(entity.getLocation()));

		boolean queryFound = false;

		for(int i = 0; i < regionQuery.size(); i++) {

			if(regionQuery.get(i).contains(lookup)) {

				queryFound = true;

			}

		}

		return queryFound;

	}

	// Declare a function for checking whether a location is in a region.
	private ArrayList<String> getRegionNames(Location worldGuardBlockLocation) {

		// Query the list of regions at the location using the WorldGuard API.
		WorldGuard.getInstance();
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery query = container.createQuery();
		ApplicableRegionSet set = query.getApplicableRegions(worldGuardBlockLocation);
		List<ProtectedRegion> region = Lists.newArrayList(set);
		ArrayList<String> regionNames = new ArrayList<String>(region.size());
		regionNames.add("none");
		for(int i = 0; i < region.size(); i++) {

			regionNames.add(i, region.get(i).getId());

		}

		// Return a list of all applicable regions.
		return regionNames;

	}

	// Turns chat text into a command button.
	public static void sendClickableMessage(Player player, double x, double y, double z) {

		// Declare a TextComponent with contents.
		TextComponent clickableText = Component.text("[Click Here]");

		// Style the clickable text.
		clickableText.color(NamedTextColor.AQUA).decoration(TextDecoration.BOLD, TextDecoration.State.TRUE);

		// Create a click event on the chat text.
		clickableText = clickableText.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "tps"));

		// Send the clickable text to the player.
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Are you sure you want to &cdelete &6the ender crystal at X = &e" + x + "&6, Y = &e" + y + "&6, Z = &e" + z + "&6? To confirm: &B") + clickableText.content());

	}

}