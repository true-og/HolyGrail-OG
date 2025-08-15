// This is free and unencumbered software released into the public domain.
// Author: NotAlexNoyle.
package plugin;

// Import required libraries.
import org.bukkit.plugin.java.JavaPlugin;

// Extending this class is standard bukkit boilerplate for any plugin, or else the server software won't load the
// classes.
public class HolyGrailOG extends JavaPlugin {

    // Declare variable to hold class for passing.
    private static HolyGrailOG plugin;

    // What to do when the plugin is run by the server.
    public void onEnable() {

        // Assign the plugin variable to the main class instance.
        plugin = this;

        // Register the event.
        getServer().getPluginManager().registerEvents(new Listeners(), this);

    }

    // Class constructor.
    public static HolyGrailOG getPlugin() {

        // Pass instance of main to other classes.
        return plugin;

    }

}
