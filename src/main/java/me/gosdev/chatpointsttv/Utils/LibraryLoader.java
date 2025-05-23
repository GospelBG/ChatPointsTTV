package me.gosdev.chatpointsttv.Utils;

import java.util.ArrayList;

import com.alessiodp.libby.BukkitLibraryManager;
import com.alessiodp.libby.Library;
import com.alessiodp.libby.logging.LogLevel;

import me.gosdev.chatpointsttv.ChatPointsTTV;

public class LibraryLoader {

    private static BukkitLibraryManager libraryManager;
    private final static String relocationBase = "me{}gosdev{}chatpointsttv{}libraries{}";

    public static ArrayList<Library> libraries = new ArrayList<Library>() {
        {
            add(Library.builder()
                .groupId("org{}json")
                .artifactId("json")
                .version("20240303")
                .relocate("org{}json{}json", relocationBase + "json")
                .resolveTransitiveDependencies(true)
                .build());

            // Define the main libraries with relocations
            add(Library.builder()
                .groupId("com{}github{}philippheuer.events4j")
                .artifactId("events4j-handler-simple")
                .version("0.12.2")
                .relocate("com{}github{}philippheuer.events4j", relocationBase + "events4j")
                .resolveTransitiveDependencies(true)
                .build());
                
            add(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j")
                .version("1.23.0")
                .resolveTransitiveDependencies(true)
                .relocate("com{}github{}twitch4j{}twitch4j", relocationBase + "twitch4j")
                .relocate("com{}fasterxml{}jackson", relocationBase + "jackson")
                .build());
        }
    };

    public static void LoadLibraries(ChatPointsTTV plugin) {
        libraryManager = new BukkitLibraryManager(plugin);
        libraryManager.setLogLevel(LogLevel.DEBUG);
        libraryManager.addMavenCentral();
        ChatPointsTTV.log.info("Loading libraries...");
        for (Library lib : libraries) {
            try {
                libraryManager.loadLibrary(lib);
            } catch (Exception e) {
                ChatPointsTTV.log.severe("Failed to load library: " + lib.getArtifactId() + " v" + lib.getVersion() + " \n" + e.getMessage());
            }
            
        }
    }
}
