package me.gosdev.chatpointsttv.Utils;

import java.util.ArrayList;

import me.gosdev.chatpointsttv.ChatPointsTTV;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;

public class LibraryLoader {

    private static BukkitLibraryManager libraryManager;
    private final static String relocationBase = "me{}gosdev{}chatpointsttv{}libraries{}";

    public static ArrayList<Library> libraries = new ArrayList<Library>() {
        {
            add(Library.builder()
                .groupId("com{}github{}twitch4j")
                .artifactId("twitch4j")
                .version("1.23.0")
                .relocate("com{}github{}twitch4j{}twitch4j", relocationBase + "twitch4j")
                .build());

        add(Library.builder()
                .groupId("com{}github{}philippheuer{}events4j")
                .artifactId("events4j-handler-simple")
                .version("0.12.2")
                .relocate("com{}github{}philippheuer{}events4j", relocationBase + "events4j")
                .build());

        add(Library.builder()
                .groupId("org{}json")
                .artifactId("json")
                .version("20240303")
                .relocate("org{}json{}json", relocationBase + "json")
                .build());
        }
    };

    public static void LoadLibraries(ChatPointsTTV plugin) {
        libraryManager = new BukkitLibraryManager(plugin);
        libraryManager.addMavenCentral();
        for (Library lib : libraries) {
            libraryManager.loadLibrary(lib);
        }
    }
}
